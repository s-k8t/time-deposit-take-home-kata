package org.ikigaidigital.command.datasource

internal interface UpdateTimeDepositBalanceDataSource {
    data class Input(
        val id: Int,
        val balance: Double,
        val version: Long,
    )

    suspend fun updateBalances(inputs: List<Input>)
}
