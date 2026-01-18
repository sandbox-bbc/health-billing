package com.linx.health.service.billing

import jakarta.inject.Named
import jakarta.inject.Singleton
import java.math.BigDecimal

/**
 * Fee strategy for Orthopedic specialty.
 * 
 * Fee Table:
 * | Experience    | Fee    |
 * |---------------|--------|
 * | 0-19 years    | $800   |
 * | 20-30 years   | $1000  |
 * | 31+ years     | $1500  |
 */
@Singleton
@Named("ORTHO")
class OrthoFeeStrategy : BaseFeeStrategy() {
    
    companion object {
        private val JUNIOR_FEE = BigDecimal("800")
        private val MID_FEE = BigDecimal("1000")
        private val SENIOR_FEE = BigDecimal("1500")
    }
    
    override fun getJuniorFee(): BigDecimal = JUNIOR_FEE
    override fun getMidFee(): BigDecimal = MID_FEE
    override fun getSeniorFee(): BigDecimal = SENIOR_FEE
}
