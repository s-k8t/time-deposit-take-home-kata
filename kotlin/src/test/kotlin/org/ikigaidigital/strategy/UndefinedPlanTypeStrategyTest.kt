package org.ikigaidigital.strategy

import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDeposit
import org.ikigaidigital.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class UndefinedPlanTypeStrategyTest {
    private val sut = UndefinedPlanTypeStrategy()

    @ParameterizedTest(name = "should return zero interest when days = {0}")
    @ValueSource(ints = [0, 30, 31, 46, 365, 366, 3650])
    fun `should return zero interest when plan type is unknown`(days: Int) {
        // prepare
        val deposit = createTimeDeposit(planType = "other", days = days)
        val expectedInterest = ZERO_INTEREST_RATE

        // execute
        val interest = sut.calculateInterest(deposit)

        // verify
        assertThat(interest).isEqualTo(expectedInterest)
    }
}
