package org.ikigaidigital.command.datasource

internal interface GetTimeDepositsDataSource {
    data class Output(
        val id: Int,
        val planType: String,
        val balance: Double,
        val days: Int,
        val version: Long,
    )

    suspend fun fetchTimeDeposits(): List<Output>
}
