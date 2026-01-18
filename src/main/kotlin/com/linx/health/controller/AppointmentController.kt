package com.linx.health.controller

import com.linx.health.dto.AppointmentResponse
import com.linx.health.dto.CreateAppointmentRequest
import com.linx.health.dto.UpdateStatusRequest
import com.linx.health.service.AppointmentService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import java.net.URI
import java.util.UUID

/**
 * REST controller for Appointment management.
 */
@Controller("/appointments")
class AppointmentController(private val appointmentService: AppointmentService) {
    
    @Post
    fun createAppointment(@Body request: CreateAppointmentRequest): HttpResponse<AppointmentResponse> {
        val appointment = appointmentService.create(request)
        return HttpResponse.created<AppointmentResponse>(URI.create("/appointments/${appointment.id}")).body(appointment)
    }
    
    @Get("/{id}")
    fun getAppointment(@PathVariable id: UUID): AppointmentResponse {
        return appointmentService.findById(id)
    }
    
    @Get
    fun getAllAppointments(
        @QueryValue patientId: UUID?,
        @QueryValue doctorId: UUID?
    ): List<AppointmentResponse> {
        return when {
            patientId != null -> appointmentService.findByPatientId(patientId)
            doctorId != null -> appointmentService.findByDoctorId(doctorId)
            else -> appointmentService.findAll()
        }
    }
    
    /**
     * Update appointment status.
     * Only SCHEDULED → COMPLETED or SCHEDULED → CANCELLED allowed.
     */
    @Put("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Body request: UpdateStatusRequest
    ): AppointmentResponse {
        return appointmentService.updateStatus(id, request)
    }
    
    @Delete("/{id}")
    fun deleteAppointment(@PathVariable id: UUID): HttpResponse<Unit> {
        appointmentService.delete(id)
        return HttpResponse.noContent()
    }
}
