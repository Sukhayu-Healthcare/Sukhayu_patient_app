package com.sukhayu.patient.ui.teleconsult

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.databinding.ActivityVideoCallBinding
import com.sukhayu.utils.VoiceInputHelper

// import androidx.activity.viewModels
// import com.sukhayu.patient.viewmodel.TeleconsultViewModel

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    // private val viewModel: TeleconsultViewModel by viewModels()  // Commented
    private lateinit var voiceHelper: VoiceInputHelper

    private val TAG = "VideoCallActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // viewModel.startVideoSession()

        binding.btnEndCall.setOnClickListener {
            // viewModel.endSession()
            finish()
        }

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // If this activity was explicitly started for a video call, try to start the WebRTC flow.
        // If starting the WebRTC components throws or fails synchronously, fall back to voice.
        val preferredMode = intent.getStringExtra(EXTRA_CALL_MODE)
        if (preferredMode == CALL_MODE_VIDEO) {
            try {
                // TODO: Integrate the real WebRTC start here (e.g. viewModel.startVideoSession())
                // For now, we keep this try/catch so that any synchronous failure will fall back.
                Log.d(TAG, "Starting video session (preferred)")
                startWebRtcSession()
            } catch (t: Throwable) {
                Log.e(TAG, "WebRTC start failed synchronously, falling back to voice", t)
                startVoiceCallAndFinish()
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
        // viewModel.endSession()
    }

    // Placeholder for the real WebRTC startup. Keep minimal to avoid breaking the app.
    // Replace the body with your actual WebRTC initiation (offer creation / signaling).
    private fun startWebRtcSession() {
        // This method intentionally keeps behavior minimal. If your existing WebRTC code
        // throws an exception at startup (for example "offer not sending"), that exception
        // will be caught in onCreate and we will fall back to a voice call.
        Log.d(TAG, "startWebRtcSession() called - integrate real WebRTC startup here")

        // Example: if you have a ViewModel that exposes startVideoSession(), call it here.
        // If that call is asynchronous and may fail later, consider adding a timeout/heartbeat
        // in your real WebRTC code to call `onWebRtcStarted()` or `onWebRtcFailed()`.
    }

    private fun startVoiceCallAndFinish() {
        try {
            val intent = Intent(this, VoiceCallActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VoiceCallActivity", e)
        } finally {
            finish()
        }
    }

    companion object {
        private const val EXTRA_CALL_MODE = "extra_call_mode"
        private const val CALL_MODE_VIDEO = "video"
        private const val CALL_MODE_VOICE = "voice"

        /**
         * Entry point for other screens (e.g. the "send" button). Decides whether to start
         * a video call or fall back to a voice call based on simple network checks.
         * Call this from any Activity/Fragment that wants to initiate the teleconsult flow.
         */
        fun initiateCall(context: Context) {
            Log.d("VideoCallActivity", "initiateCall called")
            val useVideo = isNetworkGoodForVideo(context)
            val intent = if (useVideo) {
                Log.d("VideoCallActivity", "Network looks good — starting VideoCallActivity")
                Intent(context, VideoCallActivity::class.java).also {
                    it.putExtra(EXTRA_CALL_MODE, CALL_MODE_VIDEO)
                    if (context !is AppCompatActivity) {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            } else {
                Log.d("VideoCallActivity", "Network poor — starting VoiceCallActivity")
                Intent(context, VoiceCallActivity::class.java).also {
                    if (context !is AppCompatActivity) {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            }
            context.startActivity(intent)
        }

        /**
         * Basic network heuristic: returns true if we have an active network transport (WIFI or CELLULAR).
         * You can improve this to check bandwidth / metered status / signal strength if needed.
         */
        private fun isNetworkGoodForVideo(context: Context): Boolean {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val nc = cm.activeNetwork ?: return false
                val caps = cm.getNetworkCapabilities(nc) ?: return false
                // Consider WIFI and CELLULAR as usable for video; you can tighten this later.
                return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } catch (e: Exception) {
                Log.w("VideoCallActivity", "isNetworkGoodForVideo check failed, defaulting to false", e)
                return false
            }
        }
    }
}
