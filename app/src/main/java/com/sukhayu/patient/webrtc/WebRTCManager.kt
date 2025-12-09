package com.sukhayu.patient.webrtc

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import org.webrtc.*
import java.util.concurrent.TimeUnit

class WebRTCManager(
    private val context: Context,
    private val localVideoView: SurfaceViewRenderer,
    private val remoteVideoView: SurfaceViewRenderer,
    private val patientId: String,
    private var doctorId: String
) {

    companion object {
        private const val TAG = "WebRTCManager"

        // CHANGE THIS (IMPORTANT)
        private const val SIGNALING_URL = "wss://ashartc.onrender.com/ws"
    }

    private var webSocket: WebSocket? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    private var preferredDoctorLevel: String? = "MO"

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    // Callbacks
    var onCallConnected: (() -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onDoctorFound: ((String) -> Unit)? = null

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        initPeerConnectionFactory()
    }

    // ----------------------------
    // INITIALIZATION
    // ----------------------------

    private fun initPeerConnectionFactory(): PeerConnectionFactory {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )

        return PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun initializeWebSocket(level: String? = "MO") {
        preferredDoctorLevel = level
        Log.d(TAG, "Initializing WebSocket: $SIGNALING_URL")

        val client = OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url(SIGNALING_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket OPENED")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Message: $text")
                handleSignalingMessage(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket FAILED: ${t.message}")
                onError?.invoke("WebSocket failed: ${t.message}")
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket CLOSING: $reason")
            }
        })
    }

    // ----------------------------
    // SERVER INTERACTIONS
    // ----------------------------

    private fun registerAsPatient() {
        val msg = JSONObject().apply {
            put("type", "register")
            put("id", patientId)
            put("role", "patient")
        }
        Log.d(TAG, "Registering as PATIENT: $msg")
        webSocket?.send(msg.toString())
    }

    private fun findDoctor() {
        val msg = JSONObject().apply {
            put("type", "call-request")
            put("preferredLevel", preferredDoctorLevel ?: "MO")
        }
        Log.d(TAG, "REQUESTING doctor: $msg")
        webSocket?.send(msg.toString())
    }

    private fun sendOffer(sdp: SessionDescription) {
        val msg = JSONObject().apply {
            put("type", "offer")
            put("toUserID", doctorId)
            put("payload", JSONObject().apply {
                put("type", sdp.type.canonicalForm())
                put("sdp", sdp.description)
            })
        }
        webSocket?.send(msg.toString())
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val msg = JSONObject().apply {
            put("type", "ice")
            put("toUserID", doctorId)
            put("payload", JSONObject().apply {
                put("candidate", candidate.sdp)
                put("sdpMid", candidate.sdpMid)
                put("sdpMLineIndex", candidate.sdpMLineIndex)
            })
        }
        webSocket?.send(msg.toString())
    }

    // ----------------------------
    // PEER CONNECTION
    // ----------------------------

    private fun createPeerConnection() {
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory.createPeerConnection(config,
            object : PeerConnection.Observer {

                override fun onIceCandidate(c: IceCandidate) {
                    sendIceCandidate(c)
                }

                override fun onAddStream(stream: MediaStream) {
                    if (stream.videoTracks.isNotEmpty()) {
                        stream.videoTracks[0].addSink(remoteVideoView)
                    }
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "ICE STATE: $state")
                    if (state == PeerConnection.IceConnectionState.CONNECTED) {
                        onCallConnected?.invoke()
                    }
                    if (state == PeerConnection.IceConnectionState.DISCONNECTED ||
                        state == PeerConnection.IceConnectionState.FAILED
                    ) {
                        onCallEnded?.invoke()
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {}
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(dc: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(r: RtpReceiver, ms: Array<out MediaStream>) {}
            }
        )

        setupLocalMedia()
    }

    private fun setupLocalMedia() {
        val videoCapturer = createVideoCapturer()

        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(
            SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext),
            context,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(720, 480, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("local_video", videoSource)
        localVideoTrack?.addSink(localVideoView)

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)

        peerConnection?.addTrack(localVideoTrack, listOf("local_stream"))
        peerConnection?.addTrack(localAudioTrack, listOf("local_stream"))
    }

    private fun createVideoCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val devices = enumerator.deviceNames

        devices.firstOrNull { enumerator.isFrontFacing(it) }?.let {
            return enumerator.createCapturer(it, null)
        }
        devices.firstOrNull { enumerator.isBackFacing(it) }?.let {
            return enumerator.createCapturer(it, null)
        }

        throw RuntimeException("No camera found")
    }

    // ----------------------------
    // SIGNALLING MESSAGE HANDLER
    // ----------------------------

    private fun handleSignalingMessage(msg: String) {
        val json = JSONObject(msg)
        when (json.getString("type")) {

            "socket-id" -> registerAsPatient()

            "registered" -> findDoctor()

            "doctor-assigned" -> {
                doctorId = json.getString("doctorID")
                onDoctorFound?.invoke(doctorId)
                createPeerConnection()
                createOffer()
            }

            "answer" -> {
                val payload = json.getJSONObject("payload")
                val sdp = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    payload.getString("sdp")
                )
                peerConnection?.setRemoteDescription(
                    SimpleSdpObserver(),
                    sdp
                )
            }

            "ice" -> {
                val payload = json.getJSONObject("payload")
                val c = IceCandidate(
                    payload.getString("sdpMid"),
                    payload.getInt("sdpMLineIndex"),
                    payload.getString("candidate")
                )
                peerConnection?.addIceCandidate(c)
            }
        }
    }

    private fun createOffer() {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                sendOffer(sdp)
            }
            override fun onCreateFailure(error: String) {
                onError?.invoke("Offer failed: $error")
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    // ----------------------------
    // CLEANUP
    // ----------------------------

    fun endCall() {
        val msg = JSONObject().apply {
            put("type", "call-status")
            put("status", "ended")
            put("toUserID", doctorId)
        }
        webSocket?.send(msg.toString())
        cleanup()
    }

    private fun cleanup() {
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        peerConnection?.close()
        webSocket?.close(1000, "bye")
    }

    inner class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(msg: String?) {}
        override fun onSetFailure(msg: String?) {}
    }
}
