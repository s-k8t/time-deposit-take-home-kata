package org.ikigaidigital

import org.ikigaidigital.command.service.strategy.BasicPlanTypeStrategy
import org.ikigaidigital.command.service.strategy.PlanTypeStrategy
import org.ikigaidigital.command.service.strategy.PremiumPlanTypeStrategy
import org.ikigaidigital.command.service.strategy.StudentPlanTypeStrategy
import org.ikigaidigital.command.service.strategy.UndefinedPlanTypeStrategy
import org.ikigaidigital.extension.roundToCents
import org.ikigaidigital.model.PlanType

class TimeDepositCalculator {
    fun updateBalance(xs: List<TimeDeposit>) {
        xs.forEach { deposit ->
            val planType = PlanType.fromString(deposit.planType)
            val interest = strategies.getValue(planType).calculateInterest(deposit)
            deposit.balance += interest.roundToCents()
        }
    }

    private val strategies: Map<PlanType, PlanTypeStrategy> =
        PlanType.entries.associateWith { planType ->
            when (planType) {
                PlanType.BASIC -> BasicPlanTypeStrategy()
                PlanType.STUDENT -> StudentPlanTypeStrategy()
                PlanType.PREMIUM -> PremiumPlanTypeStrategy()
                PlanType.UNDEFINED -> UndefinedPlanTypeStrategy()
            }
        }
}
