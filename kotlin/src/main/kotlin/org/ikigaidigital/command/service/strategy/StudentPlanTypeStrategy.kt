package org.ikigaidigital.command.service.strategy

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy.Companion.MONTHS_IN_YEAR
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE

internal class StudentPlanTypeStrategy : PlanTypeStrategy {
    companion object {
        const val START_DAY = 31
        const val END_DAY = 365
        const val ANNUAL_INTEREST_RATE = 0.03
    }

    override fun calculateInterest(deposit: TimeDeposit): Double =
        if (deposit.days in START_DAY..END_DAY) deposit.balance * ANNUAL_INTEREST_RATE / MONTHS_IN_YEAR else ZERO_INTEREST_RATE
}
