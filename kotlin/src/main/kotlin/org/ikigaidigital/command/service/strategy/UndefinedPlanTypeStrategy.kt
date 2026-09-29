package org.ikigaidigital.command.service.strategy

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy.Companion.ZERO_INTEREST_RATE

internal class UndefinedPlanTypeStrategy : PlanTypeStrategy {
    override fun calculateInterest(deposit: TimeDeposit): Double = ZERO_INTEREST_RATE
}
