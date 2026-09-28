package org.ikigaidigital.models

enum class PlanType {
    BASIC,
    STUDENT,
    PREMIUM,
    UNDEFINED,
    ;

    companion object {
        fun fromString(value: String): PlanType = entries.find { it.name.lowercase() == value } ?: UNDEFINED
    }
}
