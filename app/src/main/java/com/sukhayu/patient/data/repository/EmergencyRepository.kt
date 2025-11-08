// package com.sukhayu.patient.data.repository

// import com.sukhayu.patient.data.local.dao.EmergencyDao
// import com.sukhayu.patient.data.local.entities.EmergencyEntity
// import com.sukhayu.patient.data.remote.SocketManager
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext

// class EmergencyRepository(private val dao: EmergencyDao) {

//     suspend fun sendEmergencySignal() = withContext(Dispatchers.IO) {
//         val entity = EmergencyEntity(
//             timestamp = System.currentTimeMillis(),
//             type = "SOS",
//             status = "Sent",
//             location = "Pune, India"
//         )
//         dao.insert(entity)
//         SocketManager.emitEvent("sos_triggered", entity)
//     }

//     suspend fun getAllEvents() = dao.getAllEvents()
// }
