package com.linx.health.dto

import com.linx.health.domain.Bill
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal
import java.util.UUID

// --- Response DTOs ---

/**
 * Response DTO for bill information.
 * Shows full billing breakdown.
 */
@Serdeable
data class BillResponse(
    val id: UUID,
    val appointmentId: UUID,
    val baseFee: BigDecimal,
    val discountPercent: Int,
    val discountAmount: BigDecimal,
    val gstAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val insuranceAmount: BigDecimal,
    val coPayAmount: BigDecimal
)

// --- Extension Functions for Mapping ---

fun Bill.toResponse(): BillResponse {
    return BillResponse(
        id = this.id,
        appointmentId = this.appointmentId,
        baseFee = this.baseFee,
        discountPercent = this.discountPercent,
        discountAmount = this.discountAmount,
        gstAmount = this.gstAmount,
        totalAmount = this.totalAmount,
        insuranceAmount = this.insuranceAmount,
        coPayAmount = this.coPayAmount
    )
}
