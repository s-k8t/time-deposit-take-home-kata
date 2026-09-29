package org.ikigaidigital.query.service

import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.springframework.stereotype.Service

@Service
internal class GetTimeDepositsService(
    private val getAllTimeDepositsDataSource: GetAllTimeDepositsDataSource,
) {
    suspend fun getTimeDeposits(): List<TimeDepositResponse> = getAllTimeDepositsDataSource.fetAllDeposits().map { it.toResponse() }
}
