package org.ikigaidigital.domain

enum class PlanType(
    val code: String,
) {
    BASIC("basic"),
    STUDENT("student"),
    PREMIUM("premium"),
    UNDEFINED("undefined"),
    ;

    companion object {
        fun fromString(value: String): PlanType = entries.find { it.code == value } ?: UNDEFINED
    }
}
