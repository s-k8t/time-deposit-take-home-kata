package org.ikigaidigital.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TimeDepositResponse(
    @get:JsonProperty("id")
    val id: TimeDepositId,
    val planType: PlanTypeName,
    @get:JsonProperty("balance")
    val balance: Amount,
    @get:JsonProperty("days")
    val days: Day,
    val withdrawals: List<WithdrawalResponse>,
)
