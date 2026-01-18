package com.linx.health.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.linx.health.common.Constants
import com.linx.health.domain.Doctor
import com.linx.health.domain.Specialty
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

// --- Request DTOs ---

/**
 * Request DTO for creating a new doctor.
 * Note: No UpdateDoctorRequest - doctors are immutable after creation.
 */
@Serdeable
data class CreateDoctorRequest(
    val firstName: String,
    val lastName: String,
    val npiNo: String,
    val specialty: Specialty,
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    val practiceStartDate: LocalDate
)

// --- Response DTOs ---

/**
 * Response DTO for doctor information.
 * Includes calculated experience years.
 */
@Serdeable
data class DoctorResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val npiNo: String,
    val specialty: Specialty,
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    val practiceStartDate: LocalDate,
    val experienceYears: Int
)

// --- Extension Functions for Mapping ---

fun CreateDoctorRequest.toDomain(): Doctor {
    return Doctor(
        firstName = this.firstName,
        lastName = this.lastName,
        npiNo = this.npiNo,
        specialty = this.specialty,
        practiceStartDate = this.practiceStartDate
    )
}

fun Doctor.toResponse(): DoctorResponse {
    return DoctorResponse(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        npiNo = this.npiNo,
        specialty = this.specialty,
        practiceStartDate = this.practiceStartDate,
        experienceYears = this.experienceYears
    )
}
