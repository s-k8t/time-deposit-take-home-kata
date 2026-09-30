package org.ikigaidigital.api

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.ikigaidigital.api.model.TimeDepositResponse

interface TimeDepositQueryFacade {
    suspend fun getTimeDeposits(
        @PositiveOrZero page: Int?,
        @Positive size: Int?,
    ): List<TimeDepositResponse>
}
