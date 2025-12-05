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
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.patient.webrtc.WebRTCManager
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer

class VideoCallActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var btnEndCall: Button

    private var webRTCManager: WebRTCManager? = null
    private var eglBase: EglBase? = null

    private lateinit var patientId: String
    private lateinit var doctorId: String
    private var doctorSocketId: String = ""

    companion object {
        const val PERMISSION_REQUEST_CODE = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)
        btnEndCall = findViewById(R.id.btnEndCall)

        patientId = intent.getStringExtra("patientId")
            ?: TokenManager.getSupremeId().ifEmpty { TokenManager.getUserId() }

        doctorId = intent.getStringExtra("doctorId") ?: ""

        checkPermissions()
        btnEndCall.setOnClickListener { endCall() }
    }

    private fun checkPermissions() {
        val need = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (need.isEmpty()) startWebRTC()
        else ActivityCompat.requestPermissions(
            this,
            need.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE &&
            results.all { it == PackageManager.PERMISSION_GRANTED }
        ) startWebRTC()
        else finish()
    }

    private fun startWebRTC() {
        eglBase = EglBase.create()
        val ctx = eglBase!!.eglBaseContext

        localVideoView.init(ctx, null)
        remoteVideoView.init(ctx, null)

        webRTCManager = WebRTCManager(
            context = this,
            localVideoView = localVideoView,
            remoteVideoView = remoteVideoView,
            patientId = patientId,
            doctorId = doctorId,
            eglBaseContext = ctx
        ).apply {

            onDoctorFound = { socketId ->
                doctorSocketId = socketId
                initiateCall(socketId)
            }

            onCallConnected = {
                runOnUiThread {
                    Toast.makeText(this@VideoCallActivity, "Call Connected", Toast.LENGTH_SHORT).show()
                }
            }

            onError = { msg ->
                runOnUiThread { Toast.makeText(this@VideoCallActivity, msg, Toast.LENGTH_SHORT).show() }
            }

            onCallEnded = {
                runOnUiThread { finish() }
            }
        }

        webRTCManager!!.initializeWebSocket()
    }

    private fun endCall() {
        webRTCManager?.endCall()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCManager?.endCall()
    }
}
