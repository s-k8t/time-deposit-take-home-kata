package org.ikigaidigital.api.model

data class WithdrawalResponse(
    val id: WithdrawalId,
    val amount: Amount,
    val date: WithdrawalDate,
)
