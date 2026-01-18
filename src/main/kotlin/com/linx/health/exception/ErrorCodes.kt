package com.linx.health.exception

/**
 * Centralized error codes for the application.
 * Provides IDE autocomplete and prevents typos.
 * 
 * Add new codes as features are implemented.
 */
object ErrorCodes {
    // Patient
    const val PATIENT_NOT_FOUND = "PATIENT_NOT_FOUND"
    const val PATIENT_HAS_APPOINTMENTS = "PATIENT_HAS_APPOINTMENTS"
}
