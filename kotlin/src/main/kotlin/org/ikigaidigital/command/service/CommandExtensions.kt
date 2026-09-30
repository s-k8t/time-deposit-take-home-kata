package org.ikigaidigital.command.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.command.datasource.GetTimeDepositsDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.ikigaidigital.extension.daysUntil
import java.time.LocalDate

internal fun GetTimeDepositsDataSource.Output.toTimeDeposit(today: LocalDate) =
    TimeDeposit(
        id = id,
        planType = planType,
        balance = balance,
        days = startDate.daysUntil(today),
    )

internal fun TimeDeposit.toBalanceUpdate(version: Long) =
    UpdateTimeDepositBalanceDataSource.Input(
        id = id,
        balance = balance,
        version = version,
    )
