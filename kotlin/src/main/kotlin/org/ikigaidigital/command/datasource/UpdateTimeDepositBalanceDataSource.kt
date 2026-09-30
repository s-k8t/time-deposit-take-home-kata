package org.ikigaidigital.command.datasource

import java.time.LocalDate

internal interface UpdateTimeDepositBalanceDataSource {
    data class Input(
        val id: Int,
        val balance: Double,
        val version: Long,
        val lastInterestDate: LocalDate,
    )

    suspend fun updateBalances(inputs: List<Input>)
}
