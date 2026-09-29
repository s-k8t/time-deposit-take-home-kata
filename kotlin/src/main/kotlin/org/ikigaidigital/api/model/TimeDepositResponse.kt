package org.ikigaidigital.api.model

data class TimeDepositResponse(
    val id: TimeDepositId,
    val planType: PlanTypeName,
    val balance: Amount,
    val days: Day,
    val withdrawals: List<WithdrawalResponse>,
)
