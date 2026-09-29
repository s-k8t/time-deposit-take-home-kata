package org.ikigaidigital.api

import org.ikigaidigital.api.model.TimeDepositResponse

interface TimeDepositQueryFacade {
    suspend fun getAllTimeDeposits(): List<TimeDepositResponse>
}
