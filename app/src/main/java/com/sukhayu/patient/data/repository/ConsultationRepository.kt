package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.ConsultationEntity
import com.sukhayu.patient.data.local.entity.PrescriptionItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConsultationRepository(private val db: AshaLocalDatabase) {

    suspend fun saveConsultationToLocal(consultation: ConsultationEntity) =
        withContext(Dispatchers.IO) {
            db.consultationDao().insertConsultation(consultation)
        }

    suspend fun savePrescriptionItem(item: PrescriptionItemEntity) =
        withContext(Dispatchers.IO) {
            db.prescriptionDao().insertPrescription(item)
        }

    suspend fun getLatestConsultations(hasNetwork: Boolean) =
        withContext(Dispatchers.IO) {
            if (hasNetwork) db.consultationDao().getAll()
            else db.consultationDao().getLatestFive()
        }
}
