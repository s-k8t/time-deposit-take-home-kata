package org.ikigaidigital.store.repository

import kotlinx.coroutines.flow.Flow
import org.ikigaidigital.store.entity.WithdrawalEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WithdrawalRepository : CoroutineCrudRepository<WithdrawalEntity, Int> {
    fun findAllByTimeDepositIdIn(timeDepositIds: Collection<Int>): Flow<WithdrawalEntity>
}
