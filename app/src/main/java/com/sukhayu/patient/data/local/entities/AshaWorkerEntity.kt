package com.sukhayu.patient.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.AshaWorker

@Entity(tableName = "asha_workers")
data class AshaWorkerEntity(
    @PrimaryKey
    val asha_id: String,
    val asha_name: String,
    val asha_phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profile_pic: String?,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toAshaWorker() = AshaWorker(
        asha_id = asha_id,
        asha_name = asha_name,
        asha_phone = asha_phone,
        village = village,
        district = district,
        taluka = taluka,
        profile_pic = profile_pic
    )

    companion object {
        fun fromAshaWorker(asha: AshaWorker) = AshaWorkerEntity(
            asha_id = asha.asha_id,
            asha_name = asha.asha_name,
            asha_phone = asha.asha_phone,
            village = asha.village,
            district = asha.district,
            taluka = asha.taluka,
            profile_pic = asha.profile_pic
        )
    }
}
