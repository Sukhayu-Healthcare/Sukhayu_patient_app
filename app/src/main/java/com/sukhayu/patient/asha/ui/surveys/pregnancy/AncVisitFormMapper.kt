package com.sukhayu.patient.asha.ui.surveys.pregnancy

import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.databinding.ActivityFollowUpAncVisitBinding
import com.sukhayu.patient.R

/**
 * Helper object to convert form data to AncVisitEntity
 */
object AncVisitFormMapper {

    /**
     * Build AncVisitEntity from form binding
     * @param binding ViewBinding of the follow-up ANC form
     * @param pregnancyId The pregnancy ID this visit belongs to
     * @return AncVisitEntity ready to save
     */
    fun buildEntityFromForm(
        binding: ActivityFollowUpAncVisitBinding,
        pregnancyId: String
    ): AncVisitEntity {
        return AncVisitEntity(
            pregnancyId = pregnancyId,
            visitNumber = binding.etVisitNumber.text.toString().toIntOrNull() ?: 0,
            visitDate = binding.etVisitDate.text.toString(),
            facilityType = getFacilityType(binding),
            symptomsToday = getSelectedSymptoms(binding),
            bpSystolic = if (binding.switchBpRecorded.isChecked)
                binding.etBpSystolic.text.toString().toIntOrNull() else null,
            bpDiastolic = if (binding.switchBpRecorded.isChecked)
                binding.etBpDiastolic.text.toString().toIntOrNull() else null,
            weightKg = binding.etWeightKg.text.toString().toFloatOrNull(),
            ifaTabletsGiven = binding.etIfaTablets.text.toString().toIntOrNull(),
            calciumTabletsGiven = binding.etCalciumTablets.text.toString().toIntOrNull(),
            ttDose = binding.autoTtDose.text.toString(),
            referred = binding.switchReferralMade.isChecked,
            referralReason = if (binding.switchReferralMade.isChecked)
                binding.etReferralReason.text.toString() else null,
            nextVisitDate = binding.etNextVisitDate.text.toString().ifBlank { null }
        )
    }

    /**
     * Get selected facility type from radio group
     */
    private fun getFacilityType(binding: ActivityFollowUpAncVisitBinding): String {
        return when (binding.rgFacilityType.checkedRadioButtonId) {
            R.id.rbFacilityGovt -> "GOVT"
            R.id.rbFacilityPrivate -> "PRIVATE"
            R.id.rbFacilityHome -> "HOME"
            else -> "HOME"
        }
    }

    /**
     * Get comma-separated list of selected symptoms
     */
    private fun getSelectedSymptoms(binding: ActivityFollowUpAncVisitBinding): String? {
        val symptoms = mutableListOf<String>()

        if (binding.cbSymptomBleeding.isChecked) {
            symptoms.add("BLEEDING")
        }
        if (binding.cbSymptomHeadacheBlurredVision.isChecked) {
            symptoms.add("HEADACHE_BLURRED_VISION")
        }
        if (binding.cbSymptomSwelling.isChecked) {
            symptoms.add("SWELLING")
        }
        if (binding.cbSymptomFeverChills.isChecked) {
            symptoms.add("FEVER_CHILLS")
        }
        if (binding.cbSymptomReducedMovements.isChecked) {
            symptoms.add("REDUCED_MOVEMENTS")
        }
        if (binding.cbSymptomSevereAbdominalPain.isChecked) {
            symptoms.add("ABDOMINAL_PAIN")
        }
        if (binding.cbSymptomNone.isChecked) {
            symptoms.add("NONE")
        }

        return symptoms.joinToString(",").ifBlank { null }
    }

    /**
     * Populate form fields from existing AncVisitEntity (for editing)
     */
    fun populateFormFromEntity(
        binding: ActivityFollowUpAncVisitBinding,
        entity: AncVisitEntity
    ) {
        binding.etVisitNumber.setText(entity.visitNumber.toString())
        binding.etVisitDate.setText(entity.visitDate)

        // Set facility type radio button
        when (entity.facilityType) {
            "GOVT" -> binding.rbFacilityGovt.isChecked = true
            "PRIVATE" -> binding.rbFacilityPrivate.isChecked = true
            "HOME" -> binding.rbFacilityHome.isChecked = true
        }

        // Set symptoms checkboxes
        entity.symptomsToday?.split(",")?.forEach { symptom ->
            when (symptom.trim()) {
                "BLEEDING" -> binding.cbSymptomBleeding.isChecked = true
                "HEADACHE_BLURRED_VISION" -> binding.cbSymptomHeadacheBlurredVision.isChecked = true
                "SWELLING" -> binding.cbSymptomSwelling.isChecked = true
                "FEVER_CHILLS" -> binding.cbSymptomFeverChills.isChecked = true
                "REDUCED_MOVEMENTS" -> binding.cbSymptomReducedMovements.isChecked = true
                "ABDOMINAL_PAIN" -> binding.cbSymptomSevereAbdominalPain.isChecked = true
                "NONE" -> binding.cbSymptomNone.isChecked = true
            }
        }

        // Set BP fields
        entity.bpSystolic?.let {
            binding.switchBpRecorded.isChecked = true
            binding.etBpSystolic.setText(it.toString())
        }
        entity.bpDiastolic?.let {
            binding.etBpDiastolic.setText(it.toString())
        }

        // Set weight
        entity.weightKg?.let {
            binding.etWeightKg.setText(it.toString())
        }

        // Set interventions
        entity.ifaTabletsGiven?.let {
            binding.etIfaTablets.setText(it.toString())
        }
        entity.calciumTabletsGiven?.let {
            binding.etCalciumTablets.setText(it.toString())
        }
        entity.ttDose?.let {
            binding.autoTtDose.setText(it, false)
        }

        // Set referral
        if (entity.referred) {
            binding.switchReferralMade.isChecked = true
            binding.etReferralReason.setText(entity.referralReason)
        }

        // Set next visit date
        entity.nextVisitDate?.let {
            binding.etNextVisitDate.setText(it)
        }
    }
}

