package com.linx.health.service

import com.linx.health.domain.AppointmentStatus
import com.linx.health.domain.Bill
import com.linx.health.dto.BillResponse
import com.linx.health.dto.toResponse
import com.linx.health.exception.BadRequestException
import com.linx.health.exception.ConflictException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.BillRepository
import com.linx.health.repository.DoctorRepository
import com.linx.health.service.billing.BillingCalculator
import jakarta.inject.Singleton
import java.util.UUID

/**
 * Service layer for billing business logic.
 * 
 * Billing Rules:
 * - Only COMPLETED appointments can be billed
 * - Each appointment can only be billed once
 * - Discount based on prior completed appointments (excluding current)
 * 
 * Uses BillingCalculator (Strategy Pattern) for fee calculation.
 */
@Singleton
class BillingService(
    private val billRepository: BillRepository,
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val billingCalculator: BillingCalculator
) {
    
    /**
     * Generate bill for a completed appointment.
     * 
     * Calculation Flow:
     * 1. Validate appointment exists and is COMPLETED
     * 2. Check if bill already exists for this appointment
     * 3. Get doctor's specialty and experience
     * 4. Calculate base fee from fee table
     * 5. Calculate loyalty discount
     * 6. Apply GST
     * 7. Split into insurance and co-pay
     */
    fun generateBill(appointmentId: UUID): BillResponse {
        // 1. Get and validate appointment
        val appointment = appointmentRepository.findById(appointmentId)
            ?: throw NotFoundException(
                "Appointment not found with id: $appointmentId",
                ErrorCodes.APPOINTMENT_NOT_FOUND
            )
        
        if (appointment.status != AppointmentStatus.COMPLETED) {
            throw BadRequestException(
                "Cannot bill appointment with status: ${appointment.status}. Only COMPLETED appointments can be billed.",
                ErrorCodes.APPOINTMENT_NOT_COMPLETED
            )
        }
        
        // 2. Check for duplicate bill
        if (billRepository.existsByAppointmentId(appointmentId)) {
            throw ConflictException(
                "Bill already exists for appointment: $appointmentId",
                ErrorCodes.BILL_ALREADY_EXISTS
            )
        }
        
        // 3. Get doctor details
        val doctor = doctorRepository.findById(appointment.doctorId)
            ?: throw NotFoundException(
                "Doctor not found with id: ${appointment.doctorId}",
                ErrorCodes.DOCTOR_NOT_FOUND
            )
        
        // 4. Calculate base fee (uses Strategy Pattern)
        val baseFee = billingCalculator.getBaseFee(doctor.specialty, doctor.experienceYears)
        
        // 5. Calculate discount (prior completed appointments, excluding current)
        val priorCompleted = appointmentRepository.countCompletedByPatientIdExcluding(
            appointment.patientId, 
            appointmentId
        )
        val discountPercent = billingCalculator.calculateDiscountPercent(priorCompleted)
        val discountAmount = billingCalculator.calculateDiscountAmount(baseFee, discountPercent)
        
        // 6. Calculate amounts
        val discountedAmount = baseFee.subtract(discountAmount)
        val gstAmount = billingCalculator.calculateGst(discountedAmount)
        val totalAmount = discountedAmount.add(gstAmount)
        
        // 7. Split insurance and co-pay
        val insuranceAmount = billingCalculator.calculateInsuranceAmount(totalAmount)
        val coPayAmount = billingCalculator.calculateCoPayAmount(totalAmount)
        
        // Create and save bill
        val bill = Bill(
            appointmentId = appointmentId,
            baseFee = baseFee,
            discountPercent = discountPercent,
            discountAmount = discountAmount,
            gstAmount = gstAmount,
            totalAmount = totalAmount,
            insuranceAmount = insuranceAmount,
            coPayAmount = coPayAmount
        )
        
        return billRepository.save(bill).toResponse()
    }
    
    fun findById(id: UUID): BillResponse {
        return billRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Bill not found with id: $id", ErrorCodes.BILL_NOT_FOUND)
    }
    
    fun findByAppointmentId(appointmentId: UUID): BillResponse {
        return billRepository.findByAppointmentId(appointmentId)?.toResponse()
            ?: throw NotFoundException(
                "Bill not found for appointment: $appointmentId",
                ErrorCodes.BILL_NOT_FOUND
            )
    }
    
    fun findAll(): List<BillResponse> {
        return billRepository.findAll().map { it.toResponse() }
    }
}
