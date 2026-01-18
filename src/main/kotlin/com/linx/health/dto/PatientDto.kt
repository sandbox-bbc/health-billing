package com.linx.health.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.linx.health.domain.InsuranceInfo
import com.linx.health.domain.Patient
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

/**
 * Request DTO for creating a patient.
 */
@Serdeable
data class CreatePatientRequest(
    val firstName: String,
    val lastName: String,
    @JsonFormat(pattern = "MM/dd/yyyy")
    val dob: LocalDate,
    val insurance: InsuranceInfoDto
)

/**
 * Request DTO for updating a patient.
 */
@Serdeable
data class UpdatePatientRequest(
    val firstName: String,
    val lastName: String,
    @JsonFormat(pattern = "MM/dd/yyyy")
    val dob: LocalDate,
    val insurance: InsuranceInfoDto
)

/**
 * DTO for insurance information.
 */
@Serdeable
data class InsuranceInfoDto(
    val binNo: String,
    val pcnNo: String,
    val memberId: String
)

/**
 * Response DTO for patient data.
 * Includes calculated age.
 */
@Serdeable
data class PatientResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    @JsonFormat(pattern = "MM/dd/yyyy")
    val dob: LocalDate,
    val age: Int,
    val insurance: InsuranceInfoDto
)

// ============================================
// Extension Functions for DTO Mapping
// ============================================

/**
 * Converts Patient domain model to API response.
 */
fun Patient.toResponse() = PatientResponse(
    id = id,
    firstName = firstName,
    lastName = lastName,
    dob = dob,
    age = age,
    insurance = insurance.toDto()
)

/**
 * Converts CreatePatientRequest to domain model.
 */
fun CreatePatientRequest.toDomain() = Patient(
    firstName = firstName,
    lastName = lastName,
    dob = dob,
    insurance = insurance.toDomain()
)

/**
 * Converts UpdatePatientRequest to domain model, preserving the ID.
 */
fun UpdatePatientRequest.toDomain(existingId: UUID) = Patient(
    id = existingId,
    firstName = firstName,
    lastName = lastName,
    dob = dob,
    insurance = insurance.toDomain()
)

/**
 * Converts InsuranceInfo domain to DTO.
 */
fun InsuranceInfo.toDto() = InsuranceInfoDto(
    binNo = binNo,
    pcnNo = pcnNo,
    memberId = memberId
)

/**
 * Converts InsuranceInfoDto to domain model.
 */
fun InsuranceInfoDto.toDomain() = InsuranceInfo(
    binNo = binNo,
    pcnNo = pcnNo,
    memberId = memberId
)
