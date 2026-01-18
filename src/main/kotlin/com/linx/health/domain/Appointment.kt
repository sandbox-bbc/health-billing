package com.linx.health.domain

import java.time.LocalDate
import java.util.UUID

/**
 * Appointment linking a patient to a doctor.
 * 
 * Lifecycle: SCHEDULED → COMPLETED/CANCELLED
 * Only COMPLETED appointments can be billed.
 * 
 * Note: No @Serdeable - domain models are not serialized directly.
 * Use DTOs for API request/response.
 * 
 * @property id Unique identifier (UUID)
 * @property patientId Reference to Patient
 * @property doctorId Reference to Doctor
 * @property appointmentDate Date of the appointment
 * @property status Current status (SCHEDULED, COMPLETED, CANCELLED)
 */
data class Appointment(
    val id: UUID = UUID.randomUUID(),
    val patientId: UUID,
    val doctorId: UUID,
    val appointmentDate: LocalDate,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED
)
