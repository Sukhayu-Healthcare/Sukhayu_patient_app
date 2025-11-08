// package com.sukhayu.patient.viewmodel

// import androidx.lifecycle.*
// import com.sukhayu.patient.data.repository.AwarenessRepository
// import com.sukhayu.patient.model.AwarenessItem
// import kotlinx.coroutines.launch

// class AwarenessViewModel(private val repository: AwarenessRepository) : ViewModel() {

//     private val _items = MutableLiveData<List<AwarenessItem>>()
//     val items: LiveData<List<AwarenessItem>> = _items

//     fun fetchAwarenessData() {
//         viewModelScope.launch {
//             val data = repository.getAwarenessUpdates()
//             _items.value = data
//         }
//     }
// }
