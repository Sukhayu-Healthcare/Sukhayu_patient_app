package com.sukhayu.patient

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CheckSymptomsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_symptoms)

        val spinnerSymptoms = findViewById<Spinner>(R.id.spinnerSymptoms)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val tvSuggestion = findViewById<TextView>(R.id.tvSuggestion)

        // List of symptoms
        val symptoms = arrayOf(
            "Select a symptom",
            "Fever",
            "Cough",
            "Headache",
            "Fatigue",
            "Stomach Pain"
        )

        // Spinner Adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, symptoms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSymptoms.adapter = adapter

        // Button click listener
        btnCheck.setOnClickListener {
            val selected = spinnerSymptoms.selectedItem.toString()
            val suggestion = when (selected) {
                "Fever" -> "You may have an infection. Stay hydrated and consult a doctor if it persists."
                "Cough" -> "Try warm fluids and rest. If severe, consult a doctor."
                "Headache" -> "Rest and stay hydrated. Persistent headaches need medical attention."
                "Fatigue" -> "Ensure enough sleep and nutrition."
                "Stomach Pain" -> "Avoid spicy food and stay hydrated."
                else -> "Please select a valid symptom."
            }
            tvSuggestion.text = suggestion
        }
    }
}
