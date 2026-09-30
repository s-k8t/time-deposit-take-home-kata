package org.ikigaidigital.command.datasource

import java.time.LocalDate

internal interface GetTimeDepositsDataSource {
    data class Output(
        val id: Int,
        val planType: String,
        val balance: Double,
        val startDate: LocalDate,
        val version: Long,
    )

    suspend fun fetchTimeDeposits(): List<Output>
}
