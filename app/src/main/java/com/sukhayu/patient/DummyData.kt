package com.sukhayu.patient

object DummyData {
    fun getDoctors(): List<Doctor> {
        return listOf(
            Doctor("Asha Kulkarni", "General Physician", 4.7),
            Doctor("Rohit Sharma", "Pediatrician", 4.5),
            Doctor("Neha Patil", "Dermatologist", 4.6)
        )
    }

    fun getMedicines(): List<Medicine> {
        return listOf(
            Medicine("Paracetamol", "Pain relief and fever"),
            Medicine("Cetirizine", "Allergy relief"),
            Medicine("Omeprazole", "Acid reflux/heartburn")
        )
    }

    fun getConsultations(): List<Consultation> {
        return listOf(
            Consultation("Asha Kulkarni", "2024-08-10", "Follow-up for fever"),
            Consultation("Neha Patil", "2024-05-02", "Skin rash consultation"),
            Consultation("Rohit Sharma", "2024-01-15", "Child vaccination advice")
        )
    }
}

data class Doctor(val name: String, val specialty: String, val rating: Double)
data class Medicine(val name: String, val uses: String)
data class Consultation(val doctorName: String, val date: String, val notes: String)
