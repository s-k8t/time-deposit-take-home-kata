package org.ikigaidigital

import org.ikigaidigital.domain.PlanType
import org.ikigaidigital.extension.roundToCents
import org.ikigaidigital.strategy.BasicPlanTypeStrategy
import org.ikigaidigital.strategy.PlanTypeStrategy
import org.ikigaidigital.strategy.PremiumPlanTypeStrategy
import org.ikigaidigital.strategy.StudentPlanTypeStrategy
import org.ikigaidigital.strategy.UndefinedPlanTypeStrategy

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
