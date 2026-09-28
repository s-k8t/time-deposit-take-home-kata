package org.ikigaidigital

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.expectedInterestRoundedToCents
import org.ikigaidigital.models.PlanType
import org.ikigaidigital.strategies.BasicPlanTypeStrategy
import org.ikigaidigital.strategies.PremiumPlanTypeStrategy
import org.ikigaidigital.strategies.StudentPlanTypeStrategy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

internal class TimeDepositCalculatorTest {
    private val sut = TimeDepositCalculator()

    @ParameterizedTest(name = "should add rounded monthly interest when plan type = \"{0}\"")
    @MethodSource("knownPlanTypes")
    fun `should add rounded monthly interest when plan type is known and deposit is eligible`(
        planType: String,
        annualInterestRate: Double,
    ) {
        // prepare
        val deposit = TimeDeposit(id = 1, planType = planType, balance = 10.00, days = 46)
        val expectedDeposit =
            deposit.copy(
                balance =
                    deposit.balance +
                        expectedInterestRoundedToCents(balance = deposit.balance, annualInterestRate = annualInterestRate),
            )

        // execute
        sut.updateBalance(listOf(deposit))

        // verify
        assertThat(deposit.id).isEqualTo(expectedDeposit.id)
        assertThat(deposit.planType).isEqualTo(expectedDeposit.planType)
        assertThat(deposit.balance).isEqualTo(expectedDeposit.balance)
        assertThat(deposit.days).isEqualTo(expectedDeposit.days)
    }

    @ParameterizedTest(name = "should not add interest when plan type = \"{0}\" and days = {1}")
    @CsvSource(
        "basic, 30",
        "student, 30",
        "student, 366",
        "premium, 45",
    )
    fun `should not add interest when deposit is not eligible for plan interest`(
        planType: String,
        days: Int,
    ) {
        // prepare
        val deposit = TimeDeposit(id = 1, planType = planType, balance = 10.00, days = days)
        val expectedDeposit = deposit.copy()

        // execute
        sut.updateBalance(listOf(deposit))

        // verify
        assertThat(deposit.id).isEqualTo(expectedDeposit.id)
        assertThat(deposit.planType).isEqualTo(expectedDeposit.planType)
        assertThat(deposit.balance).isEqualTo(expectedDeposit.balance)
        assertThat(deposit.days).isEqualTo(expectedDeposit.days)
    }

    @ParameterizedTest(name = "should not add interest when plan type = \"{0}\"")
    @ValueSource(strings = ["", "other", "Basic", "STUDENT"])
    fun `should not add interest when plan type is unknown`(planType: String) {
        // prepare
        val deposit = TimeDeposit(id = 1, planType = planType, balance = 10.00, days = 46)
        val expectedDeposit = deposit.copy()

        // execute
        sut.updateBalance(listOf(deposit))

        // verify
        assertThat(deposit.id).isEqualTo(expectedDeposit.id)
        assertThat(deposit.planType).isEqualTo(expectedDeposit.planType)
        assertThat(deposit.balance).isEqualTo(expectedDeposit.balance)
        assertThat(deposit.days).isEqualTo(expectedDeposit.days)
    }

    companion object {
        @JvmStatic
        fun knownPlanTypes() =
            listOf(
                Arguments.of(PlanType.BASIC.name.lowercase(), BasicPlanTypeStrategy.ANNUAL_INTEREST_RATE),
                Arguments.of(PlanType.STUDENT.name.lowercase(), StudentPlanTypeStrategy.ANNUAL_INTEREST_RATE),
                Arguments.of(PlanType.PREMIUM.name.lowercase(), PremiumPlanTypeStrategy.ANNUAL_INTEREST_RATE),
            )
    }
}
