package com.linx.health.service

import com.linx.health.domain.Appointment
import com.linx.health.domain.AppointmentStatus
import com.linx.health.domain.Doctor
import com.linx.health.domain.Specialty
import com.linx.health.exception.BadRequestException
import com.linx.health.exception.ConflictException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.BillRepository
import com.linx.health.repository.DoctorRepository
import com.linx.health.service.billing.BillingCalculator
import com.linx.health.service.billing.CardioFeeStrategy
import com.linx.health.service.billing.OrthoFeeStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for BillingService validation logic.
 * Calculation logic is tested in BillingCalculatorTest.
 */
class BillingServiceTest : FunSpec({
    
    val billingCalculator = BillingCalculator(
        orthoStrategy = OrthoFeeStrategy(),
        cardioStrategy = CardioFeeStrategy()
    )

    test("throws NotFoundException when appointment not found") {
        val billRepo = mockk<BillRepository>(relaxed = true)
        val apptRepo = mockk<AppointmentRepository>()
        val doctorRepo = mockk<DoctorRepository>()
        val svc = BillingService(billRepo, apptRepo, doctorRepo, billingCalculator)
        
        val appointmentId = UUID.randomUUID()
        every { apptRepo.findById(appointmentId) } returns null

        val exception = shouldThrow<NotFoundException> {
            svc.generateBill(appointmentId)
        }
        exception.errorCode shouldBe ErrorCodes.APPOINTMENT_NOT_FOUND
    }

    test("throws BadRequestException when appointment is SCHEDULED") {
        val billRepo = mockk<BillRepository>(relaxed = true)
        val apptRepo = mockk<AppointmentRepository>()
        val doctorRepo = mockk<DoctorRepository>()
        val svc = BillingService(billRepo, apptRepo, doctorRepo, billingCalculator)
        
        val appointmentId = UUID.randomUUID()
        val appointment = Appointment(
            id = appointmentId,
            patientId = UUID.randomUUID(),
            doctorId = UUID.randomUUID(),
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.SCHEDULED
        )
        every { apptRepo.findById(appointmentId) } returns appointment

        val exception = shouldThrow<BadRequestException> {
            svc.generateBill(appointmentId)
        }
        exception.errorCode shouldBe ErrorCodes.APPOINTMENT_NOT_COMPLETED
    }

    test("throws BadRequestException when appointment is CANCELLED") {
        val billRepo = mockk<BillRepository>(relaxed = true)
        val apptRepo = mockk<AppointmentRepository>()
        val doctorRepo = mockk<DoctorRepository>()
        val svc = BillingService(billRepo, apptRepo, doctorRepo, billingCalculator)
        
        val appointmentId = UUID.randomUUID()
        val appointment = Appointment(
            id = appointmentId,
            patientId = UUID.randomUUID(),
            doctorId = UUID.randomUUID(),
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.CANCELLED
        )
        every { apptRepo.findById(appointmentId) } returns appointment

        val exception = shouldThrow<BadRequestException> {
            svc.generateBill(appointmentId)
        }
        exception.errorCode shouldBe ErrorCodes.APPOINTMENT_NOT_COMPLETED
    }

    test("throws ConflictException when bill already exists") {
        val billRepo = mockk<BillRepository>()
        val apptRepo = mockk<AppointmentRepository>()
        val doctorRepo = mockk<DoctorRepository>()
        val svc = BillingService(billRepo, apptRepo, doctorRepo, billingCalculator)
        
        val appointmentId = UUID.randomUUID()
        val appointment = Appointment(
            id = appointmentId,
            patientId = UUID.randomUUID(),
            doctorId = UUID.randomUUID(),
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.COMPLETED
        )
        every { apptRepo.findById(appointmentId) } returns appointment
        every { billRepo.existsByAppointmentId(appointmentId) } returns true

        val exception = shouldThrow<ConflictException> {
            svc.generateBill(appointmentId)
        }
        exception.errorCode shouldBe ErrorCodes.BILL_ALREADY_EXISTS
    }

    test("throws NotFoundException when doctor not found") {
        val billRepo = mockk<BillRepository>()
        val apptRepo = mockk<AppointmentRepository>()
        val doctorRepo = mockk<DoctorRepository>()
        val svc = BillingService(billRepo, apptRepo, doctorRepo, billingCalculator)
        
        val doctorId = UUID.randomUUID()
        val appointmentId = UUID.randomUUID()
        val appointment = Appointment(
            id = appointmentId,
            patientId = UUID.randomUUID(),
            doctorId = doctorId,
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.COMPLETED
        )
        every { apptRepo.findById(appointmentId) } returns appointment
        every { billRepo.existsByAppointmentId(appointmentId) } returns false
        every { doctorRepo.findById(doctorId) } returns null

        val exception = shouldThrow<NotFoundException> {
            svc.generateBill(appointmentId)
        }
        exception.errorCode shouldBe ErrorCodes.DOCTOR_NOT_FOUND
    }
})
