package org.ikigaidigital.api.model

@JvmInline
value class WithdrawalId(
    val value: Int,
) {
    override fun toString() = value.toString()
}
