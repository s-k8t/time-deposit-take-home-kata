package org.ikigaidigital.api.model

@JvmInline
value class Day(
    val value: Int,
) {
    override fun toString() = value.toString()
}
