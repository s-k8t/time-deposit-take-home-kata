package org.ikigaidigital.strategies

import org.ikigaidigital.TimeDeposit

interface PlanTypeStrategy {
    companion object {
        const val MONTHS_IN_YEAR = 12
        const val ZERO_INTEREST_RATE = 0.00
    }

    fun calculateInterest(deposit: TimeDeposit): Double
}
