package com.linx.health.service.billing

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Billing-related constants.
 * Centralized for easy maintenance and configuration.
 * 
 * Extension Point: To change rates, modify these constants.
 * Future enhancement: Could be loaded from configuration.
 */
object BillingConstants {
    // Tax and Insurance Rates
    val GST_RATE: BigDecimal = BigDecimal("0.12")           // 12%
    val INSURANCE_RATE: BigDecimal = BigDecimal("0.90")     // 90%
    val CO_PAY_RATE: BigDecimal = BigDecimal("0.10")        // 10%
    
    // Loyalty Discount
    const val MAX_DISCOUNT_PERCENT = 10
    
    // Monetary Calculation Settings
    const val MONEY_SCALE = 2
    val ROUNDING_MODE: RoundingMode = RoundingMode.HALF_UP
    
    // Experience Brackets (years)
    const val JUNIOR_MAX_YEARS = 19      // 0-19 years
    const val MID_MAX_YEARS = 30         // 20-30 years
    // 31+ years = SENIOR
}
