package org.ikigaidigital.api.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WithdrawalResponse(
    @get:JsonProperty("id")
    val id: WithdrawalId,
    @get:JsonProperty("amount")
    val amount: Amount,
    @get:JsonProperty("date")
    val date: WithdrawalDate,
)
