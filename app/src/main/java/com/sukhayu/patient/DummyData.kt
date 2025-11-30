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
     *
     * SHARED DATA SOURCE for both Pregnancy/ANC and TB modules.
     * This list includes:
     * - Pregnant women (for ANC surveys)
     * - Adult men (for TB screening and treatment follow-up)
     * - Adolescents and children (for TB screening in adolescents)
     *
     * This dummy data should be seeded into the local Room database on first run
     * to maintain offline-first architecture across all modules.
     */
    fun getDummyPatients(): List<PatientEntity> {
        return listOf(
            // ===== PREGNANT WOMEN (for ANC surveys) =====
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
            ),
            PatientEntity(
                id = "DUMMY_004",
                name = "Meera Gupta",
                phone = "+91-9876543213",
                gender = "Female",
                weightKg = 58.0,
                supremeId = "SUP_004",
                lastUpdated = System.currentTimeMillis()
            ),

            // ===== ADULT MEN (for TB screening & treatment follow-up) =====
            PatientEntity(
                id = "DUMMY_M001",
                name = "Rajesh Kumar",
                phone = "+91-9876543220",
                gender = "Male",
                weightKg = 68.0,
                supremeId = "SUP_M001",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_M002",
                name = "Amit Singh",
                phone = "+91-9876543221",
                gender = "Male",
                weightKg = 72.0,
                supremeId = "SUP_M002",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_M003",
                name = "Vijay Patil",
                phone = "+91-9876543222",
                gender = "Male",
                weightKg = 65.0,
                supremeId = "SUP_M003",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_M004",
                name = "Suresh Yadav",
                phone = "+91-9876543223",
                gender = "Male",
                weightKg = 70.0,
                supremeId = "SUP_M004",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_M005",
                name = "Ramesh Verma",
                phone = "+91-9876543224",
                gender = "Male",
                weightKg = 75.0,
                supremeId = "SUP_M005",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_M006",
                name = "Mohan Reddy",
                phone = "+91-9876543225",
                gender = "Male",
                weightKg = 78.0,
                supremeId = "SUP_M006",
                lastUpdated = System.currentTimeMillis()
            ),

            // ===== ADOLESCENTS / CHILDREN (for TB screening in adolescents) =====
            PatientEntity(
                id = "DUMMY_A001",
                name = "Rohit Sharma",
                phone = "+91-9876543230",
                gender = "Male",
                weightKg = 45.0,
                supremeId = "SUP_A001",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_A002",
                name = "Anjali Desai",
                phone = "+91-9876543231",
                gender = "Female",
                weightKg = 42.0,
                supremeId = "SUP_A002",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_A003",
                name = "Karan Patel",
                phone = "+91-9876543232",
                gender = "Male",
                weightKg = 48.0,
                supremeId = "SUP_A003",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_A004",
                name = "Pooja Singh",
                phone = "+91-9876543233",
                gender = "Female",
                weightKg = 40.0,
                supremeId = "SUP_A004",
                lastUpdated = System.currentTimeMillis()
            ),
            PatientEntity(
                id = "DUMMY_A005",
                name = "Arjun Kumar",
                phone = "+91-9876543234",
                gender = "Male",
                weightKg = 38.0,
                supremeId = "SUP_A005",
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

