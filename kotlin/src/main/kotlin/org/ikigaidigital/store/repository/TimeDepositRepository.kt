package org.ikigaidigital.store.repository

import kotlinx.coroutines.flow.Flow
import org.ikigaidigital.store.entity.TimeDepositEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.math.BigDecimal
import java.time.LocalDate

interface TimeDepositRepository : CoroutineCrudRepository<TimeDepositEntity, Int> {
    fun findAllBy(pageable: Pageable): Flow<TimeDepositEntity>

    @Query("SELECT * FROM time_deposits WHERE last_interest_date IS NULL OR last_interest_date < :periodStart")
    fun findAllDueForInterest(periodStart: LocalDate): Flow<TimeDepositEntity>

    @Modifying
    @Query(
        "UPDATE time_deposits SET balance = :balance, last_interest_date = :lastInterestDate, version = version + 1 " +
            "WHERE id = :id AND version = :version",
    )
    suspend fun updateBalance(
        id: Int,
        balance: BigDecimal,
        lastInterestDate: LocalDate,
        version: Long,
    ): Int
}
