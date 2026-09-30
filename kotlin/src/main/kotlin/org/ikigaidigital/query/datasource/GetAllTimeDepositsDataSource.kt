package org.ikigaidigital.query.datasource

import java.time.LocalDate

internal interface GetAllTimeDepositsDataSource {
    data class Output(
        val id: Int,
        val planType: String,
        val balance: Double,
        val startDate: LocalDate,
        val withdrawals: List<Withdrawal>,
    ) {
        data class Withdrawal(
            val id: Int,
            val amount: Double,
            val date: LocalDate,
        )
    }

    data class Page(
        val number: Int,
        val size: Int,
    )

    suspend fun fetchAllDeposits(page: Page?): List<Output>
}
