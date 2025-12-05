// package com.sukhayu.patient.ui.consultation

// import android.Manifest
// import android.content.pm.PackageManager
// import android.os.Bundle
// import android.widget.Toast
// import androidx.appcompat.app.AppCompatActivity
// import androidx.core.app.ActivityCompat
// import androidx.core.content.ContextCompat
// import com.sukhayu.patient.databinding.ActivityEmergencyVcBinding
// import com.sukhayu.patient.webrtc.WebRTCManager
// import org.webrtc.EglBase

// class EmergencyVCActivity : AppCompatActivity() {

//     private lateinit var binding: ActivityEmergencyVcBinding
//     private lateinit var webRTCManager: WebRTCManager
//     private lateinit var eglBase: EglBase
//     private var doctorSocketId: String? = null

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         binding = ActivityEmergencyVcBinding.inflate(layoutInflater)
//         setContentView(binding.root)

//         eglBase = EglBase.create()
//         checkPermissionsAndStart()
//         binding.btnEndCall.setOnClickListener { endCall() }
//     }

//     private fun checkPermissionsAndStart() {
//         val permissions = arrayOf(
//             Manifest.permission.CAMERA,
//             Manifest.permission.RECORD_AUDIO
//         )
//         val notGranted = permissions.filter {
//             ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
//         }
//         if (notGranted.isEmpty()) {
//             startWebRTC()
//         } else {
//             ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 101)
//         }
//     }

//     override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//         if (requestCode == 101 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
//             startWebRTC()
//         } else {
//             Toast.makeText(this, "Camera and audio permissions required", Toast.LENGTH_SHORT).show()
//             finish()
//         }
//     }

//     private fun startWebRTC() {
//         val patientId = intent.getStringExtra("patientId") ?: "unknown_patient"
//         val preferredLevel = intent.getStringExtra("preferredLevel") ?: "emergency"
//         webRTCManager = WebRTCManager(
//             context = this,
//             patientId = patientId,
//             localView = binding.localVideoView,
//             remoteView = binding.remoteVideoView,
//             eglBaseContext = eglBase.eglBaseContext
//         ).apply {
//             onDoctorFound = { doctorId ->
//                 doctorSocketId = doctorId
//                 Toast.makeText(this@EmergencyVCActivity, "Doctor found, connecting...", Toast.LENGTH_SHORT).show()
//                 initiateCall(doctorId)
//             }
//             onCallConnected = {
//                 Toast.makeText(this@EmergencyVCActivity, "Call connected", Toast.LENGTH_SHORT).show()
//             }
//             onCallEnded = {
//                 Toast.makeText(this@EmergencyVCActivity, "Call ended", Toast.LENGTH_SHORT).show()
//                 finish()
//             }
//             onError = { error ->
//                 Toast.makeText(this@EmergencyVCActivity, error, Toast.LENGTH_SHORT).show()
//                 finish()
//             }
//         }
//         webRTCManager.initializeWebSocket(preferredLevel)
//     }

//     private fun endCall() {
//         webRTCManager.endCall()
//         finish()
//     }

//     override fun onDestroy() {
//         super.onDestroy()
//         webRTCManager.endCall()
//         eglBase.release()
//     }
// }
