package org.ikigaidigital

import org.ikigaidigital.api.model.Amount
import org.ikigaidigital.api.model.Day
import org.ikigaidigital.api.model.PlanTypeName
import org.ikigaidigital.api.model.TimeDepositId
import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.api.model.WithdrawalDate
import org.ikigaidigital.api.model.WithdrawalId
import org.ikigaidigital.api.model.WithdrawalResponse
import org.ikigaidigital.domain.PlanType
import org.ikigaidigital.extension.roundToCents
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource.Output
import org.ikigaidigital.store.entity.TimeDepositEntity
import org.ikigaidigital.store.entity.WithdrawalEntity
import java.math.BigDecimal
import java.time.LocalDate

internal object TestFixtures {
    fun createTimeDeposit(
        planType: String = PlanType.BASIC.code,
        days: Int = 31,
    ) = TimeDeposit(id = 1, planType = planType, balance = 10.00, days = days)

    fun createBasicTimeDeposit() = createTimeDeposit(planType = PlanType.BASIC.code)

    fun createStudentTimeDeposit() = createTimeDeposit(planType = PlanType.STUDENT.code)

    fun createPremiumTimeDeposit() = createTimeDeposit(planType = PlanType.PREMIUM.code)

    fun createTimeDepositEntity(
        planType: String = PlanType.BASIC.code,
        balance: BigDecimal = BigDecimal("1234.56"),
        days: Int = 45,
    ) = TimeDepositEntity(planType = planType, days = days, balance = balance)

    fun createWithdrawalEntity(
        timeDepositId: Int,
        amount: BigDecimal = BigDecimal("100.00"),
        date: LocalDate = LocalDate.of(2026, 9, 1),
    ) = WithdrawalEntity(timeDepositId = timeDepositId, amount = amount, date = date)

    fun createTimeDepositOutput(
        id: Int = 1,
        planType: String = PlanType.BASIC.code,
        balance: Double = 1234.56,
        days: Int = 45,
        withdrawals: List<Output.Withdrawal> = listOf(createWithdrawalOutput()),
    ) = Output(id = id, planType = planType, balance = balance, days = days, withdrawals = withdrawals)

    fun createWithdrawalOutput(
        id: Int = 1,
        amount: Double = 100.00,
        date: LocalDate = LocalDate.of(2026, 9, 1),
    ) = Output.Withdrawal(id = id, amount = amount, date = date)

    fun createTimeDepositResponse(
        id: Int = 1,
        planType: PlanTypeName = PlanTypeName.BASIC,
        balance: Double = 1234.56,
        days: Int = 45,
        withdrawals: List<WithdrawalResponse> = listOf(createWithdrawalResponse()),
    ) = TimeDepositResponse(
        id = TimeDepositId(id),
        planType = planType,
        balance = Amount(balance),
        days = Day(days),
        withdrawals = withdrawals,
    )

    fun createWithdrawalResponse(
        id: Int = 1,
        amount: Double = 100.00,
        date: LocalDate = LocalDate.of(2026, 9, 1),
    ) = WithdrawalResponse(
        id = WithdrawalId(id),
        amount = Amount(amount),
        date = WithdrawalDate(date),
    )

    fun expectedInterest(
        balance: Double,
        annualInterestRate: Double,
    ): Double = balance * annualInterestRate / 12

    fun expectedInterestRoundedToCents(
        balance: Double,
        annualInterestRate: Double,
    ): Double = (balance * annualInterestRate / 12).roundToCents()
}
