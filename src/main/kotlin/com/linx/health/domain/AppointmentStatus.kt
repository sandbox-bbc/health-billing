package com.linx.health.domain

/**
 * Appointment lifecycle states.
 * 
 * SCHEDULED → COMPLETED (eligible for billing)
 * SCHEDULED → CANCELLED (no billing possible)
 */
enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
