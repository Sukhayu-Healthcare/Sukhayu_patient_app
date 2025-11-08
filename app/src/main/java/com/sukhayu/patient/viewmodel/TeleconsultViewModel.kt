// package com.sukhayu.patient.viewmodel

// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel

// class TeleconsultViewModel : ViewModel() {

//     private val _isMuted = MutableLiveData(false)
//     val isMuted: LiveData<Boolean> = _isMuted

//     private val _callStatus = MutableLiveData("Idle")
//     val callStatus: LiveData<String> = _callStatus

//     fun startVideoSession() {
//         _callStatus.value = "Video Session Started"
//         // TODO: Initialize WebRTC or Jitsi session here
//     }

//     fun startVoiceSession() {
//         _callStatus.value = "Voice Session Started"
//         // TODO: Initialize audio-only call here
//     }

//     fun toggleMute() {
//         _isMuted.value = !_isMuted.value!!
//     }

//     fun endSession() {
//         _callStatus.value = "Ended"
//         // TODO: Cleanup connection resources
//     }
// }
