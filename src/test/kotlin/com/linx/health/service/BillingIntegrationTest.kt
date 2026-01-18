package com.linx.health.service

import com.linx.health.domain.Appointment
import com.linx.health.domain.AppointmentStatus
import com.linx.health.domain.Doctor
import com.linx.health.domain.Patient
import com.linx.health.domain.InsuranceInfo
import com.linx.health.domain.Specialty
import com.linx.health.repository.AppointmentRepository
import com.linx.health.repository.BillRepository
import com.linx.health.repository.DoctorRepository
import com.linx.health.repository.PatientRepository
import com.linx.health.service.billing.BillingCalculator
import com.linx.health.service.billing.CardioFeeStrategy
import com.linx.health.service.billing.OrthoFeeStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Integration tests for BillingService using real repositories.
 * Tests the full billing flow with actual data.
 */
class BillingIntegrationTest : FunSpec({
    
    // Real implementations (not mocks)
    val patientRepo = PatientRepository()
    val doctorRepo = DoctorRepository()
    val appointmentRepo = AppointmentRepository()
    val billRepo = BillRepository()
    val billingCalculator = BillingCalculator(
        orthoStrategy = OrthoFeeStrategy(),
        cardioStrategy = CardioFeeStrategy()
    )
    val billingService = BillingService(billRepo, appointmentRepo, doctorRepo, billingCalculator)

    test("generates bill for first visit with 0% discount - CARDIO mid") {
        // Create patient
        val patient = Patient(
            firstName = "John",
            lastName = "Doe",
            dob = LocalDate.of(1990, 1, 1),
            insurance = InsuranceInfo("BIN123", "PCN456", "MEM789")
        )
        patientRepo.save(patient)

        // Create doctor - CARDIO, 26 years experience (mid bracket = $1500)
        val doctor = Doctor(
            firstName = "Jane",
            lastName = "Smith",
            npiNo = "NPI-FIRST-VISIT",
            specialty = Specialty.CARDIO,
            practiceStartDate = LocalDate.of(2000, 1, 1)
        )
        doctorRepo.save(doctor)

        // Create and complete appointment
        val appointment = Appointment(
            patientId = patient.id,
            doctorId = doctor.id,
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.COMPLETED
        )
        appointmentRepo.save(appointment)

        // Generate bill
        val bill = billingService.generateBill(appointment.id)

        // Verify: CARDIO mid = $1500, 0% discount
        bill.baseFee.compareTo(BigDecimal("1500")) shouldBe 0
        bill.discountPercent shouldBe 0
        bill.discountAmount.compareTo(BigDecimal("0.00")) shouldBe 0
        bill.gstAmount.compareTo(BigDecimal("180.00")) shouldBe 0  // 1500 * 12%
        bill.totalAmount.compareTo(BigDecimal("1680.00")) shouldBe 0  // 1500 + 180
        bill.insuranceAmount.compareTo(BigDecimal("1512.00")) shouldBe 0  // 1680 * 90%
        bill.coPayAmount.compareTo(BigDecimal("168.00")) shouldBe 0  // 1680 * 10%
    }

    test("generates bill with 5% loyalty discount after 5 prior visits") {
        // Create patient
        val patient = Patient(
            firstName = "Loyal",
            lastName = "Customer",
            dob = LocalDate.of(1985, 6, 15),
            insurance = InsuranceInfo("BIN-LOYAL", "PCN-LOYAL", "MEM-LOYAL")
        )
        patientRepo.save(patient)

        // Create doctor - ORTHO, 5 years experience (junior bracket = $800)
        val doctor = Doctor(
            firstName = "Dr",
            lastName = "Junior",
            npiNo = "NPI-LOYALTY-TEST",
            specialty = Specialty.ORTHO,
            practiceStartDate = LocalDate.of(2021, 1, 1)
        )
        doctorRepo.save(doctor)

        // Create 5 prior completed appointments
        repeat(5) { i ->
            val priorAppt = Appointment(
                patientId = patient.id,
                doctorId = doctor.id,
                appointmentDate = LocalDate.now().minusDays((i + 1).toLong()),
                status = AppointmentStatus.COMPLETED
            )
            appointmentRepo.save(priorAppt)
        }

        // Create the appointment to bill
        val appointment = Appointment(
            patientId = patient.id,
            doctorId = doctor.id,
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.COMPLETED
        )
        appointmentRepo.save(appointment)

        // Generate bill
        val bill = billingService.generateBill(appointment.id)

        // Verify: ORTHO junior = $800, 5% discount (5 prior visits)
        bill.baseFee.compareTo(BigDecimal("800")) shouldBe 0
        bill.discountPercent shouldBe 5
        bill.discountAmount.compareTo(BigDecimal("40.00")) shouldBe 0  // 800 * 5%
        // Discounted = 800 - 40 = 760
        bill.gstAmount.compareTo(BigDecimal("91.20")) shouldBe 0  // 760 * 12%
        bill.totalAmount.compareTo(BigDecimal("851.20")) shouldBe 0  // 760 + 91.20
    }

    test("caps loyalty discount at 10% for many prior visits") {
        // Create patient
        val patient = Patient(
            firstName = "Super",
            lastName = "Loyal",
            dob = LocalDate.of(1975, 3, 20),
            insurance = InsuranceInfo("BIN-SUPER", "PCN-SUPER", "MEM-SUPER")
        )
        patientRepo.save(patient)

        // Create doctor - CARDIO, 35 years experience (senior bracket = $2000)
        val doctor = Doctor(
            firstName = "Dr",
            lastName = "Senior",
            npiNo = "NPI-SUPER-LOYALTY",
            specialty = Specialty.CARDIO,
            practiceStartDate = LocalDate.of(1990, 1, 1)
        )
        doctorRepo.save(doctor)

        // Create 15 prior completed appointments
        repeat(15) { i ->
            val priorAppt = Appointment(
                patientId = patient.id,
                doctorId = doctor.id,
                appointmentDate = LocalDate.now().minusDays((i + 1).toLong()),
                status = AppointmentStatus.COMPLETED
            )
            appointmentRepo.save(priorAppt)
        }

        // Create the appointment to bill
        val appointment = Appointment(
            patientId = patient.id,
            doctorId = doctor.id,
            appointmentDate = LocalDate.now(),
            status = AppointmentStatus.COMPLETED
        )
        appointmentRepo.save(appointment)

        // Generate bill
        val bill = billingService.generateBill(appointment.id)

        // Verify: CARDIO senior = $2000, 10% max discount
        bill.baseFee.compareTo(BigDecimal("2000")) shouldBe 0
        bill.discountPercent shouldBe 10  // Capped at 10%
        bill.discountAmount.compareTo(BigDecimal("200.00")) shouldBe 0  // 2000 * 10%
        // Discounted = 2000 - 200 = 1800
        bill.gstAmount.compareTo(BigDecimal("216.00")) shouldBe 0  // 1800 * 12%
        bill.totalAmount.compareTo(BigDecimal("2016.00")) shouldBe 0  // 1800 + 216
    }
})
