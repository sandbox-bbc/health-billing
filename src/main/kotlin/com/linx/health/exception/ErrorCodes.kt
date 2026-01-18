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
    
    // Doctor
    const val DOCTOR_NOT_FOUND = "DOCTOR_NOT_FOUND"
    const val DOCTOR_HAS_APPOINTMENTS = "DOCTOR_HAS_APPOINTMENTS"
    const val DUPLICATE_NPI = "DUPLICATE_NPI"
    
    // Appointment
    const val APPOINTMENT_NOT_FOUND = "APPOINTMENT_NOT_FOUND"
    const val INVALID_PATIENT = "INVALID_PATIENT"
    const val INVALID_DOCTOR = "INVALID_DOCTOR"
    const val INVALID_STATUS_TRANSITION = "INVALID_STATUS_TRANSITION"
    
    // Billing
    const val BILL_NOT_FOUND = "BILL_NOT_FOUND"
    const val APPOINTMENT_NOT_COMPLETED = "APPOINTMENT_NOT_COMPLETED"
    const val BILL_ALREADY_EXISTS = "BILL_ALREADY_EXISTS"
}
