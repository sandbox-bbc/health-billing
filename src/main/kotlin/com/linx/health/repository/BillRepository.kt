package com.linx.health.repository

import com.linx.health.domain.Bill
import jakarta.inject.Singleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory repository for Bill entities.
 * Thread-safe using ConcurrentHashMap.
 * 
 * Note: Bills are immutable once created.
 */
@Singleton
class BillRepository {
    
    private val bills = ConcurrentHashMap<UUID, Bill>()
    
    fun save(bill: Bill): Bill {
        bills[bill.id] = bill
        return bill
    }
    
    fun findById(id: UUID): Bill? = bills[id]
    
    fun findByAppointmentId(appointmentId: UUID): Bill? =
        bills.values.find { it.appointmentId == appointmentId }
    
    fun findAll(): List<Bill> = bills.values.toList()
    
    fun existsById(id: UUID): Boolean = bills.containsKey(id)
    
    fun existsByAppointmentId(appointmentId: UUID): Boolean =
        bills.values.any { it.appointmentId == appointmentId }
    
    fun count(): Int = bills.size
}
