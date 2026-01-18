package com.linx.health.service.billing

import com.linx.health.domain.Specialty
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.math.BigDecimal

/**
 * Calculator for billing amounts.
 * Uses Strategy Pattern for specialty-based fee calculation.
 * 
 * Responsibilities:
 * - Resolve correct fee strategy for specialty
 * - Calculate discount, GST, insurance split
 * 
 * EXTENSION POINTS:
 * - New specialty: Add new FeeStrategy implementation
 * - Change rates: Modify BillingConstants
 * - Custom discount logic: Override calculateDiscountPercent
 */
@Singleton
class BillingCalculator(
    @Named("ORTHO") private val orthoStrategy: FeeStrategy,
    @Named("CARDIO") private val cardioStrategy: FeeStrategy
) {
    
    /**
     * Get the appropriate fee strategy for a specialty.
     * 
     * EXTENSION: When adding new specialty, add case here.
     * Future enhancement: Could use a Map<Specialty, FeeStrategy> for dynamic lookup.
     */
    fun getStrategy(specialty: Specialty): FeeStrategy {
        return when (specialty) {
            Specialty.ORTHO -> orthoStrategy
            Specialty.CARDIO -> cardioStrategy
        }
    }
    
    /**
     * Get base fee for a specialty and experience level.
     */
    fun getBaseFee(specialty: Specialty, experienceYears: Int): BigDecimal {
        return getStrategy(specialty).getBaseFee(experienceYears)
    }
    
    /**
     * Calculate loyalty discount percentage.
     * Discount = min(priorCompletedAppointments, MAX_DISCOUNT_PERCENT)%
     */
    fun calculateDiscountPercent(priorCompletedAppointments: Int): Int {
        return minOf(priorCompletedAppointments, BillingConstants.MAX_DISCOUNT_PERCENT)
    }
    
    /**
     * Calculate discount amount from base fee.
     */
    fun calculateDiscountAmount(baseFee: BigDecimal, discountPercent: Int): BigDecimal {
        return baseFee
            .multiply(BigDecimal(discountPercent))
            .divide(BigDecimal("100"), BillingConstants.MONEY_SCALE, BillingConstants.ROUNDING_MODE)
    }
    
    /**
     * Calculate GST on the discounted amount.
     */
    fun calculateGst(discountedAmount: BigDecimal): BigDecimal {
        return discountedAmount
            .multiply(BillingConstants.GST_RATE)
            .setScale(BillingConstants.MONEY_SCALE, BillingConstants.ROUNDING_MODE)
    }
    
    /**
     * Calculate insurance coverage (90% of total).
     */
    fun calculateInsuranceAmount(totalAmount: BigDecimal): BigDecimal {
        return totalAmount
            .multiply(BillingConstants.INSURANCE_RATE)
            .setScale(BillingConstants.MONEY_SCALE, BillingConstants.ROUNDING_MODE)
    }
    
    /**
     * Calculate co-pay amount (10% of total).
     */
    fun calculateCoPayAmount(totalAmount: BigDecimal): BigDecimal {
        return totalAmount
            .multiply(BillingConstants.CO_PAY_RATE)
            .setScale(BillingConstants.MONEY_SCALE, BillingConstants.ROUNDING_MODE)
    }
}
