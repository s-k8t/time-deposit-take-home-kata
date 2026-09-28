package org.ikigaidigital.extensions

import java.math.BigDecimal
import java.math.RoundingMode

internal fun Double.roundToCents(): Double = BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble()
