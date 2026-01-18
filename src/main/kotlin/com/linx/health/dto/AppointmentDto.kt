package com.linx.health.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.linx.health.common.Constants
import com.linx.health.domain.Appointment
import com.linx.health.domain.AppointmentStatus
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

// --- Request DTOs ---

/**
 * Request DTO for creating a new appointment.
 */
@Serdeable
data class CreateAppointmentRequest(
    val patientId: UUID,
    val doctorId: UUID,
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    val appointmentDate: LocalDate
)

/**
 * Request DTO for updating appointment status.
 * Only SCHEDULED → COMPLETED or SCHEDULED → CANCELLED allowed.
 */
@Serdeable
data class UpdateStatusRequest(
    val status: AppointmentStatus
)

// --- Response DTOs ---

/**
 * Response DTO for appointment information.
 */
@Serdeable
data class AppointmentResponse(
    val id: UUID,
    val patientId: UUID,
    val doctorId: UUID,
    @JsonFormat(pattern = Constants.DATE_FORMAT)
    val appointmentDate: LocalDate,
    val status: AppointmentStatus
)

// --- Extension Functions for Mapping ---

fun CreateAppointmentRequest.toDomain(): Appointment {
    return Appointment(
        patientId = this.patientId,
        doctorId = this.doctorId,
        appointmentDate = this.appointmentDate
    )
}

fun Appointment.toResponse(): AppointmentResponse {
    return AppointmentResponse(
        id = this.id,
        patientId = this.patientId,
        doctorId = this.doctorId,
        appointmentDate = this.appointmentDate,
        status = this.status
    )
}
