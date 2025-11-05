package com.sukhayu.patient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ConsultDoctorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consult_doctor)

        val rvDoctors = findViewById<RecyclerView>(R.id.rvDoctors)
        rvDoctors.layoutManager = LinearLayoutManager(this)

        val doctors = DummyData.getDoctors()
        val adapter = DoctorAdapter(doctors) { doctor ->
            val intent = Intent(this, DoctorDetailActivity::class.java)
            intent.putExtra("doctor_name", doctor.name)
            intent.putExtra("doctor_specialty", doctor.specialty)
            intent.putExtra("doctor_rating", doctor.rating)
            startActivity(intent)
        }

        rvDoctors.adapter = adapter
    }
}
