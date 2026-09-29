package org.ikigaidigital.command.service

import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.command.datasource.GetTimeDepositsDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource

internal fun GetTimeDepositsDataSource.Output.toTimeDeposit() =
    TimeDeposit(
        id = id,
        planType = planType,
        balance = balance,
        days = days,
    )

internal fun TimeDeposit.toBalanceUpdate(version: Long) =
    UpdateTimeDepositBalanceDataSource.Input(
        id = id,
        balance = balance,
        version = version,
    )
