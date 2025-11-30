package com.sukhayu.patient.ui.supervisor.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.*
import com.sukhayu.patient.ui.login.LoginActivity
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AshaProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private lateinit var tvName: TextView
    private lateinit var tvId: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton

    private lateinit var cardViewContainer: View
    private lateinit var formViewContainer: View

    private lateinit var tvAshaId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhoneNo: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvTaluka: TextView

    private lateinit var etFullName: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNo: EditText
    private lateinit var etAshaId: EditText
    private lateinit var etVillage: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etTaluka: EditText
    private lateinit var etPassword: EditText

    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_supervisor_profile)
            TokenManager.init(this)
            
            initViews()
            setFieldsEnabled(false)
            loadData()
            setupListeners()
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error in onCreate", e)
            toast("Error initializing profile: ${e.message}")
            finish()
        }
    }

    private fun initViews() {
        try {
            tvName = findViewById(R.id.tvPatientName)
            tvId = findViewById(R.id.tvPatientId)
            profileImage = findViewById(R.id.profile_image)
            btnChangeImage = findViewById(R.id.btn_change_image)

            cardViewContainer = findViewById(R.id.card_view_container)
            formViewContainer = findViewById(R.id.form_view_container)

            tvAshaId = findViewById(R.id.tv_asha_id)
            tvFullName = findViewById(R.id.tv_full_name)
            tvGender = findViewById(R.id.tv_gender)
            tvPhoneNo = findViewById(R.id.tv_phone_no)
            tvVillage = findViewById(R.id.tv_village)
            tvDistrict = findViewById(R.id.tv_district)
            tvTaluka = findViewById(R.id.tv_taluka)

            etAshaId = findViewById(R.id.et_asha_id)
            etFullName = findViewById(R.id.et_full_name)
            etPhoneNo = findViewById(R.id.et_phone_no)
            spinnerGender = findViewById(R.id.spinner_gender)
            etVillage = findViewById(R.id.et_village)
            etDistrict = findViewById(R.id.et_district)
            etTaluka = findViewById(R.id.et_taluka)
            etPassword = findViewById(R.id.et_password)

            btnEdit = findViewById(R.id.btn_edit)
            btnLogout = findViewById(R.id.btn_logout)

            etPhoneNo.inputType = InputType.TYPE_CLASS_PHONE

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Male", "Female", "Other")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
            spinnerGender.setSelection(1) // Female
            spinnerGender.isEnabled = false
            
            Log.d("AshaProfile", "All views initialized successfully")
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error initializing views", e)
            throw e
        }
    }

    private fun loadData() {
        val token = TokenManager.getToken()
        
        Log.d("AshaProfile", "Loading profile with token: ${token.take(20)}...")
        
        if (token.isEmpty()) {
            toast("Missing authentication token")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.retrofit.getSupervisorProfile("Bearer $token")
            .enqueue(object : Callback<SupervisorProfile> {
                override fun onResponse(
                    call: Call<SupervisorProfile>,
                    response: Response<SupervisorProfile>
                ) {
                    Log.d("AshaProfile", "Response code: ${response.code()}")
                    
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Log.d("AshaProfile", "Profile loaded: $body")
                        displayProfileData(body)
                        toast("Profile loaded successfully")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AshaProfile", "Failed to load profile: $errorBody")
                        toast("Failed to load profile: ${response.code()}")
                        
                        if (response.code() == 401) {
                            TokenManager.clearToken()
                            startActivity(Intent(this@AshaProfileActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<SupervisorProfile>, t: Throwable) {
                    Log.e("AshaProfile", "Network error", t)
                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun displayProfileData(profile: SupervisorProfile) {
        try {
            Log.d("AshaProfile", "Displaying profile data")
            
            tvName.text = profile.user_name ?: "N/A"
            tvId.text = "Supervisor ID: ${profile.asha_id ?: "--"}"

            tvAshaId.text = profile.asha_id ?: "N/A"
            tvFullName.text = profile.user_name ?: "N/A"
            tvGender.text = "Female"
            tvPhoneNo.text = profile.phone ?: "N/A"
            tvVillage.text = profile.village ?: "N/A"
            tvDistrict.text = profile.district ?: "N/A"
            tvTaluka.text = profile.taluka ?: "N/A"

            etAshaId.setText(profile.asha_id ?: "")
            etFullName.setText(profile.user_name ?: "")
            etPhoneNo.setText(profile.phone ?: "")
            etVillage.setText(profile.village ?: "")
            etDistrict.setText(profile.district ?: "")
            etTaluka.setText(profile.taluka ?: "")
            etPassword.setText("")

            profileImage.setImageResource(R.drawable.sample_patient)
            
            Log.d("AshaProfile", "Profile data displayed successfully")
        } catch (e: Exception) {
            Log.e("AshaProfile", "Error displaying profile", e)
            toast("Error displaying profile: ${e.message}")
        }
    }

    private fun setupListeners() {
        btnChangeImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnEdit.setOnClickListener {
            if (etPhoneNo.isEnabled) {
                saveProfileChanges()
            } else {
                cardViewContainer.visibility = View.GONE
                formViewContainer.visibility = View.VISIBLE
                setFieldsEnabled(true)
                btnEdit.text = "Save"
            }
        }

        btnLogout.setOnClickListener {
            TokenManager.clearToken()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveProfileChanges() {
        val token = TokenManager.getToken()
        if (token.isEmpty()) {
            toast("Token missing")
            return
        }

        btnEdit.isEnabled = false
        btnEdit.text = "Saving..."

        val newPhone = etPhoneNo.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()

        if (newPhone.isEmpty()) {
            btnEdit.isEnabled = true
            btnEdit.text = "Save"
            toast("Phone number is required")
            return
        }

        val updateRequest = SelfUpdateRequest(
            asha_phone = newPhone,
            asha_password = if (newPassword.isNotEmpty()) newPassword else null,
            asha_profile_pic = "dummy_profile_pic"
        )

        Log.d("AshaProfile", "Updating profile: $updateRequest")

        ApiClient.retrofit.updateSupervisorProfile("Bearer $token", updateRequest)
            .enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(
                    call: Call<UpdateProfileResponse>,
                    response: Response<UpdateProfileResponse>
                ) {
                    btnEdit.isEnabled = true
                    
                    if (response.isSuccessful && response.body() != null) {
                        toast("Profile updated successfully")
                        val updated = response.body()!!.profile
                        displayProfileData(updated)

                        formViewContainer.visibility = View.GONE
                        cardViewContainer.visibility = View.VISIBLE
                        setFieldsEnabled(false)
                        btnEdit.text = "Edit"
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AshaProfile", "Update failed: $errorBody")
                        toast("Update failed: ${response.code()}")
                        btnEdit.text = "Save"
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    btnEdit.isEnabled = true
                    btnEdit.text = "Save"
                    Log.e("AshaProfile", "Network error", t)
                    toast("Network error: ${t.message}")
                }
            })
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etPhoneNo.isEnabled = enabled
        etPassword.isEnabled = enabled
        btnChangeImage.isEnabled = enabled

        etFullName.isEnabled = false
        etVillage.isEnabled = false
        etDistrict.isEnabled = false
        etTaluka.isEnabled = false
        etAshaId.isEnabled = false
        spinnerGender.isEnabled = false
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
