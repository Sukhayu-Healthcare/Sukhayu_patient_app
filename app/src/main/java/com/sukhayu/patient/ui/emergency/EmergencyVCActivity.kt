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
import com.sukhayu.patient.webrtc.WebRTCManager
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import org.webrtc.RendererCommon

class EmergencyVCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyVcBinding
    private lateinit var voiceHelper: VoiceInputHelper
    private var webRTCManager: WebRTCManager? = null
    private var patientId: String = ""
    private val TAG = "EmergencyVCActivity"
    private var eglBase: org.webrtc.EglBase? = null

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyVcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get patient ID from TokenManager
        patientId = TokenManager.getSupremeId().ifEmpty {
            TokenManager.getUserId()
        }

        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Using patient ID: $patientId")

        if (checkPermissions()) {
            initializeVideoCall()
        } else {
            requestPermissions()
        }

        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        binding.btnEndEmergency.setOnClickListener {
            endCall()
        }
    }

    private fun checkPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeVideoCall()
            } else {
                Toast.makeText(this, "Permissions required for video call", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initializeVideoCall() {
        try {
            // Create shared EglBase context
            eglBase = org.webrtc.EglBase.create()
            val eglContext = eglBase!!.eglBaseContext

            // Initialize video views with shared context
            binding.localVideoView.apply {
                init(eglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setEnableHardwareScaler(true)
                setMirror(true)
            }

            binding.remoteVideoView.apply {
                init(eglContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                setEnableHardwareScaler(true)
            }

            // Get preferred doctor level from intent
            val preferredLevel = intent.getStringExtra("PREFERRED_LEVEL") ?: "MO"
            
            // Initialize WebRTC Manager
            webRTCManager = WebRTCManager(
                context = this,
                localVideoView = binding.localVideoView,
                remoteVideoView = binding.remoteVideoView,
                patientId = patientId,
                doctorId = "", // Will be set when doctor is assigned
                eglBaseContext = eglContext
            )

            // Set up callbacks
            webRTCManager?.apply {
                onDoctorFound = { doctorId ->
                    runOnUiThread {
                        Log.d(TAG, "Doctor found: $doctorId")
                        Toast.makeText(this@EmergencyVCActivity, "Doctor connected", Toast.LENGTH_SHORT).show()
                    }
                }

                onCallConnected = {
                    runOnUiThread {
                        Log.d(TAG, "Call connected")
                        Toast.makeText(this@EmergencyVCActivity, "Call connected", Toast.LENGTH_SHORT).show()
                    }
                }

                onCallEnded = {
                    runOnUiThread {
                        Log.d(TAG, "Call ended")
                        Toast.makeText(this@EmergencyVCActivity, "Call ended", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                onError = { error ->
                    runOnUiThread {
                        Log.e(TAG, "Error: $error")
                        Toast.makeText(this@EmergencyVCActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Initialize WebSocket and start call process
            webRTCManager?.initializeWebSocket(preferredLevel)
            
            Toast.makeText(this, "Connecting to doctor...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing video call: ${e.message}")
            Toast.makeText(this, "Failed to initialize video call", Toast.LENGTH_SHORT).show()
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
