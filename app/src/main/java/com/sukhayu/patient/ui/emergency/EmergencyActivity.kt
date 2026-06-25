package com.sukhayu.patient.ui.emergency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.ui.asha.emergency.EmergencyContactAdapter
import com.sukhayu.patient.ui.asha.emergency.EmergencyContact
import com.sukhayu.patient.utils.HeaderUtils
import com.sukhayu.patient.utils.LocalizableActivity
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper

class EmergencyActivity : LocalizableActivity() {

    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)
        setupLanguageToggle()
        HeaderUtils.setupRoleInHeader(this)

        val contacts = mutableListOf(
            EmergencyContact(
                name = "Emergency Helpline",
                number = "112",
                description = "National Emergency Number"
            ),
            EmergencyContact(
                name = "Ambulance",
                number = "108",
                description = "Ambulance Service"
            ),
            EmergencyContact(
                name = "Police",
                number = "100",
                description = "Police Control Room"
            ),
            EmergencyContact(
                name = "Fire Brigade",
                number = "101",
                description = "Fire Emergency"
            ),
            EmergencyContact(
                name = "Dad",
                number = "9876543210",
                description = "Father (Speed Dial)"
            ),
            EmergencyContact(
                name = "Mom",
                number = "9123456780",
                description = "Mother (Speed Dial)"
            ),
            EmergencyContact(
                name = "Family Doctor",
                number = "9001234567",
                description = "Family Physician"
            )
            // Add more as needed
        )

        // Optionally, load family contacts from preferences or backend and add to contacts list

        val recyclerView = findViewById<RecyclerView>(R.id.rvEmergencyContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EmergencyContactAdapter(contacts) { number ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
        }

        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )
    }
}
