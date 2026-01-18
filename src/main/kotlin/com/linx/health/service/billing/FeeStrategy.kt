package com.linx.health.service.billing

import java.math.BigDecimal

/**
 * Strategy interface for specialty-based fee calculation.
 * 
 * EXTENSION POINT: To add a new specialty:
 * 1. Add enum value to Specialty.kt
 * 2. Create new class implementing FeeStrategy (e.g., NeuroFeeStrategy)
 * 3. Annotate with @Singleton @Named("NEURO")
 * 4. That's it! No other changes needed.
 * 
 * This follows the Open/Closed Principle:
 * - Open for extension (new specialties)
 * - Closed for modification (existing code unchanged)
 * 
 * @see OrthoFeeStrategy
 * @see CardioFeeStrategy
 */
interface FeeStrategy {
    
    /**
     * Get base fee based on doctor's experience years.
     * 
     * Experience brackets:
     * - JUNIOR: 0-19 years
     * - MID: 20-30 years
     * - SENIOR: 31+ years
     */
    fun getBaseFee(experienceYears: Int): BigDecimal
    
    /**
     * Get fee for junior doctors (0-19 years experience).
     */
    fun getJuniorFee(): BigDecimal
    
    /**
     * Get fee for mid-level doctors (20-30 years experience).
     */
    fun getMidFee(): BigDecimal
    
    /**
     * Get fee for senior doctors (31+ years experience).
     */
    fun getSeniorFee(): BigDecimal
}

/**
 * Base implementation with common experience bracket logic.
 * Subclasses only need to define the fee amounts.
 */
abstract class BaseFeeStrategy : FeeStrategy {
    
    override fun getBaseFee(experienceYears: Int): BigDecimal {
        return when {
            experienceYears <= BillingConstants.JUNIOR_MAX_YEARS -> getJuniorFee()
            experienceYears <= BillingConstants.MID_MAX_YEARS -> getMidFee()
            else -> getSeniorFee()
        }
    }
}
