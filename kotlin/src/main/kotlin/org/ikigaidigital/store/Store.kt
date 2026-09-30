package org.ikigaidigital.store

import kotlinx.coroutines.flow.toList
import org.ikigaidigital.command.datasource.GetTimeDepositsDueForInterestDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.ikigaidigital.store.entity.TimeDepositEntity
import org.ikigaidigital.store.entity.WithdrawalEntity
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.ikigaidigital.store.repository.WithdrawalRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
internal class Store(
    private val timeDepositRepository: TimeDepositRepository,
    private val withdrawalRepository: WithdrawalRepository,
) : GetAllTimeDepositsDataSource,
    GetTimeDepositsDueForInterestDataSource,
    UpdateTimeDepositBalanceDataSource {
    override suspend fun fetchAllDeposits(page: GetAllTimeDepositsDataSource.Page?): List<GetAllTimeDepositsDataSource.Output> {
        val sortById = Sort.by("id")
        val pageable = page?.let { PageRequest.of(it.number, it.size, sortById) } ?: Pageable.unpaged(sortById)
        val timeDeposits = timeDepositRepository.findAllBy(pageable).toList()
        val withdrawals =
            when {
                timeDeposits.isEmpty() -> emptyList()
                page == null -> withdrawalRepository.findAll().toList()
                else -> withdrawalRepository.findAllByTimeDepositIdIn(timeDeposits.map { it.id }).toList()
            }
        val withdrawalsByTimeDepositId = withdrawals.groupBy { it.timeDepositId }
        return timeDeposits.map { it.toOutput(withdrawalsByTimeDepositId[it.id].orEmpty()) }
    }

    override suspend fun fetchTimeDepositsDueForInterest(periodStart: LocalDate): List<GetTimeDepositsDueForInterestDataSource.Output> =
        timeDepositRepository
            .findAllDueForInterest(periodStart)
            .toList()
            .map { it.toTimeDepositOutput() }

    override suspend fun updateBalances(inputs: List<UpdateTimeDepositBalanceDataSource.Input>) {
        inputs.forEach { input ->
            timeDepositRepository.updateBalance(input.id, BigDecimal.valueOf(input.balance), input.lastInterestDate, input.version)
        }
    }

    private fun TimeDepositEntity.toOutput(withdrawals: List<WithdrawalEntity>) =
        GetAllTimeDepositsDataSource.Output(
            id = id,
            planType = planType,
            balance = balance.toDouble(),
            startDate = startDate,
            withdrawals = withdrawals.sortedBy { it.date }.map { it.toOutput() },
        )

    private fun WithdrawalEntity.toOutput() =
        GetAllTimeDepositsDataSource.Output.Withdrawal(
            id = id,
            amount = amount.toDouble(),
            date = date,
        )

    private fun TimeDepositEntity.toTimeDepositOutput() =
        GetTimeDepositsDueForInterestDataSource.Output(
            id = id,
            planType = planType,
            balance = balance.toDouble(),
            startDate = startDate,
            version = checkNotNull(version),
        )
}
