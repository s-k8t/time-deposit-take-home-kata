package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createBasicTimeDeposit
import org.ikigaidigital.TestFixtures.createPremiumTimeDeposit
import org.ikigaidigital.TestFixtures.createStudentTimeDeposit
import org.ikigaidigital.TestFixtures.expectedInterest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class TimeDepositCalculatorTest {
    private val sut = TimeDepositCalculator()

    @Nested
    inner class BasicPlan {
        // Basic Plan: 1% interest, No interest is applied for the first 30 days.
        private val annualInterestRate = 1.0

        @ParameterizedTest(name = "should not add interest when days = {0}")
        @ValueSource(ints = [0, 1, 30])
        fun `should not add interest when deposit is less or equal to 30 days`(days: Int) {
            // prepare
            val deposit = createBasicTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }

        @ParameterizedTest(name = "should add 1% annual interest when days = {0}")
        @ValueSource(ints = [31, 366, 3650])
        fun `should add 1 percent annual interest when deposit is older than 30 days`(days: Int) {
            // prepare
            val deposit = createBasicTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance + expectedInterest(balance = deposit.balance, annualInterestRate = annualInterestRate)

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }
    }

    @Nested
    inner class StudentPlan {
        // 3% interest (no interest after 1 year), No interest is applied for the first 30 days.
        private val annualInterestRate = 3.0

        @ParameterizedTest(name = "should not add interest when days = {0}")
        @ValueSource(ints = [0, 1, 30])
        fun `should not add interest when deposit is less or equal to 30 days`(days: Int) {
            // prepare
            val deposit = createStudentTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }

        @ParameterizedTest(name = "should add 3% annual interest when days = {0}")
        @ValueSource(ints = [31, 364, 365])
        fun `should add 3 percent annual interest when deposit is between 31 and 365 days old`(days: Int) {
            // prepare

            val deposit = createStudentTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance + expectedInterest(balance = deposit.balance, annualInterestRate = annualInterestRate)

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }

        @ParameterizedTest(name = "should not add interest when days = {0}")
        @ValueSource(ints = [366, 367, 3650])
        fun `should not add interest when deposit is older than one year`(days: Int) {
            // prepare
            val deposit = createStudentTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }
    }

    @Nested
    inner class PremiumPlan {
        // Premium Plan: 5% interest (interest starts after 45 days)
        private val annualInterestRate = 5.0

        @ParameterizedTest(name = "should not add interest when days = {0}")
        @ValueSource(ints = [0, 1, 45])
        fun `should not add interest when deposit is less or equal to 45 days`(days: Int) {
            // prepare
            val deposit = createPremiumTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }

        @ParameterizedTest(name = "should add 5% annual interest when days = {0}")
        @ValueSource(ints = [46, 365, 366, 3650])
        fun `should add 5 percent annual interest when deposit is older than 45 days`(days: Int) {
            // prepare
            val deposit = createPremiumTimeDeposit().copy(days = days)
            val expectedBalance = deposit.balance + expectedInterest(balance = deposit.balance, annualInterestRate = annualInterestRate)

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.id).isEqualTo(deposit.id)
            assertThat(deposit.planType).isEqualTo(deposit.planType)
            assertThat(deposit.balance).isEqualTo(expectedBalance)
            assertThat(deposit.days).isEqualTo(deposit.days)
        }
    }

    @Nested
    inner class UnsupportedPlan {
        @ParameterizedTest(name = "should not add interest when plan type = \"{0}\"")
        @ValueSource(strings = ["other", "some"])
        fun `should leave balance unchanged when plan type is unknown`(planType: String) {
            // prepare
            val deposit = TimeDeposit(id = 1, planType = planType, balance = 50.00, days = 40)
            val expectedBalance = deposit.balance

            // execute
            sut.updateBalance(listOf(deposit))

            // verify
            assertThat(deposit.balance).isEqualTo(expectedBalance)
        }
    }
}
