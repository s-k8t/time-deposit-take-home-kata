package org.ikigaidigital.command.service.strategy

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createStudentTimeDeposit
import org.ikigaidigital.TestFixtures.expectedInterest
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class StudentPlanTypeStrategyTest {
    private val sut = StudentPlanTypeStrategy()

    @ParameterizedTest(name = "should return zero interest when days = {0}")
    @ValueSource(ints = [0, 1, 30])
    fun `should return zero interest when deposit is 30 days old or less`(days: Int) {
        // prepare
        val deposit = createStudentTimeDeposit().copy(days = days)
        val expectedInterest = ZERO_INTEREST_RATE

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }

    @ParameterizedTest(name = "should return 3% annual interest for one month when days = {0}")
    @ValueSource(ints = [31, 364, 365])
    fun `should return 3 percent annual interest for one month when deposit is between 31 and 365 days old`(days: Int) {
        // prepare
        val deposit = createStudentTimeDeposit().copy(days = days)
        val expectedInterest =
            expectedInterest(balance = deposit.balance, annualInterestRate = StudentPlanTypeStrategy.ANNUAL_INTEREST_RATE)

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }

    @ParameterizedTest(name = "should return zero interest when days = {0}")
    @ValueSource(ints = [366, 3650])
    fun `should return zero interest when deposit is older than one year`(days: Int) {
        // prepare
        val deposit = createStudentTimeDeposit().copy(days = days)
        val expectedInterest = ZERO_INTEREST_RATE

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }
}
