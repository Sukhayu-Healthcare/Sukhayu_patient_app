package com.sukhayu.patient.ui.consultation

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.repository.ConsultationRepository
import com.sukhayu.patient.utils.NetworkUtils
import com.sukhayu.patient.utils.formatDate
import kotlinx.coroutines.launch
import com.sukhayu.patient.utils.formatDate


class PastConsultationsActivity : AppCompatActivity() {

    private lateinit var repository: ConsultationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_consultations)

        val ll = findViewById<LinearLayout>(R.id.llPast)

        val db = AshaLocalDatabase.getInstance(this)
        repository = ConsultationRepository(db)

        lifecycleScope.launch {

            val hasNetwork = NetworkUtils.isNetworkAvailable(this@PastConsultationsActivity)

            val consultations = repository.getLatestConsultations(hasNetwork)

            consultations.forEach { c ->
                val card = layoutInflater.inflate(R.layout.item_consultation_card, ll, false)

                card.findViewById<TextView>(R.id.tvDoctor).text = "Doctor: ${c.doctor_id}"
                card.findViewById<TextView>(R.id.tvDate).text = formatDate(c.consultation_date)
                card.findViewById<TextView>(R.id.tvNotes).text = c.notes ?: "No Notes"

                ll.addView(card)
            }
        }
    }
}
