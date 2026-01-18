package com.linx.health.repository

import com.linx.health.domain.Appointment
import com.linx.health.domain.AppointmentStatus
import jakarta.inject.Singleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository for Appointment entities.
 * Thread-safe using ConcurrentHashMap.
 */
@Singleton
class AppointmentRepository {
    
    private val appointments = ConcurrentHashMap<UUID, Appointment>()
    
    fun save(appointment: Appointment): Appointment {
        appointments[appointment.id] = appointment
        return appointment
    }
    
    fun findById(id: UUID): Appointment? = appointments[id]
    
    fun findAll(): List<Appointment> = appointments.values.toList()
    
    fun findByPatientId(patientId: UUID): List<Appointment> =
        appointments.values.filter { it.patientId == patientId }
    
    fun findByDoctorId(doctorId: UUID): List<Appointment> =
        appointments.values.filter { it.doctorId == doctorId }
    
    /**
     * Count completed appointments for a patient, excluding a specific appointment.
     * Used for loyalty discount calculation.
     */
    fun countCompletedByPatientIdExcluding(patientId: UUID, excludeAppointmentId: UUID): Int =
        appointments.values.count { 
            it.patientId == patientId && 
            it.status == AppointmentStatus.COMPLETED && 
            it.id != excludeAppointmentId 
        }
    
    fun existsById(id: UUID): Boolean = appointments.containsKey(id)
    
    fun existsByPatientId(patientId: UUID): Boolean =
        appointments.values.any { it.patientId == patientId }
    
    fun existsByDoctorId(doctorId: UUID): Boolean =
        appointments.values.any { it.doctorId == doctorId }
    
    fun deleteById(id: UUID): Boolean = appointments.remove(id) != null
    
    fun count(): Int = appointments.size
}
