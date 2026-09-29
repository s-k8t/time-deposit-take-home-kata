package org.ikigaidigital.api.model

import java.time.LocalDate

@JvmInline
value class WithdrawalDate(
    val value: LocalDate,
) {
    override fun toString() = value.toString()
}
