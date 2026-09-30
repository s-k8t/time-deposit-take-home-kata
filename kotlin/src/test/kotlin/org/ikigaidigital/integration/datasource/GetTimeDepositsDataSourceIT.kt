package org.ikigaidigital.integration.datasource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestFixtures.createTimeDepositToCalculate
import org.ikigaidigital.TestFixtures.createWithdrawalEntity
import org.ikigaidigital.TestFixtures.daysAgo
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.command.datasource.GetTimeDepositsDataSource
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

@DataR2dbcTest
@Import(TestcontainersConfiguration::class, Store::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class GetTimeDepositsDataSourceIT(
    private val sut: GetTimeDepositsDataSource,
    private val timeDepositRepository: TimeDepositRepository,
    private val withdrawalRepository: WithdrawalRepository,
) {
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
            val result = sut.fetchTimeDeposits()

            // verify
            assertThat(result).isEmpty()
        }

    @Test
    fun `should return all time deposits when time deposits exist`() =
        runTest {
            // prepare
            val basicDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val premiumDeposit =
                timeDepositRepository
                    .save(
                        createTimeDepositEntity(planType = PlanType.PREMIUM.code, balance = BigDecimal("9000.00"), startDate = daysAgo(90)),
                    ).let { timeDepositRepository.save(it.copy(balance = BigDecimal("10000.00"))) }
            withdrawalRepository.save(createWithdrawalEntity(timeDepositId = basicDeposit.id))
            val expected =
                listOf(
                    createTimeDepositToCalculate(id = basicDeposit.id, version = basicDeposit.version!!),
                    createTimeDepositToCalculate(
                        id = premiumDeposit.id,
                        planType = PlanType.PREMIUM.code,
                        balance = 10000.00,
                        startDate = daysAgo(90),
                        version = premiumDeposit.version!!,
                    ),
                )

            // execute
            val result = sut.fetchTimeDeposits()

            // verify
            assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
        }
}
