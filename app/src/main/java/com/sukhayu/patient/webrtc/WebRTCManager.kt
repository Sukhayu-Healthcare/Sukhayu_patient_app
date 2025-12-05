package com.sukhayu.patient.webrtc

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import org.webrtc.*
import java.util.concurrent.TimeUnit

class WebRTCManager(
    private val context: Context,
    private val localVideoView: SurfaceViewRenderer?,
    private val remoteVideoView: SurfaceViewRenderer?,
    private val patientId: String,
    private var doctorId: String,
    private val eglBaseContext: EglBase.Context
) {

    private val TAG = "WebRTCManager"

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var videoCapturer: VideoCapturer? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var remoteVideoTrack: VideoTrack? = null

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    var onDoctorFound: ((String) -> Unit)? = null
    var onCallConnected: (() -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun setRemoteRenderer(renderer: SurfaceViewRenderer) {
        remoteVideoView?.let {
            remoteVideoTrack?.addSink(renderer)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // WebSocket Init
    // ---------------------------------------------------------------------------------------------
    fun initializeWebSocket(preferredLevel: String = "CHO") {
        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("wss://ashartc.onrender.com")
            .build()

        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Received signaling message: $text")
                handleSignalingMessage(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                onError?.invoke("WebSocket failure: ${t.message}")
            }
        })
    }

    // ---------------------------------------------------------------------------------------------
    // Doctor Search
    // ---------------------------------------------------------------------------------------------
    fun findDoctor(preferredLevel: String? = "MO") {
        val req = JSONObject()
        req.put("type", "call-request")
        req.put("preferredLevel", preferredLevel)
        webSocket?.send(req.toString())
    }

    // ---------------------------------------------------------------------------------------------
    // Call Setup
    // ---------------------------------------------------------------------------------------------
    fun initiateCall(doctorSocketId: String) {
        doctorId = doctorSocketId
        ensureFactory()
        createPeerConnection()
        setupLocalMedia()

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                sendOffer(desc!!)
            }
        }, MediaConstraints())
    }

    fun endCall() {
        val msg = JSONObject()
        msg.put("type", "call-status")
        msg.put("toUserID", doctorId)
        msg.put("status", "ended")
        webSocket?.send(msg.toString())

        cleanup()
    }

    // ---------------------------------------------------------------------------------------------
    // PeerConnection Factory
    // ---------------------------------------------------------------------------------------------
    private fun ensureFactory() {
        if (peerConnectionFactory != null) return

        val initOpts = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(initOpts)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .createPeerConnectionFactory()
    }

    // ---------------------------------------------------------------------------------------------
    // PeerConnection
    // ---------------------------------------------------------------------------------------------
    private fun createPeerConnection() {
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            config,
            object : PeerConnection.Observer {

                override fun onIceCandidate(candidate: IceCandidate) {
                    sendIceCandidate(candidate)
                }

                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                    val track = receiver?.track()
                    if (track is VideoTrack) {
                        remoteVideoTrack = track
                        remoteVideoView?.let { track.addSink(it) }
                    }
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    if (state == PeerConnection.IceConnectionState.CONNECTED)
                        onCallConnected?.invoke()
                    if (state == PeerConnection.IceConnectionState.DISCONNECTED ||
                        state == PeerConnection.IceConnectionState.FAILED
                    ) onCallEnded?.invoke()
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {}
                override fun onDataChannel(dc: DataChannel?) {}
                override fun onSignalingChange(state: PeerConnection.SignalingState) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onRenegotiationNeeded() {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            }
        )
    }

    // ---------------------------------------------------------------------------------------------
    // Local Media
    // ---------------------------------------------------------------------------------------------
    private fun setupLocalMedia() {
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)

        videoCapturer = Camera2Enumerator(context).run {
            deviceNames.firstOrNull { isFrontFacing(it) }
                ?.let { createCapturer(it, null) }
        }

        videoSource = peerConnectionFactory!!.createVideoSource(false)
        videoCapturer!!.initialize(surfaceTextureHelper, context, videoSource!!.capturerObserver)
        videoCapturer!!.startCapture(720, 480, 30)

        localVideoTrack = peerConnectionFactory!!.createVideoTrack("local_video", videoSource)
        localVideoView?.let { localVideoTrack!!.addSink(it) }

        audioSource = peerConnectionFactory!!.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory!!.createAudioTrack("local_audio", audioSource)

        peerConnection?.addTrack(localVideoTrack, listOf("stream-$patientId"))
        peerConnection?.addTrack(localAudioTrack, listOf("stream-$patientId"))
    }

    // ---------------------------------------------------------------------------------------------
    // Signaling Send
    // ---------------------------------------------------------------------------------------------
    private fun sendOffer(sdp: SessionDescription) {
        val msg = JSONObject()
        msg.put("type", "offer")
        msg.put("toUserID", doctorId)

        msg.put("payload", JSONObject().apply {
            put("type", sdp.type.canonicalForm())
            put("sdp", sdp.description)
        })

        webSocket?.send(msg.toString())
    }

    private fun sendIceCandidate(c: IceCandidate) {
        val msg = JSONObject()
        msg.put("type", "ice")
        msg.put("toUserID", doctorId)
        msg.put("payload", JSONObject().apply {
            put("candidate", c.sdp)
            put("sdpMid", c.sdpMid)
            put("sdpMLineIndex", c.sdpMLineIndex)
        })
        webSocket?.send(msg.toString())
    }

    // ---------------------------------------------------------------------------------------------
    // Signaling Receive
    // ---------------------------------------------------------------------------------------------
    private fun handleSignalingMessage(msg: String) {
        val json = JSONObject(msg)
        when (json.getString("type")) {

            "socket-id" -> {
                val reg = JSONObject()
                reg.put("type", "register")
                reg.put("id", patientId)
                reg.put("role", "patient")
                webSocket?.send(reg.toString())
            }

            "registered" -> findDoctor(null)

            "doctor-assigned" -> {
                val doctorSocket = json.getString("doctorID")
                onDoctorFound?.invoke(doctorSocket)
            }

            "answer" -> {
                val pay = json.getJSONObject("payload")
                val sdp = SessionDescription(SessionDescription.Type.ANSWER, pay.getString("sdp"))
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
            }

            "ice" -> {
                val pay = json.getJSONObject("payload")
                val ice = IceCandidate(
                    pay.getString("sdpMid"),
                    pay.getInt("sdpMLineIndex"),
                    pay.getString("candidate")
                )
                peerConnection?.addIceCandidate(ice)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Cleanup
    // ---------------------------------------------------------------------------------------------
    private fun cleanup() {
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoSource?.dispose()
        audioSource?.dispose()
        peerConnection?.close()
        peerConnection?.dispose()
        webSocket?.close(1000, "Closed")
    }

    // ---------------------------------------------------------------------------------------------
    // Simple Observer
    // ---------------------------------------------------------------------------------------------
    open class SimpleSdpObserver(
        private val onCreateSuccessCb: ((SessionDescription?) -> Unit)? = null
    ) : SdpObserver {

        override fun onCreateSuccess(desc: SessionDescription?) {
            onCreateSuccessCb?.invoke(desc)
        }

        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
