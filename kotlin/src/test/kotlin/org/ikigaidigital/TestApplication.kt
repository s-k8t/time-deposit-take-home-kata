package org.ikigaidigital

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<TimeDepositApplication>().with(TestcontainersConfiguration::class).run(*args)
}
