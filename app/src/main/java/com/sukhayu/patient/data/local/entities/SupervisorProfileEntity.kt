package com.sukhayu.patient.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sukhayu.patient.data.remote.SupervisorProfile

@Entity(tableName = "supervisor_profile")
data class SupervisorProfileEntity(
    @PrimaryKey
    val asha_id: String,
    val user_id: String?,
    val user_name: String?,
    val user_role: String?,
    val phone: String?,
    val date_of_birth: String?,
    val village: String?,
    val district: String?,
    val taluka: String?,
    val user_created_at: String?,
    val profile_pic: String?,
    val supervisor_id: String?,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toSupervisorProfile() = SupervisorProfile(
        user_id = user_id,
        asha_id = asha_id,
        user_name = user_name,
        user_role = user_role,
        phone = phone,
        date_of_birth = date_of_birth,
        village = village,
        district = district,
        taluka = taluka,
        user_created_at = user_created_at,
        profile_pic = profile_pic,
        supervisor_id = supervisor_id
    )

    companion object {
        fun fromSupervisorProfile(profile: SupervisorProfile) = SupervisorProfileEntity(
            asha_id = profile.asha_id ?: "",
            user_id = profile.user_id,
            user_name = profile.user_name,
            user_role = profile.user_role,
            phone = profile.phone,
            date_of_birth = profile.date_of_birth,
            village = profile.village,
            district = profile.district,
            taluka = profile.taluka,
            user_created_at = profile.user_created_at,
            profile_pic = profile.profile_pic,
            supervisor_id = profile.supervisor_id
        )
    }
}
