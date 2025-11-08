// package com.sukhayu.patient.viewmodel

// import androidx.lifecycle.*
// import com.sukhayu.patient.data.repository.EmergencyRepository
// import kotlinx.coroutines.launch

// class EmergencyViewModel(private val repository: EmergencyRepository) : ViewModel() {

//     private val _countdown = MutableLiveData(5)
//     val countdown: LiveData<Int> = _countdown

//     private val _sosStatus = MutableLiveData<String>()
//     val sosStatus: LiveData<String> = _sosStatus

//     fun startCountdown() {
//         viewModelScope.launch {
//             var time = 5
//             while (time >= 0) {
//                 _countdown.value = time
//                 kotlinx.coroutines.delay(1000)
//                 time--
//             }
//             sendSOS()
//         }
//     }

//     fun sendSOS() {
//         viewModelScope.launch {
//             _sosStatus.value = "Sending..."
//             repository.sendEmergencySignal()
//             _sosStatus.value = "SOS Sent"
//         }
//     }

//     fun cancelSOS() {
//         _sosStatus.value = "Cancelled"
//     }
// }
