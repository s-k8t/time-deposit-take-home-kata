package org.ikigaidigital

import org.ikigaidigital.extensions.roundToCents

internal object TestFixtures {
    fun createBasicTimeDeposit() = TimeDeposit(id = 1, planType = "basic", balance = 10.00, days = 31)

    fun createStudentTimeDeposit() = TimeDeposit(id = 1, planType = "student", balance = 10.00, days = 31)

    fun createPremiumTimeDeposit() = TimeDeposit(id = 1, planType = "premium", balance = 10.00, days = 31)

    fun expectedInterest(
        balance: Double,
        annualInterestRate: Double,
    ): Double = (balance * annualInterestRate / 12).roundToCents()
}
