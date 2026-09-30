package org.ikigaidigital.query.service

import org.ikigaidigital.api.model.Amount
import org.ikigaidigital.api.model.Day
import org.ikigaidigital.api.model.PlanTypeName
import org.ikigaidigital.api.model.TimeDepositId
import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.api.model.WithdrawalDate
import org.ikigaidigital.api.model.WithdrawalId
import org.ikigaidigital.api.model.WithdrawalResponse
import org.ikigaidigital.extension.daysUntil
import org.ikigaidigital.model.PlanType
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import java.time.LocalDate

internal fun GetAllTimeDepositsDataSource.Output.toResponse(today: LocalDate) =
    TimeDepositResponse(
        id = TimeDepositId(id),
        planType = PlanType.fromString(planType).toPlanTypeName(),
        balance = Amount(balance),
        days = Day(startDate.daysUntil(today)),
        withdrawals = withdrawals.map { it.toResponse() },
    )

internal fun GetAllTimeDepositsDataSource.Output.Withdrawal.toResponse() =
    WithdrawalResponse(
        id = WithdrawalId(id),
        amount = Amount(amount),
        date = WithdrawalDate(date),
    )

private fun PlanType.toPlanTypeName() =
    when (this) {
        PlanType.BASIC -> PlanTypeName.BASIC
        PlanType.STUDENT -> PlanTypeName.STUDENT
        PlanType.PREMIUM -> PlanTypeName.PREMIUM
        PlanType.UNDEFINED -> PlanTypeName.UNDEFINED
    }
