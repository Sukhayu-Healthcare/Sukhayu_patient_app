package com.sukhayu.patient.ui.asha.emergency

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class EmergencyContactsActivity : AppCompatActivity() {

    private lateinit var rvEmergencyContacts: RecyclerView
    private lateinit var adapter: EmergencyContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        // Set up toolbar
        supportActionBar?.apply {
            title = "Emergency Contacts"
            setDisplayHomeAsUpEnabled(true)
        }

        rvEmergencyContacts = findViewById(R.id.rvEmergencyContacts)

        // Get supervisor contact if available
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val supervisorPhone = prefs.getString("supervisor_phone", null)

        // Create list of emergency contacts
        val contacts = mutableListOf(
            EmergencyContact("Ambulance", "108", "Emergency medical services"),
            EmergencyContact("Police", "100", "Police emergency helpline"),
            EmergencyContact("Fire Brigade", "101", "Fire emergency services"),
            EmergencyContact("Women Helpline", "1091", "24x7 women in distress helpline"),
            EmergencyContact("Child Helpline", "1098", "Child protection services")
        )

        // Add supervisor contact if available
        if (!supervisorPhone.isNullOrEmpty()) {
            contacts.add(
                EmergencyContact(
                    "Supervisor Contact",
                    supervisorPhone,
                    "Your assigned supervisor"
                )
            )
        }

        adapter = EmergencyContactAdapter(contacts) { phoneNumber ->
            dialNumber(phoneNumber)
        }

        rvEmergencyContacts.layoutManager = LinearLayoutManager(this)
        rvEmergencyContacts.adapter = adapter
    }

    private fun dialNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class EmergencyContact(
    val name: String,
    val number: String,
    val description: String
)

