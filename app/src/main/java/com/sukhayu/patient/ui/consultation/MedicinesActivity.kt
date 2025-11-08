package com.sukhayu.patient

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MedicinesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicines)

        val ll = findViewById<LinearLayout>(R.id.llMedicines)
        val meds = DummyData.getMedicines()
        for (m in meds) {
            val tv = TextView(this)
            tv.text = "${m.name} — ${m.uses}"
            tv.setPadding(0, 8, 0, 8)
            ll.addView(tv)
        }
    }
}
