package org.ikigaidigital.store.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("withdrawals")
data class WithdrawalEntity(
    @Id
    val id: Int = 0,
    val timeDepositId: Int,
    val amount: BigDecimal,
    val date: LocalDate,
    @Version
    val version: Long? = null,
)
