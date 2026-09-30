package org.ikigaidigital.store.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate

@Table("time_deposits")
data class TimeDepositEntity(
    @Id
    val id: Int = 0,
    val planType: String,
    val startDate: LocalDate,
    val balance: BigDecimal,
    val lastInterestDate: LocalDate? = null,
    @Version
    val version: Long? = null,
)
