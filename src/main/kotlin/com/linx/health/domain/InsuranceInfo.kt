package com.linx.health.domain

/**
 * Patient insurance information.
 * Embedded within Patient entity (not stored separately).
 * 
 * Note: No @Serdeable - domain models are not serialized directly.
 * Use DTOs for API request/response.
 * 
 * @property binNo Bank Identification Number
 * @property pcnNo Processor Control Number
 * @property memberId Unique member identifier
 */
data class InsuranceInfo(
    val binNo: String,
    val pcnNo: String,
    val memberId: String
)
