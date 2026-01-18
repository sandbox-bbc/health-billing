package com.linx.health.domain

import java.time.LocalDate
import java.time.Period
import java.util.UUID

/**
 * Patient entity with embedded insurance information.
 * Age is calculated from DOB, not stored.
 * 
 * Note: No @Serdeable - domain models are not serialized directly.
 * Use DTOs for API request/response.
 * 
 * @property id Unique identifier (UUID)
 * @property firstName Patient's first name
 * @property lastName Patient's last name
 * @property dob Date of birth
 * @property insurance Embedded insurance information
 */
data class Patient(
    val id: UUID = UUID.randomUUID(),
    val firstName: String,
    val lastName: String,
    val dob: LocalDate,
    val insurance: InsuranceInfo
) {
    /**
     * Calculates age from DOB.
     * Returns years between DOB and current date.
     */
    val age: Int
        get() = Period.between(dob, LocalDate.now()).years
}
