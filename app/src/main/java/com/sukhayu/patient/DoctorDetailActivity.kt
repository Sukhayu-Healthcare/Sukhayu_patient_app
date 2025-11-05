package com.sukhayu.patient

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DoctorDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_detail)

        val name = intent.getStringExtra("doctor_name") ?: "Doctor"
        val specialty = intent.getStringExtra("doctor_specialty") ?: "Specialty"
        val rating = intent.getDoubleExtra("doctor_rating", 0.0)

        val tvName = findViewById<TextView>(R.id.tvDocName)
        val tvSpec = findViewById<TextView>(R.id.tvDocSpec)
        val tvRating = findViewById<TextView>(R.id.tvDocRating)
        val btnBook = findViewById<Button>(R.id.btnBook)

        tvName.text = name
        tvSpec.text = specialty
        tvRating.text = "Rating: $rating"

        btnBook.setOnClickListener {
            Toast.makeText(this, "Consultation booked with $name (dummy)", Toast.LENGTH_LONG).show()
        }
    }
}
