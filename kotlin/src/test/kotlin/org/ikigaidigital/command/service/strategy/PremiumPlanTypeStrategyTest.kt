package org.ikigaidigital.command.service.strategy

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createPremiumTimeDeposit
import org.ikigaidigital.TestFixtures.expectedInterest
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PremiumPlanTypeStrategyTest {
    private val sut = PremiumPlanTypeStrategy()

    @ParameterizedTest(name = "should return zero interest when days = {0}")
    @ValueSource(ints = [0, 1, 30, 31, 45])
    fun `should return zero interest when deposit is 45 days old or less`(days: Int) {
        // prepare
        val deposit = createPremiumTimeDeposit().copy(days = days)
        val expectedInterest = ZERO_INTEREST_RATE

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }

    @ParameterizedTest(name = "should return 5% annual interest for one month when days = {0}")
    @ValueSource(ints = [46, 365, 366, 3650])
    fun `should return 5 percent annual interest for one month when deposit is older than 45 days`(days: Int) {
        // prepare
        val deposit = createPremiumTimeDeposit().copy(days = days)
        val expectedInterest =
            expectedInterest(balance = deposit.balance, annualInterestRate = PremiumPlanTypeStrategy.ANNUAL_INTEREST_RATE)

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }
}
