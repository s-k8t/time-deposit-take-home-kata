package org.ikigaidigital.integration.datasource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestFixtures.createTimeDepositToCalculate
import org.ikigaidigital.TestFixtures.createWithdrawalEntity
import org.ikigaidigital.TestFixtures.daysAgo
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.command.datasource.GetTimeDepositsDueForInterestDataSource
import org.ikigaidigital.model.PlanType
import org.ikigaidigital.store.Store
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.ikigaidigital.store.repository.WithdrawalRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import java.math.BigDecimal
import java.time.LocalDate

@DataR2dbcTest
@Import(TestcontainersConfiguration::class, Store::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class GetTimeDepositsDueForInterestDataSourceIT(
    private val sut: GetTimeDepositsDueForInterestDataSource,
    private val timeDepositRepository: TimeDepositRepository,
    private val withdrawalRepository: WithdrawalRepository,
) {
    private val periodStart = LocalDate.of(2026, 9, 1)

    @AfterEach
    fun cleanUp() =
        runTest {
            withdrawalRepository.deleteAll()
            timeDepositRepository.deleteAll()
        }

    @Test
    fun `should return empty list when no time deposits exist`() =
        runTest {
            // execute
            val result = sut.fetchTimeDepositsDueForInterest(periodStart)

            // verify
            assertThat(result).isEmpty()
        }

    @Test
    fun `should return time deposits never credited or last credited before period start when time deposits exist`() =
        runTest {
            // prepare
            val neverCreditedDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val creditedLastMonthDeposit =
                timeDepositRepository
                    .save(
                        createTimeDepositEntity(
                            planType = PlanType.PREMIUM.code,
                            balance = BigDecimal("9000.00"),
                            startDate = daysAgo(90),
                            lastInterestDate = periodStart.minusDays(1),
                        ),
                    ).let { timeDepositRepository.save(it.copy(balance = BigDecimal("10000.00"))) }
            val missedMonthDeposit = timeDepositRepository.save(createTimeDepositEntity(lastInterestDate = periodStart.minusMonths(2)))
            timeDepositRepository.save(createTimeDepositEntity(lastInterestDate = periodStart))
            timeDepositRepository.save(createTimeDepositEntity(lastInterestDate = periodStart.plusDays(14)))
            withdrawalRepository.save(createWithdrawalEntity(timeDepositId = neverCreditedDeposit.id))
            val expected =
                listOf(
                    createTimeDepositToCalculate(id = neverCreditedDeposit.id, version = neverCreditedDeposit.version!!),
                    createTimeDepositToCalculate(
                        id = creditedLastMonthDeposit.id,
                        planType = PlanType.PREMIUM.code,
                        balance = 10000.00,
                        startDate = daysAgo(90),
                        version = creditedLastMonthDeposit.version!!,
                    ),
                    createTimeDepositToCalculate(id = missedMonthDeposit.id, version = missedMonthDeposit.version!!),
                )

            // execute
            val result = sut.fetchTimeDepositsDueForInterest(periodStart)

            // verify
            assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
        }
}
