package com.linx.health.service

import com.linx.health.domain.Specialty
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Fee calculation service.
 * Encapsulates fee table and billing calculations.
 * 
 * Fee Table:
 * | Specialty | 0-19 yrs | 20-30 yrs | 31+ yrs |
 * |-----------|----------|-----------|---------|
 * | ORTHO     | $800     | $1000     | $1500   |
 * | CARDIO    | $1000    | $1500     | $2000   |
 * 
 * Constants:
 * - GST_RATE: 12%
 * - INSURANCE_COVERAGE: 90%
 * - CO_PAY_RATE: 10%
 * - MAX_DISCOUNT: 10%
 */
@Singleton
class FeeCalculator {
    
    companion object {
        // Fee table
        private val FEE_TABLE = mapOf(
            Specialty.ORTHO to mapOf(
                ExperienceBracket.JUNIOR to BigDecimal("800"),
                ExperienceBracket.MID to BigDecimal("1000"),
                ExperienceBracket.SENIOR to BigDecimal("1500")
            ),
            Specialty.CARDIO to mapOf(
                ExperienceBracket.JUNIOR to BigDecimal("1000"),
                ExperienceBracket.MID to BigDecimal("1500"),
                ExperienceBracket.SENIOR to BigDecimal("2000")
            )
        )
        
        // Rates
        val GST_RATE: BigDecimal = BigDecimal("0.12")           // 12%
        val INSURANCE_RATE: BigDecimal = BigDecimal("0.90")     // 90%
        val CO_PAY_RATE: BigDecimal = BigDecimal("0.10")        // 10%
        const val MAX_DISCOUNT_PERCENT = 10
        
        // Scale for monetary calculations
        private const val MONEY_SCALE = 2
        private val ROUNDING_MODE = RoundingMode.HALF_UP
    }
    
    /**
     * Experience brackets for fee calculation.
     */
    enum class ExperienceBracket {
        JUNIOR,  // 0-19 years
        MID,     // 20-30 years
        SENIOR   // 31+ years
    }
    
    /**
     * Determines experience bracket from years of experience.
     */
    fun getExperienceBracket(years: Int): ExperienceBracket {
        return when {
            years <= 19 -> ExperienceBracket.JUNIOR
            years <= 30 -> ExperienceBracket.MID
            else -> ExperienceBracket.SENIOR
        }
    }
    
    /**
     * Gets base fee from fee table.
     */
    fun getBaseFee(specialty: Specialty, experienceYears: Int): BigDecimal {
        val bracket = getExperienceBracket(experienceYears)
        return FEE_TABLE[specialty]?.get(bracket) 
            ?: throw IllegalArgumentException("Unknown specialty: $specialty")
    }
    
    /**
     * Calculates loyalty discount percentage.
     * Discount = min(priorCompletedAppointments, 10)%
     */
    fun calculateDiscountPercent(priorCompletedAppointments: Int): Int {
        return minOf(priorCompletedAppointments, MAX_DISCOUNT_PERCENT)
    }
    
    /**
     * Calculates discount amount from base fee.
     */
    fun calculateDiscountAmount(baseFee: BigDecimal, discountPercent: Int): BigDecimal {
        return baseFee.multiply(BigDecimal(discountPercent))
            .divide(BigDecimal("100"), MONEY_SCALE, ROUNDING_MODE)
    }
    
    /**
     * Calculates GST on the discounted amount.
     */
    fun calculateGst(discountedAmount: BigDecimal): BigDecimal {
        return discountedAmount.multiply(GST_RATE)
            .setScale(MONEY_SCALE, ROUNDING_MODE)
    }
    
    /**
     * Calculates insurance coverage (90%).
     */
    fun calculateInsuranceAmount(totalAmount: BigDecimal): BigDecimal {
        return totalAmount.multiply(INSURANCE_RATE)
            .setScale(MONEY_SCALE, ROUNDING_MODE)
    }
    
    /**
     * Calculates co-pay amount (10%).
     */
    fun calculateCoPayAmount(totalAmount: BigDecimal): BigDecimal {
        return totalAmount.multiply(CO_PAY_RATE)
            .setScale(MONEY_SCALE, ROUNDING_MODE)
    }
}
