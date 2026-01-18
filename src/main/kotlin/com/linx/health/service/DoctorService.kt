package com.linx.health.service

import com.linx.health.dto.CreateDoctorRequest
import com.linx.health.dto.DoctorResponse
import com.linx.health.dto.toDomain
import com.linx.health.dto.toResponse
import com.linx.health.exception.ConflictException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.DoctorRepository
import jakarta.inject.Singleton
import java.util.UUID

/**
 * Service layer for Doctor business logic.
 * Note: No update method - doctors are immutable after creation (ADR-002).
 */
@Singleton
class DoctorService(
    private val doctorRepository: DoctorRepository,
    private val appointmentRepository: AppointmentRepository
) {
    
    fun create(request: CreateDoctorRequest): DoctorResponse {
        // Check for duplicate NPI
        if (doctorRepository.existsByNpiNo(request.npiNo)) {
            throw ConflictException(
                "Doctor with NPI ${request.npiNo} already exists",
                ErrorCodes.DUPLICATE_NPI
            )
        }
        
        val doctor = request.toDomain()
        return doctorRepository.save(doctor).toResponse()
    }
    
    fun findById(id: UUID): DoctorResponse {
        return doctorRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Doctor not found with id: $id", ErrorCodes.DOCTOR_NOT_FOUND)
    }
    
    fun findAll(): List<DoctorResponse> {
        return doctorRepository.findAll().map { it.toResponse() }
    }
    
    fun delete(id: UUID) {
        if (!doctorRepository.existsById(id)) {
            throw NotFoundException("Doctor not found with id: $id", ErrorCodes.DOCTOR_NOT_FOUND)
        }
        
        // Cannot delete doctor with existing appointments
        if (appointmentRepository.existsByDoctorId(id)) {
            throw ConflictException(
                "Cannot delete doctor with existing appointments",
                ErrorCodes.DOCTOR_HAS_APPOINTMENTS
            )
        }
        
        doctorRepository.deleteById(id)
    }
}
