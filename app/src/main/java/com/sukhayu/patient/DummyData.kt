package com.sukhayu.patient

import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.model.Doctor
import com.sukhayu.patient.model.Medicine

/**
 * Temporary dummy data for testing when backend is unavailable.
 * TODO: Remove this file once backend is stable.
 */
object DummyData {

    /**
     * Creates a dummy patient with the given name or returns a default dummy patient.
     */
    fun getDummyPatient(name: String? = null): PatientEntity {
        return PatientEntity(
            id = "DUMMY_001",
            name = name ?: "Dummy Patient",
            phone = "+91-9876543210",
            gender = "Female",
            weightKg = 55.0,
            supremeId = "SUP_DUMMY_001",
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Returns a list of dummy patients for testing multiple patient selection.
     */
    fun getDummyPatients(): List<PatientEntity> {
        return listOf(
            PatientEntity(
                id = "DUMMY_001",
                name = "Priya Sharma",
                phone = "+91-9876543210",
                gender = "Female",
                weightKg = 55.0,
                supremeId = "SUP_001",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_002",
                name = "Sunita Devi",
                phone = "+91-9876543211",
                gender = "Female",
                weightKg = 60.0,
                supremeId = "SUP_002",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_003",
                name = "Lakshmi Patel",
                phone = "+91-9876543212",
                gender = "Female",
                weightKg = 52.0,
                supremeId = "SUP_003",
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    /**
     * Search dummy patients by name or phone.
     */
    fun searchDummyPatients(query: String): List<PatientEntity> {
        if (query.isBlank()) return emptyList()

        return getDummyPatients().filter { patient ->
            patient.name.contains(query, ignoreCase = true) ||
            patient.phone?.contains(query) == true
        }
    }

    /**
     * Returns a list of dummy doctors for testing.
     */
    fun getDummyDoctors(): List<Doctor> {
        return listOf(
            Doctor(
                id = "DOC_001",
                name = "Dr. Rajesh Kumar",
                specialty = "General Physician",
                rating = 4.5,
                experience = 10,
                availability = "Available",
                consultationFee = 500.0,
                imageUrl = null
            ),
            Doctor(
                id = "DOC_002",
                name = "Dr. Priya Patel",
                specialty = "Gynecologist",
                rating = 4.8,
                experience = 15,
                availability = "Available",
                consultationFee = 800.0,
                imageUrl = null
            ),
            Doctor(
                id = "DOC_003",
                name = "Dr. Amit Sharma",
                specialty = "Pediatrician",
                rating = 4.6,
                experience = 8,
                availability = "Busy",
                consultationFee = 600.0,
                imageUrl = null
            ),
            Doctor(
                id = "DOC_004",
                name = "Dr. Sunita Verma",
                specialty = "Cardiologist",
                rating = 4.9,
                experience = 20,
                availability = "Available",
                consultationFee = 1000.0,
                imageUrl = null
            )
        )
    }

    /**
     * Returns a list of dummy medicines for testing.
     */
    fun getMedicines(): List<Medicine> {
        return listOf(
            Medicine(name = "Paracetamol", uses = "Fever / Pain"),
            Medicine(name = "Amoxicillin", uses = "Bacterial infections"),
            Medicine(name = "Iron Supplement", uses = "Anemia / Iron deficiency"),
            Medicine(name = "ORS", uses = "Dehydration"),
            Medicine(name = "Ibuprofen", uses = "Pain / Inflammation"),
            Medicine(name = "Folic Acid", uses = "Pregnancy supplement"),
            Medicine(name = "Vitamin B12", uses = "Vitamin deficiency"),
            Medicine(name = "Cough Syrup", uses = "Cough / Cold")
        )
    }
}

