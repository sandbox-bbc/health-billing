package com.linx.health.service

import com.linx.health.domain.AppointmentStatus
import com.linx.health.dto.AppointmentResponse
import com.linx.health.dto.CreateAppointmentRequest
import com.linx.health.dto.UpdateStatusRequest
import com.linx.health.dto.toDomain
import com.linx.health.dto.toResponse
import com.linx.health.exception.BadRequestException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.DoctorRepository
import com.linx.health.repository.PatientRepository
import jakarta.inject.Singleton
import java.util.UUID

/**
 * Service layer for Appointment business logic.
 */
@Singleton
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val doctorRepository: DoctorRepository
) {
    
    fun create(request: CreateAppointmentRequest): AppointmentResponse {
        // Validate patient exists
        if (!patientRepository.existsById(request.patientId)) {
            throw NotFoundException(
                "Patient not found with id: ${request.patientId}",
                ErrorCodes.INVALID_PATIENT
            )
        }
        
        // Validate doctor exists
        if (!doctorRepository.existsById(request.doctorId)) {
            throw NotFoundException(
                "Doctor not found with id: ${request.doctorId}",
                ErrorCodes.INVALID_DOCTOR
            )
        }
        
        val appointment = request.toDomain()
        return appointmentRepository.save(appointment).toResponse()
    }
    
    fun findById(id: UUID): AppointmentResponse {
        return appointmentRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Appointment not found with id: $id", ErrorCodes.APPOINTMENT_NOT_FOUND)
    }
    
    fun findAll(): List<AppointmentResponse> {
        return appointmentRepository.findAll().map { it.toResponse() }
    }
    
    fun findByPatientId(patientId: UUID): List<AppointmentResponse> {
        return appointmentRepository.findByPatientId(patientId).map { it.toResponse() }
    }
    
    fun findByDoctorId(doctorId: UUID): List<AppointmentResponse> {
        return appointmentRepository.findByDoctorId(doctorId).map { it.toResponse() }
    }
    
    /**
     * Update appointment status.
     * Only SCHEDULED → COMPLETED or SCHEDULED → CANCELLED allowed.
     */
    fun updateStatus(id: UUID, request: UpdateStatusRequest): AppointmentResponse {
        val appointment = appointmentRepository.findById(id)
            ?: throw NotFoundException("Appointment not found with id: $id", ErrorCodes.APPOINTMENT_NOT_FOUND)
        
        // Validate status transition
        if (appointment.status != AppointmentStatus.SCHEDULED) {
            throw BadRequestException(
                "Cannot change status of ${appointment.status} appointment",
                ErrorCodes.INVALID_STATUS_TRANSITION
            )
        }
        
        if (request.status == AppointmentStatus.SCHEDULED) {
            throw BadRequestException(
                "Appointment is already SCHEDULED",
                ErrorCodes.INVALID_STATUS_TRANSITION
            )
        }
        
        val updated = appointment.copy(status = request.status)
        return appointmentRepository.save(updated).toResponse()
    }
    
    fun delete(id: UUID) {
        if (!appointmentRepository.existsById(id)) {
            throw NotFoundException("Appointment not found with id: $id", ErrorCodes.APPOINTMENT_NOT_FOUND)
        }
        appointmentRepository.deleteById(id)
    }
}
