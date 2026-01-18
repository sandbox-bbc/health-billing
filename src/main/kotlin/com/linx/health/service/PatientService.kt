package com.linx.health.service

import com.linx.health.dto.CreatePatientRequest
import com.linx.health.dto.PatientResponse
import com.linx.health.dto.UpdatePatientRequest
import com.linx.health.dto.toDomain
import com.linx.health.dto.toResponse
import com.linx.health.exception.ConflictException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.PatientRepository
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class PatientService(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository
) {
    
    fun create(request: CreatePatientRequest): PatientResponse {
        val patient = request.toDomain()
        return patientRepository.save(patient).toResponse()
    }
    
    fun findById(id: UUID): PatientResponse {
        return patientRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Patient not found with id: $id", ErrorCodes.PATIENT_NOT_FOUND)
    }
    
    fun findAll(): List<PatientResponse> {
        return patientRepository.findAll().map { it.toResponse() }
    }
    
    fun update(id: UUID, request: UpdatePatientRequest): PatientResponse {
        if (!patientRepository.existsById(id)) {
            throw NotFoundException("Patient not found with id: $id", ErrorCodes.PATIENT_NOT_FOUND)
        }
        
        val updated = request.toDomain(id)
        return patientRepository.save(updated).toResponse()
    }
    
    fun delete(id: UUID) {
        if (!patientRepository.existsById(id)) {
            throw NotFoundException("Patient not found with id: $id", ErrorCodes.PATIENT_NOT_FOUND)
        }
        
        if (appointmentRepository.existsByPatientId(id)) {
            throw ConflictException(
                "Cannot delete patient with existing appointments",
                ErrorCodes.PATIENT_HAS_APPOINTMENTS
            )
        }
        
        patientRepository.deleteById(id)
    }
}
