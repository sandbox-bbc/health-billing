package com.linx.health.domain

import java.math.BigDecimal
import java.util.UUID

/**
 * Bill generated for a completed appointment.
 * Immutable once created.
 * 
 * Calculation:
 * 1. baseFee = from fee table (specialty + experience)
 * 2. discountPercent = min(priorCompletedVisits, 10)
 * 3. discountAmount = baseFee × discountPercent%
 * 4. discountedAmount = baseFee - discountAmount
 * 5. gstAmount = discountedAmount × 12%
 * 6. totalAmount = discountedAmount + gstAmount
 * 7. insuranceAmount = totalAmount × 90%
 * 8. coPayAmount = totalAmount × 10%
 * 
 * Note: No @Serdeable - domain models are not serialized directly.
 * Use DTOs for API request/response.
 * 
 * @property id Unique identifier (UUID)
 * @property appointmentId Reference to the billed appointment
 * @property baseFee Fee from specialty/experience table
 * @property discountPercent Loyalty discount percentage applied
 * @property discountAmount Amount discounted from base fee
 * @property gstAmount GST (12%) on discounted amount
 * @property totalAmount Final total (discounted + GST)
 * @property insuranceAmount Amount covered by insurance (90%)
 * @property coPayAmount Amount to be paid by patient (10%)
 */
data class Bill(
    val id: UUID = UUID.randomUUID(),
    val appointmentId: UUID,
    val baseFee: BigDecimal,
    val discountPercent: Int,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val insuranceAmount: BigDecimal,
    val coPayAmount: BigDecimal
)
