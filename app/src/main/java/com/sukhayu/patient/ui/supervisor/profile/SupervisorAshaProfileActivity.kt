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
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.AshaDetailsResponse
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AshaProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    // Header Views
    private lateinit var tvName: TextView
    private lateinit var tvId: TextView
    private lateinit var profileImage: ImageView
    private lateinit var btnChangeImage: ImageButton

    // View containers
    private lateinit var cardViewContainer: LinearLayout
    private lateinit var formViewContainer: LinearLayout

    // Display TextViews
    private lateinit var tvAshaId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvPhoneNo: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvTaluka: TextView
    private lateinit var tvAadhar: TextView

    // Form Fields
    private lateinit var etFullName: EditText
    private lateinit var etAge: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etPhoneNo: EditText
    private lateinit var etAshaId: EditText
    private lateinit var etVillage: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etTaluka: EditText
    private lateinit var etAadhar: EditText

    // Buttons
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            profileImage.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supervisor_profile)

        initViews()
        setFieldsEnabled(false)
        loadData()
        setupListeners()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvPatientName)
        tvId = findViewById(R.id.tvPatientId)

        profileImage = findViewById(R.id.profile_image)
        btnChangeImage = findViewById(R.id.btn_change_image)

        cardViewContainer = findViewById(R.id.card_view_container)
        formViewContainer = findViewById(R.id.form_view_container)

        tvAshaId = findViewById(R.id.tv_asha_id)
        tvFullName = findViewById(R.id.tv_full_name)
        tvAge = findViewById(R.id.tv_age)
        tvGender = findViewById(R.id.tv_gender)
        tvPhoneNo = findViewById(R.id.tv_phone_no)
        tvVillage = findViewById(R.id.tv_village)
        tvDistrict = findViewById(R.id.tv_district)
        tvTaluka = findViewById(R.id.tv_taluka)
        tvAadhar = findViewById(R.id.tv_aadhar_number)

        etFullName = findViewById(R.id.et_full_name)
        etAge = findViewById(R.id.et_age)
        spinnerGender = findViewById(R.id.spinner_gender)
        etPhoneNo = findViewById(R.id.et_phone_no)
        etAshaId = findViewById(R.id.et_asha_id)
        etVillage = findViewById(R.id.et_village)
        etDistrict = findViewById(R.id.et_district)
        etTaluka = findViewById(R.id.et_taluka)
        etAadhar = findViewById(R.id.et_aadhar_number)

        btnEdit = findViewById(R.id.btn_edit)
        btnLogout = findViewById(R.id.btn_logout)

        etAge.inputType = InputType.TYPE_CLASS_NUMBER
        etPhoneNo.inputType = InputType.TYPE_CLASS_NUMBER
        etAadhar.inputType = InputType.TYPE_CLASS_NUMBER

        val genders = listOf("Male", "Female", "Other")
        spinnerGender.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            genders
        )
    }

    // ------------------ LOAD DATA ------------------
    private fun loadData() {
        val ashaId = intent.getStringExtra("ashaId")
        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", null)

        Log.d("AshaProfile", "=== API CALL DEBUG ===")
        Log.d("AshaProfile", "ASHA ID: $ashaId")
        Log.d("AshaProfile", "Token exists: ${token != null}")
        Log.d("AshaProfile", "Token value: $token")
        Log.d("AshaProfile", "Full URL will be: ${ApiClient.retrofit.javaClass.name}")

        if (ashaId == null || token == null) {
            toast("Missing ASHA ID or authentication token")
            return
        }

        val call = ApiClient.retrofit.getAshaDetails("Bearer $token", ashaId)
        Log.d("AshaProfile", "Request URL: ${call.request().url}")

        call.enqueue(object : Callback<AshaDetailsResponse> {

                override fun onResponse(
                    call: Call<AshaDetailsResponse>,
                    response: Response<AshaDetailsResponse>
                ) {
                    Log.d("AshaProfile", "=== RESPONSE DEBUG ===")
                    Log.d("AshaProfile", "Response code: ${response.code()}")
                    Log.d("AshaProfile", "Response message: ${response.message()}")
                    Log.d("AshaProfile", "Response body: ${response.body()}")
                    
                    val errorBody = response.errorBody()?.string()
                    Log.d("AshaProfile", "Error body: $errorBody")

                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        displayAshaData(body)
                        toast("Profile loaded successfully")
                    } else {
                        toast("Failed: ${response.code()} - $errorBody")
                    }
                }

                override fun onFailure(call: Call<AshaDetailsResponse>, t: Throwable) {
                    Log.e("AshaProfile", "=== API FAILURE ===", t)
                    toast("Error: ${t.message}")
                }
            })
    }

    // ------------------ DISPLAY IN UI ------------------
    private fun displayAshaData(a: AshaDetailsResponse) {
        tvName.text = a.user_name
        tvId.text = "ASHA ID: ${a.asha_id}"

        tvAshaId.text = a.asha_id
        tvFullName.text = a.user_name
        tvPhoneNo.text = a.phone
        tvVillage.text = a.village
        tvDistrict.text = a.district
        tvTaluka.text = a.taluka

        tvAge.text = "--"
        tvGender.text = "--"
        tvAadhar.text = "--"

        etAshaId.setText(a.asha_id)
        etFullName.setText(a.user_name)
        etPhoneNo.setText(a.phone)
        etVillage.setText(a.village)
        etDistrict.setText(a.district)
        etTaluka.setText(a.taluka)

        // -------- NO GLIDE, NO NETWORK IMAGE LOADING --------
        when {
            a.profile_pic == null ->
                profileImage.setImageResource(R.drawable.sample_patient)

            a.profile_pic.startsWith("content://", true) ||
                    a.profile_pic.startsWith("file://", true) ->
                profileImage.setImageURI(Uri.parse(a.profile_pic))

            else ->
                profileImage.setImageResource(R.drawable.sample_patient)
        }
    }

    // ------------------ BUTTON LISTENERS ------------------
    private fun setupListeners() {
        btnChangeImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnEdit.setOnClickListener {
            if (etFullName.isEnabled) {
                updateCardViewData()
                cardViewContainer.visibility = View.VISIBLE
                formViewContainer.visibility = View.GONE
                setFieldsEnabled(false)
                btnEdit.text = "Edit"
            } else {
                cardViewContainer.visibility = View.GONE
                formViewContainer.visibility = View.VISIBLE
                setFieldsEnabled(true)
                btnEdit.text = "Save"
            }
        }

        btnLogout.setOnClickListener {
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateCardViewData() {
        tvFullName.text = etFullName.text
        tvAge.text = etAge.text
        tvGender.text = spinnerGender.selectedItem.toString()
        tvPhoneNo.text = etPhoneNo.text
        tvVillage.text = etVillage.text
        tvDistrict.text = etDistrict.text
        tvTaluka.text = etTaluka.text
        tvAadhar.text = etAadhar.text
    }

    private fun setFieldsEnabled(b: Boolean) {
        etFullName.isEnabled = b
        etAge.isEnabled = b
        spinnerGender.isEnabled = b
        etPhoneNo.isEnabled = b
        etVillage.isEnabled = b
        etDistrict.isEnabled = b
        etTaluka.isEnabled = b
        etAadhar.isEnabled = b
        etAshaId.isEnabled = false
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
