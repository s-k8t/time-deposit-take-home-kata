package org.ikigaidigital.strategies

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.strategies.PlanTypeStrategy.Companion.MONTHS_IN_YEAR
import org.ikigaidigital.strategies.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE

internal class BasicPlanTypeStrategy : PlanTypeStrategy {
    private companion object {
        const val START_DAY = 31
        const val ANNUAL_INTEREST_RATE = 0.01
    }

    override fun calculateInterest(deposit: TimeDeposit): Double =
        if (deposit.days >= START_DAY) deposit.balance * ANNUAL_INTEREST_RATE / MONTHS_IN_YEAR else ZERO_INTEREST_RATE
}
