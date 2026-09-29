package org.ikigaidigital.store.repository

import org.ikigaidigital.store.entity.WithdrawalEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface WithdrawalRepository : CoroutineCrudRepository<WithdrawalEntity, Int>
