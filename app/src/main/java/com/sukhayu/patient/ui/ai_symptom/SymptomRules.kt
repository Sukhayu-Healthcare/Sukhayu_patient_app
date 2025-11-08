package com.sukhayu.patient.ui.ai_symptom

data class SymptomRule(val keywords: List<String>, val result: String)

object SymptomRules {

    private val rules = listOf(
        SymptomRule(listOf("fever", "cough", "sore throat"), "Common Cold or Flu"),
        SymptomRule(listOf("chest pain", "breathing difficulty"), "Possible Heart or Lung issue"),
        SymptomRule(listOf("nausea", "vomiting", "headache"), "Migraine or Food Poisoning"),
        SymptomRule(listOf("fatigue", "joint pain"), "Possible Viral Infection"),
        SymptomRule(listOf("rash", "itching"), "Skin Allergy or Infection")
    )

    fun inferDisease(input: String): String {
        val text = input.lowercase()
        for (rule in rules) {
            if (rule.keywords.all { text.contains(it) }) {
                return "Possible Condition: ${rule.result}"
            }
        }
        return "Condition unclear — please provide more details."
    }
}
