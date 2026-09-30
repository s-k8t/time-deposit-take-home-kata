package org.ikigaidigital.command.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.command.datasource.GetTimeDepositsDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
internal class TimeDepositCommandService(
    private val getTimeDepositsDataSource: GetTimeDepositsDataSource,
    private val updateTimeDepositBalanceDataSource: UpdateTimeDepositBalanceDataSource,
    private val timeDepositCalculator: TimeDepositCalculator,
) {
    @Transactional
    suspend fun calculateBalances() {
        val today = LocalDate.now()
        val timeDepositsToCalculate = getTimeDepositsDataSource.fetchTimeDeposits()
        val versionsById = timeDepositsToCalculate.associate { it.id to it.version }
        val timeDeposits = timeDepositsToCalculate.map { it.toTimeDeposit(today) }
        timeDepositCalculator.updateBalance(timeDeposits)
        updateTimeDepositBalanceDataSource.updateBalances(timeDeposits.map { it.toBalanceUpdate(versionsById.getValue(it.id)) })
    }
}
