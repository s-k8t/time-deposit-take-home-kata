package org.ikigaidigital

import org.ikigaidigital.extensions.roundToCents
import org.ikigaidigital.models.PlanType
import org.ikigaidigital.strategies.BasicPlanTypeStrategy
import org.ikigaidigital.strategies.PlanTypeStrategy
import org.ikigaidigital.strategies.PremiumPlanTypeStrategy
import org.ikigaidigital.strategies.StudentPlanTypeStrategy
import org.ikigaidigital.strategies.UndefinedPlanTypeStrategy

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
