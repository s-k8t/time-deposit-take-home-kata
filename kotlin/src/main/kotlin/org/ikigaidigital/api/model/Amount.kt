package org.ikigaidigital.api.model

@JvmInline
value class Amount(
    val value: Double,
) {
    override fun toString() = value.toString()
}
