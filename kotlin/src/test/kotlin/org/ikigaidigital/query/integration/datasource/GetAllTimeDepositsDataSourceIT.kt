package org.ikigaidigital.query.integration.datasource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestFixtures.createTimeDepositOutput
import org.ikigaidigital.TestFixtures.createWithdrawalEntity
import org.ikigaidigital.TestFixtures.createWithdrawalOutput
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.domain.PlanType
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
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
internal class GetAllTimeDepositsDataSourceIT(
    private val sut: GetAllTimeDepositsDataSource,
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
            val result = sut.fetAllDeposits()

            // verify
            assertThat(result).isEmpty()
        }

    @Test
    fun `should return time deposits with their withdrawals ordered by date when time deposits exist`() =
        runTest {
            // prepare
            val basicDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val premiumDeposit =
                timeDepositRepository.save(
                    createTimeDepositEntity(planType = PlanType.PREMIUM.code, balance = BigDecimal("10000.00"), days = 90),
                )
            val laterWithdrawal = withdrawalRepository.save(createWithdrawalEntity(timeDepositId = basicDeposit.id))
            val earlierWithdrawal =
                withdrawalRepository.save(
                    createWithdrawalEntity(
                        timeDepositId = basicDeposit.id,
                        amount = BigDecimal("50.25"),
                        date = LocalDate.of(2026, 8, 1),
                    ),
                )
            val expected =
                listOf(
                    createTimeDepositOutput(
                        id = basicDeposit.id,
                        withdrawals =
                            listOf(
                                createWithdrawalOutput(id = earlierWithdrawal.id, amount = 50.25, date = LocalDate.of(2026, 8, 1)),
                                createWithdrawalOutput(id = laterWithdrawal.id),
                            ),
                    ),
                    createTimeDepositOutput(
                        id = premiumDeposit.id,
                        planType = PlanType.PREMIUM.code,
                        balance = 10000.00,
                        days = 90,
                        withdrawals = emptyList(),
                    ),
                )

            // execute
            val result = sut.fetAllDeposits()

            // verify
            assertThat(result).isEqualTo(expected)
        }
}
