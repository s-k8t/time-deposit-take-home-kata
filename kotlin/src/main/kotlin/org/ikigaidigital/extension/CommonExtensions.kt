package org.ikigaidigital.extension

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun Double.roundToCents(): Double = BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble()

internal fun LocalDate.daysUntil(date: LocalDate): Int = ChronoUnit.DAYS.between(this, date).toInt()
