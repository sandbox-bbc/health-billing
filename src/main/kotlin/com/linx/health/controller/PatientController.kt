package com.linx.health.controller

import com.linx.health.dto.CreatePatientRequest
import com.linx.health.dto.PatientResponse
import com.linx.health.dto.UpdatePatientRequest
import com.linx.health.service.PatientService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import java.util.UUID

@Controller("/patients")
class PatientController(
    private val patientService: PatientService
) {
    
    @Post
    fun create(@Body request: CreatePatientRequest): HttpResponse<PatientResponse> {
        val patient = patientService.create(request)
        return HttpResponse.created(patient)
    }
    
    @Get("/{id}")
    fun findById(@PathVariable id: UUID): PatientResponse {
        return patientService.findById(id)
    }
    
    @Get
    fun findAll(): List<PatientResponse> {
        return patientService.findAll()
    }
    
    @Put("/{id}")
    fun update(@PathVariable id: UUID, @Body request: UpdatePatientRequest): PatientResponse {
        return patientService.update(id, request)
    }
    
    @Delete("/{id}")
    fun delete(@PathVariable id: UUID): HttpResponse<Unit> {
        patientService.delete(id)
        return HttpResponse.noContent()
    }
}
