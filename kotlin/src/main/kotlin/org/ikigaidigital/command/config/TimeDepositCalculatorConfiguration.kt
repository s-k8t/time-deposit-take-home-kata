package org.ikigaidigital.command.config

import org.ikigaidigital.TimeDepositCalculator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class TimeDepositCalculatorConfiguration {
    @Bean
    fun timeDepositCalculator() = TimeDepositCalculator()
}
