package org.ikigaidigital.query.datasource

import java.time.LocalDate

internal interface GetAllTimeDepositsDataSource {
    data class Output(
        val id: Int,
        val planType: String,
        val balance: Double,
        val days: Int,
        val withdrawals: List<Withdrawal>,
    ) {
        data class Withdrawal(
            val id: Int,
            val amount: Double,
            val date: LocalDate,
        )
    }

    suspend fun fetchAllDeposits(): List<Output>
}
