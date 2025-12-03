package com.sukhayu.patient.ui.consultation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.webrtc.WebRTCManager
import com.sukhayu.patient.utils.TokenManager
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer

class VideoCallActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var btnEndCall: Button
    
    private var webRTCManager: WebRTCManager? = null
    private var eglBase: EglBase? = null
    private var patientId: String = ""
    private var doctorId: String = ""
    private var actualDoctorSocketId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        // Get patient ID from intent or TokenManager
        patientId = intent.getStringExtra("patientId") ?: TokenManager.getSupremeId().ifEmpty {
            TokenManager.getUserId()
        }
        doctorId = intent.getStringExtra("doctorId") ?: ""

        if (patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID not found. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (doctorId.isEmpty()) {
            Toast.makeText(this, "Invalid call parameters", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)
        btnEndCall = findViewById(R.id.btnEndCall)

        checkPermissionsAndStartCall()

        btnEndCall.setOnClickListener {
            endCall()
        }
    }

    private fun checkPermissionsAndStartCall() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            initializeCall()
        } else {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeCall()
            } else {
                Toast.makeText(this, "Permissions required for video call", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initializeCall() {
        // Create shared EglBase context
        eglBase = EglBase.create()
        val eglContext = eglBase!!.eglBaseContext

        localVideoView.init(eglContext, null)
        remoteVideoView.init(eglContext, null)

        webRTCManager = WebRTCManager(
            context = this,
            localVideoView = localVideoView,
            remoteVideoView = remoteVideoView,
            patientId = patientId,
            doctorId = doctorId,
            eglBaseContext = eglContext
        ).apply {
            onCallConnected = {
                runOnUiThread {
                    Toast.makeText(this@VideoCallActivity, "Call connected", Toast.LENGTH_SHORT).show()
                }
            }
            onCallEnded = {
                runOnUiThread {
                    finish()
                }
            }
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this@VideoCallActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
            onDoctorFound = { doctorSocketId ->
                runOnUiThread {
                    actualDoctorSocketId = doctorSocketId
                    Toast.makeText(this@VideoCallActivity, "Doctor found, connecting...", Toast.LENGTH_SHORT).show()
                    initiateCall()
                }
            }
        }

        webRTCManager?.initializeWebSocket()
        // Find doctor first, then initiate call in onDoctorFound callback
        webRTCManager?.findDoctor()
    }

    private fun endCall() {
        webRTCManager?.endCall()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        localVideoView.release()
        remoteVideoView.release()
        eglBase?.release()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
