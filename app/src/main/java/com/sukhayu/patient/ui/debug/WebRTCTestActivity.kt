package com.sukhayu.patient.ui.debug

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.R
import com.sukhayu.patient.webrtc.WebRTCManager
import org.webrtc.SurfaceViewRenderer
import java.util.UUID

class WebRTCTestActivity : AppCompatActivity() {

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var btnTest: Button
    private lateinit var tvStatus: TextView
    
    private var webRTCManager: WebRTCManager? = null
    private val testPatientId = "patient_${UUID.randomUUID().toString().take(8)}"
    private val testDoctorId = "doctor_test"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webrtc_test)

        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)
        btnTest = findViewById(R.id.btnTest)
        tvStatus = findViewById(R.id.tvStatus)

        updateStatus("Ready to test. Patient ID: $testPatientId")

        btnTest.setOnClickListener {
            checkPermissionsAndTest()
        }
    }

    private fun checkPermissionsAndTest() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            startTest()
        } else {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startTest()
        } else {
            updateStatus("Permissions denied")
        }
    }

    private fun startTest() {
        updateStatus("Initializing WebRTC...")
        
        localVideoView.init(null, null)
        remoteVideoView.init(null, null)

        webRTCManager = WebRTCManager(
            context = this,
            localVideoView = localVideoView,
            remoteVideoView = remoteVideoView,
            patientId = testPatientId,
            doctorId = testDoctorId
        ).apply {
            onCallConnected = {
                runOnUiThread {
                    updateStatus("✓ Call connected!")
                    Toast.makeText(this@WebRTCTestActivity, "Connected", Toast.LENGTH_SHORT).show()
                }
            }
            onCallEnded = {
                runOnUiThread {
                    updateStatus("Call ended")
                }
            }
            onError = { error ->
                runOnUiThread {
                    updateStatus("✗ Error: $error")
                    Toast.makeText(this@WebRTCTestActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        updateStatus("Connecting to signaling server...")
        webRTCManager?.initializeWebSocket()
        
        // Wait a bit for websocket to connect, then initiate call
        localVideoView.postDelayed({
            updateStatus("Initiating call...")
            webRTCManager?.initiateCall()
        }, 2000)
    }

    private fun updateStatus(status: String) {
        tvStatus.text = "$status\n${tvStatus.text}"
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCManager?.endCall()
        localVideoView.release()
        remoteVideoView.release()
    }
}
