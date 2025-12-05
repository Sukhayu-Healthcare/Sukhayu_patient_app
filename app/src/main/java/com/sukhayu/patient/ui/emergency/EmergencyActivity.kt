package com.sukhayu.patient.ui.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityEmergencyVcBinding
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.patient.webrtc.WebRTCManager
import com.sukhayu.utils.VoiceInputHelper
import org.webrtc.RendererCommon
import org.webrtc.EglBase

class EmergencyVCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyVcBinding
    private lateinit var voiceHelper: VoiceInputHelper
    private var webRTCManager: WebRTCManager? = null
    private lateinit var patientId: String

    private val TAG = "EmergencyVCActivity"
    private var eglBase: EglBase? = null

    companion object {
        private const val PERMISSION_REQUEST = 100
        private val REQUIRED = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyVcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get patient ID
        patientId = TokenManager.getSupremeId().ifEmpty { TokenManager.getUserId() }
        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID missing. Login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Speech helper
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // Ask permissions
        if (hasPermissions()) initCall()
        else ActivityCompat.requestPermissions(this, REQUIRED, PERMISSION_REQUEST)

        binding.btnEndEmergency.setOnClickListener {
            endCall()
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initCall()
        } else {
            Toast.makeText(this, "Camera/Mic needed for emergency call", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initCall() {
        try {
            // EGL context
            eglBase = EglBase.create()
            val eglCtx = eglBase!!.eglBaseContext

            // Local view
            binding.localVideoView.init(eglCtx, null)
            binding.localVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            binding.localVideoView.setMirror(true)
            binding.localVideoView.setEnableHardwareScaler(true)

            // Remote view
            binding.remoteVideoView.init(eglCtx, null)
            binding.remoteVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            binding.remoteVideoView.setEnableHardwareScaler(true)

            // Preferred doctor level passed from EmergencyInitActivity
            val preferredLevel = intent.getStringExtra("PREFERRED_LEVEL") ?: "MO"

            // INIT MANAGER (Unified Plan)
            webRTCManager = WebRTCManager(
                context = this,
                localVideoView = binding.localVideoView,
                remoteVideoView = binding.remoteVideoView,
                patientId = patientId,
                doctorId = "", // updated when doctor is assigned
                eglBaseContext = eglCtx
            )

            webRTCManager?.apply {
                onDoctorFound = { doctorSocketId ->
                    runOnUiThread {
                        Toast.makeText(this@EmergencyVCActivity, "Doctor Assigned: $doctorSocketId", Toast.LENGTH_SHORT).show()
                        // start call
                        initiateCall(doctorSocketId)
                    }
                }
                onCallConnected = {
                    runOnUiThread {
                        Toast.makeText(this@EmergencyVCActivity, "Emergency Call Connected", Toast.LENGTH_SHORT).show()
                    }
                }
                onCallEnded = {
                    runOnUiThread {
                        Toast.makeText(this@EmergencyVCActivity, "Call Ended", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                onError = { err ->
                    runOnUiThread {
                        Toast.makeText(this@EmergencyVCActivity, err, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Connect socket & request doctor
            webRTCManager!!.initializeWebSocket(preferredLevel)
            // Server will register and then send assigned doctor; manager will call onDoctorFound

            Toast.makeText(this, "Connecting to emergency doctor…", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Init error: ${e.message}")
            Toast.makeText(this, "Could not start call", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun endCall() {
        webRTCManager?.endCall()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCManager?.endCall()
        binding.localVideoView.release()
        binding.remoteVideoView.release()
        eglBase?.release()
        voiceHelper.destroy()
    }
}
