package com.linx.health.service.billing

import jakarta.inject.Named
import jakarta.inject.Singleton
import java.math.BigDecimal

/**
 * Fee strategy for Cardiology specialty.
 * 
 * Fee Table:
 * | Experience    | Fee    |
 * |---------------|--------|
 * | 0-19 years    | $1000  |
 * | 20-30 years   | $1500  |
 * | 31+ years     | $2000  |
 */
@Singleton
@Named("CARDIO")
class CardioFeeStrategy : BaseFeeStrategy() {
    
    companion object {
        private val JUNIOR_FEE = BigDecimal("1000")
        private val MID_FEE = BigDecimal("1500")
        private val SENIOR_FEE = BigDecimal("2000")
    }
    
    override fun getJuniorFee(): BigDecimal = JUNIOR_FEE
    override fun getMidFee(): BigDecimal = MID_FEE
    override fun getSeniorFee(): BigDecimal = SENIOR_FEE
}
