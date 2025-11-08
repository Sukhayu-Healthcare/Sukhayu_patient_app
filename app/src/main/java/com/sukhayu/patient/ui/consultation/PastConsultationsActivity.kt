package com.sukhayu.patient

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PastConsultationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_consultations)

        val ll = findViewById<LinearLayout>(R.id.llPast)
        val list = DummyData.getConsultations()
        for (c in list) {
            val tv = TextView(this)
            tv.text = "Dr. ${c.doctorName} — ${c.date} — Notes: ${c.notes}"
            tv.setPadding(0, 8, 0, 8)
            ll.addView(tv)
        }
    }
}
