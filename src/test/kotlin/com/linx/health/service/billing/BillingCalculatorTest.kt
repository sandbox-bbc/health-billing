package com.linx.health.service.billing

import com.linx.health.domain.Specialty
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

/**
 * Unit tests for BillingCalculator.
 * Tests discount, GST, and insurance calculations.
 */
class BillingCalculatorTest : DescribeSpec({

    val calculator = BillingCalculator(
        orthoStrategy = OrthoFeeStrategy(),
        cardioStrategy = CardioFeeStrategy()
    )

    describe("getBaseFee") {
        it("returns correct fee for ORTHO specialty") {
            calculator.getBaseFee(Specialty.ORTHO, 5) shouldBe BigDecimal("800")
            calculator.getBaseFee(Specialty.ORTHO, 25) shouldBe BigDecimal("1000")
            calculator.getBaseFee(Specialty.ORTHO, 35) shouldBe BigDecimal("1500")
        }

        it("returns correct fee for CARDIO specialty") {
            calculator.getBaseFee(Specialty.CARDIO, 5) shouldBe BigDecimal("1000")
            calculator.getBaseFee(Specialty.CARDIO, 25) shouldBe BigDecimal("1500")
            calculator.getBaseFee(Specialty.CARDIO, 35) shouldBe BigDecimal("2000")
        }
    }

    describe("calculateDiscountPercent") {
        it("returns 0% for first visit (0 prior appointments)") {
            calculator.calculateDiscountPercent(0) shouldBe 0
        }

        it("returns 1% for second visit (1 prior appointment)") {
            calculator.calculateDiscountPercent(1) shouldBe 1
        }

        it("returns exact percent up to 10") {
            calculator.calculateDiscountPercent(5) shouldBe 5
            calculator.calculateDiscountPercent(10) shouldBe 10
        }

        it("caps at 10% for more than 10 prior appointments") {
            calculator.calculateDiscountPercent(11) shouldBe 10
            calculator.calculateDiscountPercent(15) shouldBe 10
            calculator.calculateDiscountPercent(100) shouldBe 10
        }
    }

    describe("calculateDiscountAmount") {
        it("calculates discount correctly") {
            // $1500 base fee, 5% discount = $75
            calculator.calculateDiscountAmount(BigDecimal("1500"), 5) shouldBe BigDecimal("75.00")
        }

        it("returns 0 for 0% discount") {
            calculator.calculateDiscountAmount(BigDecimal("1500"), 0) shouldBe BigDecimal("0.00")
        }

        it("handles 10% discount") {
            // $1000 base fee, 10% discount = $100
            calculator.calculateDiscountAmount(BigDecimal("1000"), 10) shouldBe BigDecimal("100.00")
        }
    }

    describe("calculateGst") {
        it("calculates 12% GST") {
            // $1500 * 12% = $180
            calculator.calculateGst(BigDecimal("1500")) shouldBe BigDecimal("180.00")
        }

        it("rounds to 2 decimal places") {
            // $1485 * 12% = $178.2 -> $178.20
            calculator.calculateGst(BigDecimal("1485")) shouldBe BigDecimal("178.20")
        }
    }

    describe("calculateInsuranceAmount") {
        it("calculates 90% insurance coverage") {
            // $1680 * 90% = $1512
            calculator.calculateInsuranceAmount(BigDecimal("1680")) shouldBe BigDecimal("1512.00")
        }
    }

    describe("calculateCoPayAmount") {
        it("calculates 10% co-pay") {
            // $1680 * 10% = $168
            calculator.calculateCoPayAmount(BigDecimal("1680")) shouldBe BigDecimal("168.00")
        }
    }

    describe("full billing calculation scenario") {
        it("calculates bill for CARDIO mid-level doctor, first visit") {
            // Doctor: CARDIO, 26 years experience -> mid bracket -> $1500
            val baseFee = calculator.getBaseFee(Specialty.CARDIO, 26)
            baseFee.compareTo(BigDecimal("1500")) shouldBe 0

            // First visit -> 0% discount
            val discountPercent = calculator.calculateDiscountPercent(0)
            discountPercent shouldBe 0

            val discountAmount = calculator.calculateDiscountAmount(baseFee, discountPercent)
            discountAmount.compareTo(BigDecimal("0.00")) shouldBe 0

            // Discounted amount = $1500 - $0 = $1500
            val discountedAmount = baseFee.subtract(discountAmount)
            discountedAmount.compareTo(BigDecimal("1500")) shouldBe 0

            // GST = $1500 * 12% = $180
            val gst = calculator.calculateGst(discountedAmount)
            gst.compareTo(BigDecimal("180.00")) shouldBe 0

            // Total = $1500 + $180 = $1680
            val total = discountedAmount.add(gst)
            total.compareTo(BigDecimal("1680.00")) shouldBe 0

            // Insurance = $1680 * 90% = $1512
            val insurance = calculator.calculateInsuranceAmount(total)
            insurance.compareTo(BigDecimal("1512.00")) shouldBe 0

            // Co-pay = $1680 * 10% = $168
            val coPay = calculator.calculateCoPayAmount(total)
            coPay.compareTo(BigDecimal("168.00")) shouldBe 0
        }

        it("calculates bill with 1% loyalty discount (second visit)") {
            // Doctor: CARDIO, 26 years experience -> $1500
            val baseFee = calculator.getBaseFee(Specialty.CARDIO, 26)

            // Second visit -> 1% discount
            val discountPercent = calculator.calculateDiscountPercent(1)
            discountPercent shouldBe 1

            val discountAmount = calculator.calculateDiscountAmount(baseFee, discountPercent)
            discountAmount.compareTo(BigDecimal("15.00")) shouldBe 0

            // Discounted = $1500 - $15 = $1485
            val discountedAmount = baseFee.subtract(discountAmount)
            discountedAmount.compareTo(BigDecimal("1485")) shouldBe 0

            // GST = $1485 * 12% = $178.20
            val gst = calculator.calculateGst(discountedAmount)
            gst.compareTo(BigDecimal("178.20")) shouldBe 0

            // Total = $1485 + $178.20 = $1663.20
            val total = discountedAmount.add(gst)
            total.compareTo(BigDecimal("1663.20")) shouldBe 0

            // Insurance = $1663.20 * 90% = $1496.88
            val insurance = calculator.calculateInsuranceAmount(total)
            insurance.compareTo(BigDecimal("1496.88")) shouldBe 0

            // Co-pay = $1663.20 * 10% = $166.32
            val coPay = calculator.calculateCoPayAmount(total)
            coPay.compareTo(BigDecimal("166.32")) shouldBe 0
        }
    }
})
