package com.sukhayu.patient.ui.asha.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.asha.schedule.AshaScheduleActivity

class AshaDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_dashboard)

        // TODO: if you had other dashboard setup code earlier
        // (like setting header name, counts, etc.), put it here.

        setupMyScheduleCard()
    }

    /**
     * Make the "Completed Today" dashboard card behave as "My Schedule"
     * and open the AshaScheduleActivity when tapped.
     */
    private fun setupMyScheduleCard() {
        // Change this ID if your card has a different one in XML
        val myScheduleCard: CardView? = findViewById(R.id.cardCompletedToday)

        myScheduleCard?.setOnClickListener {
            startActivity(Intent(this, AshaScheduleActivity::class.java))
        }
    }
}
