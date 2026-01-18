package com.linx.health.repository

import com.linx.health.domain.Doctor
import jakarta.inject.Singleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository for Doctor entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * Note: No update method - Doctors are immutable after creation.
 */
@Singleton
class DoctorRepository {
    
    private val doctors = ConcurrentHashMap<UUID, Doctor>()
    
    fun save(doctor: Doctor): Doctor {
        doctors[doctor.id] = doctor
        return doctor
    }
    
    fun findById(id: UUID): Doctor? = doctors[id]
    
    fun findByNpiNo(npiNo: String): Doctor? = 
        doctors.values.find { it.npiNo == npiNo }
    
    fun findAll(): List<Doctor> = doctors.values.toList()
    
    fun existsById(id: UUID): Boolean = doctors.containsKey(id)
    
    fun existsByNpiNo(npiNo: String): Boolean = 
        doctors.values.any { it.npiNo == npiNo }
    
    fun deleteById(id: UUID): Boolean = doctors.remove(id) != null
    
    fun count(): Int = doctors.size
}
