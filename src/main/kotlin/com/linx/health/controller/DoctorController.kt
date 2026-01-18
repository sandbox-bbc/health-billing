package com.linx.health.controller

import com.linx.health.dto.CreateDoctorRequest
import com.linx.health.dto.DoctorResponse
import com.linx.health.service.DoctorService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import java.net.URI
import java.util.UUID

/**
 * REST controller for Doctor management.
 * Note: No PUT/PATCH endpoint - doctors are immutable after creation (ADR-002).
 */
@Controller("/doctors")
class DoctorController(private val doctorService: DoctorService) {
    
    @Post
    fun createDoctor(@Body request: CreateDoctorRequest): HttpResponse<DoctorResponse> {
        val doctor = doctorService.create(request)
        return HttpResponse.created<DoctorResponse>(URI.create("/doctors/${doctor.id}")).body(doctor)
    }
    
    @Get("/{id}")
    fun getDoctor(@PathVariable id: UUID): DoctorResponse {
        return doctorService.findById(id)
    }
    
    @Get
    fun getAllDoctors(): List<DoctorResponse> {
        return doctorService.findAll()
    }
    
    @Delete("/{id}")
    fun deleteDoctor(@PathVariable id: UUID): HttpResponse<Unit> {
        doctorService.delete(id)
        return HttpResponse.noContent()
    }
}
