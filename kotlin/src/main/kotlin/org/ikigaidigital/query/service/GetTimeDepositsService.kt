package org.ikigaidigital.query.service

import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
internal class GetTimeDepositsService(
    private val getAllTimeDepositsDataSource: GetAllTimeDepositsDataSource,
) {
    suspend fun getTimeDeposits(
        page: Int?,
        size: Int?,
    ): List<TimeDepositResponse> {
        val today = LocalDate.now()
        val requestedPage = size?.let { GetAllTimeDepositsDataSource.Page(number = page ?: 0, size = it) }
        return getAllTimeDepositsDataSource.fetchAllDeposits(requestedPage).map { it.toResponse(today) }
    }
}
