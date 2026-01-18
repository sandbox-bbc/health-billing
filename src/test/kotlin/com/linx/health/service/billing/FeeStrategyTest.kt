package com.linx.health.service.billing

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

/**
 * Unit tests for Fee Strategy implementations.
 * Tests each specialty's fee calculation across experience brackets.
 */
class FeeStrategyTest : DescribeSpec({

    describe("OrthoFeeStrategy") {
        val strategy = OrthoFeeStrategy()

        describe("experience brackets") {
            it("returns $800 for junior (0-19 years)") {
                strategy.getBaseFee(0) shouldBe BigDecimal("800")
                strategy.getBaseFee(10) shouldBe BigDecimal("800")
                strategy.getBaseFee(19) shouldBe BigDecimal("800")
            }

            it("returns $1000 for mid (20-30 years)") {
                strategy.getBaseFee(20) shouldBe BigDecimal("1000")
                strategy.getBaseFee(25) shouldBe BigDecimal("1000")
                strategy.getBaseFee(30) shouldBe BigDecimal("1000")
            }

            it("returns $1500 for senior (31+ years)") {
                strategy.getBaseFee(31) shouldBe BigDecimal("1500")
                strategy.getBaseFee(40) shouldBe BigDecimal("1500")
                strategy.getBaseFee(50) shouldBe BigDecimal("1500")
            }
        }

        describe("boundary conditions") {
            it("19 years is junior, 20 years is mid") {
                strategy.getBaseFee(19) shouldBe BigDecimal("800")
                strategy.getBaseFee(20) shouldBe BigDecimal("1000")
            }

            it("30 years is mid, 31 years is senior") {
                strategy.getBaseFee(30) shouldBe BigDecimal("1000")
                strategy.getBaseFee(31) shouldBe BigDecimal("1500")
            }
        }
    }

    describe("CardioFeeStrategy") {
        val strategy = CardioFeeStrategy()

        describe("experience brackets") {
            it("returns $1000 for junior (0-19 years)") {
                strategy.getBaseFee(0) shouldBe BigDecimal("1000")
                strategy.getBaseFee(10) shouldBe BigDecimal("1000")
                strategy.getBaseFee(19) shouldBe BigDecimal("1000")
            }

            it("returns $1500 for mid (20-30 years)") {
                strategy.getBaseFee(20) shouldBe BigDecimal("1500")
                strategy.getBaseFee(25) shouldBe BigDecimal("1500")
                strategy.getBaseFee(30) shouldBe BigDecimal("1500")
            }

            it("returns $2000 for senior (31+ years)") {
                strategy.getBaseFee(31) shouldBe BigDecimal("2000")
                strategy.getBaseFee(40) shouldBe BigDecimal("2000")
                strategy.getBaseFee(50) shouldBe BigDecimal("2000")
            }
        }
    }
})
