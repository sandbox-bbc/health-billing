package com.linx.health.repository

import com.linx.health.domain.Patient
import jakarta.inject.Singleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository for Patient entities.
 * Thread-safe using ConcurrentHashMap.
 */
@Singleton
class PatientRepository {
    
    private val patients = ConcurrentHashMap<UUID, Patient>()
    
    fun save(patient: Patient): Patient {
        patients[patient.id] = patient
        return patient
    }
    
    fun findById(id: UUID): Patient? = patients[id]
    
    fun findAll(): List<Patient> = patients.values.toList()
    
    fun existsById(id: UUID): Boolean = patients.containsKey(id)
    
    fun deleteById(id: UUID): Boolean = patients.remove(id) != null
    
    fun count(): Int = patients.size
}
