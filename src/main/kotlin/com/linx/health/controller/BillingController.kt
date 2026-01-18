package com.linx.health.controller

import com.linx.health.dto.BillResponse
import com.linx.health.service.BillingService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import java.net.URI
import java.util.UUID

/**
 * REST controller for Billing operations.
 * 
 * Billing is generated per-appointment, not in bulk.
 * Only COMPLETED appointments can be billed.
 */
@Controller("/bills")
class BillingController(private val billingService: BillingService) {
    
    /**
     * Generate bill for a completed appointment.
     * 
     * POST /bills?appointmentId={uuid}
     */
    @Post
    fun generateBill(@QueryValue appointmentId: UUID): HttpResponse<BillResponse> {
        val bill = billingService.generateBill(appointmentId)
        return HttpResponse.created<BillResponse>(URI.create("/bills/${bill.id}")).body(bill)
    }
    
    @Get("/{id}")
    fun getBill(@PathVariable id: UUID): BillResponse {
        return billingService.findById(id)
    }
    
    @Get
    fun getAllBills(@QueryValue appointmentId: UUID?): List<BillResponse> {
        return if (appointmentId != null) {
            listOf(billingService.findByAppointmentId(appointmentId))
        } else {
            billingService.findAll()
        }
    }
}
