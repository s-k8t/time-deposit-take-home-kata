package org.ikigaidigital.store.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("time_deposits")
data class TimeDepositEntity(
    @Id
    val id: Int = 0,
    val planType: String,
    val days: Int,
    val balance: BigDecimal,
    @Version
    val version: Long? = null,
)
