package com.sukhayu.patient.ui.ai_symptom

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactsActivity
import com.sukhayu.patient.ui.consultation.ConsultDoctorActivity
import com.sukhayu.patient.utils.HeaderUtils

class MockSymptomResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SYMPTOM_TEXT = "extra_symptom_text"
    }

    private lateinit var spinnerSeverity: Spinner
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mock_symptom_result)
        HeaderUtils.setupRoleInHeader(this)

        spinnerSeverity = findViewById(R.id.spinnerSeverity)
        btnContinue = findViewById(R.id.btnContinue)

        val options = listOf("Yellow (Consult CHO)", "Orange (Consult MO)", "Red (Emergency)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSeverity.adapter = adapter

        val symptomText = intent.getStringExtra(EXTRA_SYMPTOM_TEXT)

        btnContinue.setOnClickListener {
            when (spinnerSeverity.selectedItemPosition) {
                0 -> {
                    // Yellow -> open CHO consultation flow
                    val intent = Intent(this, ConsultDoctorActivity::class.java)
                    intent.putExtra("consult_level", "CHO")
                    if (!symptomText.isNullOrEmpty()) intent.putExtra(EXTRA_SYMPTOM_TEXT, symptomText)
                    startActivity(intent)
                }
                1 -> {
                    // Orange -> open MO consultation flow
                    val intent = Intent(this, ConsultDoctorActivity::class.java)
                    intent.putExtra("consult_level", "MO")
                    if (!symptomText.isNullOrEmpty()) intent.putExtra(EXTRA_SYMPTOM_TEXT, symptomText)
                    startActivity(intent)
                }
                2 -> {
                    // Red -> open Emergency contacts screen
                    val intent = Intent(this, EmergencyContactsActivity::class.java)
                    startActivity(intent)
                }
            }
            finish()
        }
    }
}
