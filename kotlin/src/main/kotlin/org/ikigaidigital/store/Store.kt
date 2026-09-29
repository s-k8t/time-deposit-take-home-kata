package org.ikigaidigital.store

import kotlinx.coroutines.flow.toList
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.ikigaidigital.store.entity.TimeDepositEntity
import org.ikigaidigital.store.entity.WithdrawalEntity
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.ikigaidigital.store.repository.WithdrawalRepository
import org.springframework.stereotype.Component

@Component
internal class Store(
    private val timeDepositRepository: TimeDepositRepository,
    private val withdrawalRepository: WithdrawalRepository,
) : GetAllTimeDepositsDataSource {
    override suspend fun fetAllDeposits(): List<GetAllTimeDepositsDataSource.Output> {
        val withdrawalsByTimeDepositId = withdrawalRepository.findAll().toList().groupBy { it.timeDepositId }
        return timeDepositRepository
            .findAll()
            .toList()
            .sortedBy { it.id }
            .map { it.toOutput(withdrawalsByTimeDepositId[it.id].orEmpty()) }
    }

    private fun TimeDepositEntity.toOutput(withdrawals: List<WithdrawalEntity>) =
        GetAllTimeDepositsDataSource.Output(
            id = id,
            planType = planType,
            balance = balance.toDouble(),
            days = days,
            withdrawals = withdrawals.sortedBy { it.date }.map { it.toOutput() },
        )

    private fun WithdrawalEntity.toOutput() =
        GetAllTimeDepositsDataSource.Output.Withdrawal(
            id = id,
            amount = amount.toDouble(),
            date = date,
        )
}
