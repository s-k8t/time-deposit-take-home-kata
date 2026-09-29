package org.ikigaidigital.query.service

import org.ikigaidigital.api.model.Amount
import org.ikigaidigital.api.model.Day
import org.ikigaidigital.api.model.PlanTypeName
import org.ikigaidigital.api.model.TimeDepositId
import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.api.model.WithdrawalDate
import org.ikigaidigital.api.model.WithdrawalId
import org.ikigaidigital.api.model.WithdrawalResponse
import org.ikigaidigital.domain.PlanType
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource

internal fun GetAllTimeDepositsDataSource.Output.toResponse() =
    TimeDepositResponse(
        id = TimeDepositId(id),
        planType = PlanType.fromString(planType).toPlanTypeName(),
        balance = Amount(balance),
        days = Day(days),
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
