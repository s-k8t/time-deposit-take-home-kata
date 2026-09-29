package org.ikigaidigital.strategy

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createBasicTimeDeposit
import org.ikigaidigital.TestFixtures.expectedInterest
import org.ikigaidigital.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class BasicPlanTypeStrategyTest {
    private val sut = BasicPlanTypeStrategy()

    @ParameterizedTest(name = "should return zero interest when days = {0}")
    @ValueSource(ints = [0, 1, 30])
    fun `should return zero interest when deposit is 30 days old or less`(days: Int) {
        // prepare
        val deposit = createBasicTimeDeposit().copy(days = days)
        val expectedInterest = ZERO_INTEREST_RATE

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }

    @ParameterizedTest(name = "should return 1% annual interest for one month when days = {0}")
    @ValueSource(ints = [31, 365, 366, 3650])
    fun `should return 1 percent annual interest for one month when deposit is older than 30 days`(days: Int) {
        // prepare
        val deposit = createBasicTimeDeposit().copy(days = days)
        val expectedInterest = expectedInterest(balance = deposit.balance, annualInterestRate = BasicPlanTypeStrategy.ANNUAL_INTEREST_RATE)

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }
}
