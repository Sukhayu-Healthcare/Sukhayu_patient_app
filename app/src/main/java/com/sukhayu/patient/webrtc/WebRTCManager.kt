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
    private var doctorId: String,
    private val eglBaseContext: EglBase.Context
) {
    private var webSocket: WebSocket? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var patientSocketId: String = ""
    private var preferredDoctorLevel: String? = null
    
    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        initPeerConnectionFactory()
    }
    
    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )
    
    var onCallConnected: (() -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onDoctorFound: ((String) -> Unit)? = null

    private fun initPeerConnectionFactory(): PeerConnectionFactory {
        val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)

        val options = PeerConnectionFactory.Options()
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
    }

    fun initializeWebSocket(preferredLevel: String? = "MO") {
        this.preferredDoctorLevel = preferredLevel
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("wss://ashartc.onrender.com")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                // Wait for socket-id before registering
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleSignalingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}")
                onError?.invoke("Connection failed: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $reason")
            }
        })
    }

    private fun registerAsPatient() {
        val registerMessage = JSONObject().apply {
            put("type", "register")
            put("id", patientId)
            put("role", "patient") // Server expects "patient" or "doctor"
        }
        Log.d(TAG, "Registering as patient: $patientId")
        webSocket?.send(registerMessage.toString())
    }

    fun findDoctor(preferredLevel: String? = "MO") {
        Log.d(TAG, "Requesting doctor with preferred level: $preferredLevel")
        val callRequest = JSONObject().apply {
            put("type", "call-request")
            if (preferredLevel != null) {
                put("preferredLevel", preferredLevel) // CHO, MO, or CIVIL
            }
        }
        webSocket?.send(callRequest.toString())
    }

    fun initiateCall() {
        Log.d(TAG, "Initiating call from $patientId to $doctorId")
        createPeerConnection()
        createOffer()
    }

    private fun createPeerConnection() {
        Log.d(TAG, "Creating peer connection")
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        
        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG, "New ICE candidate: ${candidate.sdp}")
                    sendIceCandidate(candidate)
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                    Log.d(TAG, "ICE candidates removed: ${candidates?.size ?: 0}")
                }

                override fun onAddStream(stream: MediaStream) {
                    Log.d(TAG, "Remote stream added with ${stream.videoTracks.size} video tracks")
                    stream.videoTracks.firstOrNull()?.addSink(remoteVideoView)
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "ICE connection state changed: $state")
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED -> {
                            Log.d(TAG, "Call connected successfully")
                            onCallConnected?.invoke()
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED,
                        PeerConnection.IceConnectionState.FAILED,
                        PeerConnection.IceConnectionState.CLOSED -> {
                            Log.d(TAG, "Call ended with state: $state")
                            onCallEnded?.invoke()
                        }
                        else -> {}
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {
                    Log.d(TAG, "ICE connection receiving change: $receiving")
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Log.d(TAG, "Signaling state changed: $state")
                }
                
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                    Log.d(TAG, "ICE gathering state changed: $state")
                }
                
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(dataChannel: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
            }
        )

        setupLocalMedia()
    }

    private fun setupLocalMedia() {
        Log.d(TAG, "Setting up local media")
        videoCapturer = createVideoCapturer()
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer!!.isScreencast)
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer!!.initialize(
            surfaceTextureHelper,
            context,
            videoSource!!.capturerObserver
        )
        videoCapturer!!.startCapture(720, 480, 30)

        localVideoTrack = peerConnectionFactory.createVideoTrack("local_video", videoSource)
        localVideoTrack?.addSink(localVideoView)
        Log.d(TAG, "Local video track created and attached")

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }
        val audioSource = peerConnectionFactory.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio", audioSource)
        Log.d(TAG, "Local audio track created")

        // Add tracks to peer connection
        val streamId = "local_stream"
        localVideoTrack?.let { 
            peerConnection?.addTrack(it, listOf(streamId))
            Log.d(TAG, "Video track added to peer connection")
        }
        localAudioTrack?.let { 
            peerConnection?.addTrack(it, listOf(streamId))
            Log.d(TAG, "Audio track added to peer connection")
        }
    }

    private fun createVideoCapturer(): VideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        // Try front camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }

        // Fallback to back camera
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }

        throw RuntimeException("No camera found")
    }

    private fun createOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        sendOffer(sdp)
                    }
                    override fun onSetFailure(error: String) {
                        Log.e(TAG, "Set local description failed: $error")
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Create offer failed: $error")
                onError?.invoke("Failed to create offer: $error")
            }

            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, constraints)
    }

    private fun sendOffer(sdp: SessionDescription) {
        val message = JSONObject().apply {
            put("type", "offer")
            put("toUserID", doctorId)
            put("payload", JSONObject().apply {
                put("type", sdp.type.canonicalForm())
                put("sdp", sdp.description)
            })
        }
        webSocket?.send(message.toString())
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val message = JSONObject().apply {
            put("type", "ice")
            put("toUserID", doctorId)
            put("payload", JSONObject().apply {
                put("candidate", candidate.sdp)
                put("sdpMid", candidate.sdpMid)
                put("sdpMLineIndex", candidate.sdpMLineIndex)
            })
        }
        webSocket?.send(message.toString())
    }

    private fun handleSignalingMessage(message: String) {
        Log.d(TAG, "Received signaling message: $message")
        try {
            val json = JSONObject(message)
            val type = json.getString("type")

            when (type) {
                "socket-id" -> {
                    val socketID = json.getString("socketID")
                    Log.d(TAG, "Received socket ID: $socketID")
                    registerAsPatient()
                }
                "registered" -> {
                    val id = json.getString("id")
                    patientSocketId = id
                    Log.d(TAG, "Successfully registered with ID: $id")
                    // Automatically request a doctor after successful registration
                    findDoctor(preferredDoctorLevel)
                }
                "doctor-assigned" -> {
                    val assignedDoctorId = json.getString("doctorID")
                    val doctorLevel = json.optString("doctorLevel", "")
                    Log.d(TAG, "Doctor assigned: $assignedDoctorId (Level: $doctorLevel)")
                    
                    // Update the doctorId for subsequent messages
                    doctorId = assignedDoctorId
                    
                    onDoctorFound?.invoke(assignedDoctorId)
                    
                    // Automatically initiate call after doctor is assigned
                    initiateCall()
                }
                "no-doctor-available" -> {
                    Log.d(TAG, "No doctor available")
                    onError?.invoke("No doctor available at the moment")
                }
                "answer" -> {
                    val payload = json.getJSONObject("payload")
                    val sdp = SessionDescription(
                        SessionDescription.Type.ANSWER,
                        payload.getString("sdp")
                    )
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            Log.d(TAG, "Remote description set successfully")
                        }
                        override fun onSetFailure(error: String) {
                            Log.e(TAG, "Set remote description failed: $error")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, sdp)
                }
                "ice" -> {
                    val payload = json.getJSONObject("payload")
                    val candidate = IceCandidate(
                        payload.getString("sdpMid"),
                        payload.getInt("sdpMLineIndex"),
                        payload.getString("candidate")
                    )
                    peerConnection?.addIceCandidate(candidate)
                }
                "renegotiate" -> {
                    val newDoctorId = json.getString("newDoctorID")
                    Log.d(TAG, "Call handed over to new doctor: $newDoctorId")
                    onError?.invoke("Call transferred to another doctor")
                }
                "call-status" -> {
                    val status = json.getString("status")
                    when (status) {
                        "rejected", "ended" -> onCallEnded?.invoke()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message: ${e.message}")
        }
    }

    fun endCall() {
        Log.d(TAG, "Ending call")
        val message = JSONObject().apply {
            put("type", "call-status")
            put("toUserID", doctorId)
            put("status", "ended")
        }
        webSocket?.send(message.toString())
        
        cleanup()
    }

    private fun cleanup() {
        Log.d(TAG, "Cleaning up resources")
        
        // Stop video capturer
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping video capturer: ${e.message}")
        }
        
        // Dispose tracks
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        
        // Dispose sources
        videoSource?.dispose()
        
        // Close peer connection
        peerConnection?.dispose()
        
        // Close WebSocket
        webSocket?.close(1000, "Call ended")
        
        Log.d(TAG, "Cleanup completed")
    }

    companion object {
        private const val TAG = "WebRTCManager"
    }
}