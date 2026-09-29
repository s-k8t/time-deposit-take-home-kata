package org.ikigaidigital.api

interface TimeDepositCommandFacade {
    suspend fun calculateBalances()
}
