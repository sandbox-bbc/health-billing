package com.linx.health.service

import com.linx.health.domain.Appointment
import com.linx.health.domain.AppointmentStatus
import com.linx.health.dto.UpdateStatusRequest
import com.linx.health.exception.BadRequestException
import com.linx.health.exception.ErrorCodes
import com.linx.health.exception.NotFoundException
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.DoctorRepository
import com.linx.health.repository.PatientRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for AppointmentService status transition logic.
 * 
 * Status Transition Rules:
 * - SCHEDULED → COMPLETED ✅
 * - SCHEDULED → CANCELLED ✅
 * - COMPLETED → anything ❌
 * - CANCELLED → anything ❌
 * - SCHEDULED → SCHEDULED ❌
 */
class AppointmentServiceTest : FunSpec({

    val patientRepo = mockk<PatientRepository>()
    val doctorRepo = mockk<DoctorRepository>()
    val appointmentRepo = mockk<AppointmentRepository>()
    
    val service = AppointmentService(appointmentRepo, patientRepo, doctorRepo)

    // Test data
    val appointmentId = UUID.randomUUID()
    
    fun createAppointment(status: AppointmentStatus) = Appointment(
        id = appointmentId,
        patientId = UUID.randomUUID(),
        doctorId = UUID.randomUUID(),
        appointmentDate = LocalDate.now(),
        status = status
    )

    // --- Valid Transitions ---

    test("SCHEDULED can transition to COMPLETED") {
        val appointment = createAppointment(AppointmentStatus.SCHEDULED)
        every { appointmentRepo.findById(appointmentId) } returns appointment
        every { appointmentRepo.save(any()) } answers { firstArg() }

        val result = service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.COMPLETED))

        result.status shouldBe AppointmentStatus.COMPLETED
    }

    test("SCHEDULED can transition to CANCELLED") {
        val appointment = createAppointment(AppointmentStatus.SCHEDULED)
        every { appointmentRepo.findById(appointmentId) } returns appointment
        every { appointmentRepo.save(any()) } answers { firstArg() }

        val result = service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.CANCELLED))

        result.status shouldBe AppointmentStatus.CANCELLED
    }

    // --- Invalid Transitions ---

    test("COMPLETED appointment cannot change status") {
        val appointment = createAppointment(AppointmentStatus.COMPLETED)
        every { appointmentRepo.findById(appointmentId) } returns appointment

        val exception = shouldThrow<BadRequestException> {
            service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.CANCELLED))
        }
        exception.errorCode shouldBe ErrorCodes.INVALID_STATUS_TRANSITION
    }

    test("CANCELLED appointment cannot change status") {
        val appointment = createAppointment(AppointmentStatus.CANCELLED)
        every { appointmentRepo.findById(appointmentId) } returns appointment

        val exception = shouldThrow<BadRequestException> {
            service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.COMPLETED))
        }
        exception.errorCode shouldBe ErrorCodes.INVALID_STATUS_TRANSITION
    }

    test("SCHEDULED cannot transition to SCHEDULED") {
        val appointment = createAppointment(AppointmentStatus.SCHEDULED)
        every { appointmentRepo.findById(appointmentId) } returns appointment

        val exception = shouldThrow<BadRequestException> {
            service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.SCHEDULED))
        }
        exception.errorCode shouldBe ErrorCodes.INVALID_STATUS_TRANSITION
    }

    // --- Not Found ---

    test("throws NotFoundException when appointment not found") {
        every { appointmentRepo.findById(appointmentId) } returns null

        val exception = shouldThrow<NotFoundException> {
            service.updateStatus(appointmentId, UpdateStatusRequest(AppointmentStatus.COMPLETED))
        }
        exception.errorCode shouldBe ErrorCodes.APPOINTMENT_NOT_FOUND
    }
})
