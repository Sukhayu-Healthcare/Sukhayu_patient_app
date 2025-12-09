package com.sukhayu.patient.ui.asha.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.LocaleHelper

class AshaProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AshaProfile"
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "mr") ?: "mr"
        val wrapped = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_profile)
        toolbar.title = getString(R.string.profile)

        // Set a built-in back icon so we don't depend on a custom drawable
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.navigationIcon?.setTint(Color.WHITE)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadProfileData()
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        val defaultAshaName = getString(R.string.asha_default_name)
        val ashaName = prefs.getString("user_name", defaultAshaName) ?: defaultAshaName
        val ashaId = prefs.getString("user_id", "N/A") ?: "N/A"

        val tvName = findViewById<TextView>(R.id.tv_asha_name)
        val tvId = findViewById<TextView>(R.id.tvAshaId)
        val tvVillage = findViewById<TextView>(R.id.tv_asha_village)
        val tvTaluka = findViewById<TextView>(R.id.tv_asha_taluka)
        val tvDistrict = findViewById<TextView>(R.id.tv_asha_district)

        // Default values from local prefs
        tvName.text = ashaName
        tvId.text = getString(R.string.asha_id_format, ashaId)
        tvVillage.text = getString(R.string.village_format, "-")
        tvTaluka.text = getString(R.string.taluka_format, "-")
        tvDistrict.text = getString(R.string.district_format, "-")

        val token = TokenManager.getToken()
        if (token.isNotEmpty()) {
            ApiClient.retrofit.getSupervisorProfile("Bearer $token")
                .enqueue(object :
                    retrofit2.Callback<com.sukhayu.patient.data.remote.SupervisorProfile> {

                    override fun onResponse(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        response: retrofit2.Response<com.sukhayu.patient.data.remote.SupervisorProfile>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val profile = response.body()!!

                            tvName.text = profile.user_name ?: ashaName

                            val finalAshaId = profile.asha_id ?: ashaId
                            tvId.text = getString(R.string.asha_id_format, finalAshaId)

                            tvVillage.text = getString(
                                R.string.village_format,
                                profile.village ?: "-"
                            )
                            tvTaluka.text = getString(
                                R.string.taluka_format,
                                profile.taluka ?: "-"
                            )
                            tvDistrict.text = getString(
                                R.string.district_format,
                                profile.district ?: "-"
                            )
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.sukhayu.patient.data.remote.SupervisorProfile>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "Failed to load profile: ${t.message}")
                    }
                })
        }
    }
}
