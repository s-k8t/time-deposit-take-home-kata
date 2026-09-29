package org.ikigaidigital.store.repository

import org.ikigaidigital.store.entity.TimeDepositEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TimeDepositRepository : CoroutineCrudRepository<TimeDepositEntity, Int>
