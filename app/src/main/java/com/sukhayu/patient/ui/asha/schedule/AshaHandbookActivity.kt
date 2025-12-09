package com.sukhayu.patient.ui.asha.schedule

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sukhayu.patient.R

class AshaHandbookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_handbook)

        val tvHandbookContent = findViewById<TextView>(R.id.tvHandbookContent)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        tvHandbookContent.text = getHandbookContent()

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun getHandbookContent(): String {
        return """
            ASHA - ACCREDITED SOCIAL HEALTH ACTIVIST
            Guidelines & Responsibilities

            1. DAILY RESPONSIBILITIES
            • Conduct home visits as per the assigned line list
            • Record all health services provided in the daily diary and register
            • Follow up with high-risk mothers and newborns to ensure safe pregnancy and delivery
            • Provide counseling to pregnant women on nutrition, immunization, and health practices
            • Conduct health awareness sessions for eligible couples on family planning and reproductive health

            2. WEEKLY RESPONSIBILITIES
            • Organize and facilitate Village Health and Nutrition Days (VHND)
            • Conduct immunization follow-up visits
            • Make home visits to high-risk families
            • Update vaccination status of children in the community
            • Address health concerns raised by villagers

            3. MONTHLY RESPONSIBILITIES
            • Attend monthly ASHA meetings at the Primary Health Center (PHC)
            • Update the household register with vital statistics
            • Prepare monthly reports on health activities
            • Review and plan for the next month's activities
            • Share feedback and challenges with the health team

            4. YEARLY RESPONSIBILITIES
            • Participate in awareness campaigns (e.g., leprosy, tuberculosis)
            • Conduct health surveys in the community
            • Attend annual training and refresher sessions
            • Evaluate annual performance and set goals for the next year
            • Support national health programs and initiatives

            5. KEY FOCUS AREAS
            • Maternal Health: Prenatal care, safe delivery, postnatal care
            • Child Health: Immunization, nutrition, growth monitoring
            • Disease Prevention: TB, malaria, leprosy, water-borne diseases
            • Nutrition: Promotion of balanced diet, breastfeeding, complementary feeding
            • Sanitation & Hygiene: Safe water, sanitation, personal hygiene
            • Family Planning: Counseling and support services

            6. IMPORTANT CONTACTS
            • PHC: Contact your Primary Health Center for emergencies
            • ANM: Contact your Auxiliary Nurse Midwife for maternal and child health issues
            • District Health Office: For administrative matters and escalations

            Remember: As an ASHA, you are the bridge between the community and the healthcare system. 
            Your dedication and commitment directly impact the health and well-being of your community.
        """.trimIndent()
    }
}

