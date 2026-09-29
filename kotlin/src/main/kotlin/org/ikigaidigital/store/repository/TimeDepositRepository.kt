package org.ikigaidigital.store.repository

import org.ikigaidigital.store.entity.TimeDepositEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.math.BigDecimal

interface TimeDepositRepository : CoroutineCrudRepository<TimeDepositEntity, Int> {
    @Modifying
    @Query("UPDATE time_deposits SET balance = :balance, version = version + 1 WHERE id = :id AND version = :version")
    suspend fun updateBalance(
        id: Int,
        balance: BigDecimal,
        version: Long,
    ): Int
}
