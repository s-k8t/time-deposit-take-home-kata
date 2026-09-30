package org.ikigaidigital.query.service

import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
internal class GetTimeDepositsService(
    private val getAllTimeDepositsDataSource: GetAllTimeDepositsDataSource,
) {
    suspend fun getTimeDeposits(): List<TimeDepositResponse> {
        val today = LocalDate.now()
        return getAllTimeDepositsDataSource.fetchAllDeposits().map { it.toResponse(today) }
    }
}
