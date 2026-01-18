package com.linx.health.domain

import java.time.LocalDate
import java.time.Period
import java.util.UUID

/**
 * Doctor entity (immutable after creation).
 * Experience years calculated from practice start date.
 * 
 * Note: No @Serdeable - domain models are not serialized directly.
 * Use DTOs for API request/response.
 * 
 * @property id Unique identifier (UUID)
 * @property firstName Doctor's first name
 * @property lastName Doctor's last name
 * @property npiNo National Provider Identifier (unique across US healthcare)
 * @property specialty Medical specialty (ORTHO, CARDIO)
 * @property practiceStartDate When the doctor started practicing
 */
data class Doctor(
    val id: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val npiNo: String,
    val specialty: Specialty,
    val practiceStartDate: LocalDate
) {
    /**
     * Calculates years of experience from practice start date.
     * Used for fee calculation based on experience brackets:
     * - 0-19 years
     * - 20-30 years
     * - 31+ years
     */
    val experienceYears: Int
        get() = Period.between(practiceStartDate, LocalDate.now()).years
}
