package org.ikigaidigital.command.service

import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.command.datasource.GetTimeDepositsDueForInterestDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
internal class TimeDepositCommandService(
    private val getTimeDepositsDueForInterestDataSource: GetTimeDepositsDueForInterestDataSource,
    private val updateTimeDepositBalanceDataSource: UpdateTimeDepositBalanceDataSource,
    private val timeDepositCalculator: TimeDepositCalculator,
) {
    @Transactional
    suspend fun calculateBalances() {
        val today = LocalDate.now()
        val timeDepositsToCalculate = getTimeDepositsDueForInterestDataSource.fetchTimeDepositsDueForInterest(today.withDayOfMonth(1))
        val versionsById = timeDepositsToCalculate.associate { it.id to it.version }
        val timeDeposits = timeDepositsToCalculate.map { it.toTimeDeposit(today) }
        timeDepositCalculator.updateBalance(timeDeposits)
        updateTimeDepositBalanceDataSource.updateBalances(timeDeposits.map { it.toBalanceUpdate(versionsById.getValue(it.id), today) })
    }
}
