package com.linx.health.domain

import io.micronaut.serde.annotation.Serdeable

/**
 * Patient insurance information.
 * Embedded within Patient entity (not stored separately).
 * 
 * @property binNo Bank Identification Number
 * @property pcnNo Processor Control Number
 * @property memberId Unique member identifier
 */
@Serdeable
data class InsuranceInfo(
    val binNo: String,
    val pcnNo: String,
    val memberId: String
)
