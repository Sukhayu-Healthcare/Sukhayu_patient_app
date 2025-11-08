package com.sukhayu.patient.ui.ai_symptom

object SymptomChecker {

    fun analyzeSymptoms(input: String): String {
        val normalized = input.lowercase()

        return when {
            normalized.contains("fever") && normalized.contains("cough") -> 
                "Possible causes: Common cold or mild flu. Stay hydrated and monitor temperature."
            normalized.contains("chest pain") -> 
                "Chest pain can be serious. Please consult a doctor immediately."
            normalized.contains("headache") && normalized.contains("nausea") -> 
                "Possible migraine. Avoid bright light and stay hydrated."
            normalized.contains("fatigue") && normalized.contains("loss of appetite") -> 
                "Could be due to viral infection or stress."
            else -> 
                "I couldn't determine a clear condition. Please describe more symptoms."
        }
    }
}
