package org.ikigaidigital.integration.datasource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createBalanceUpdate
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestFixtures.daysAgo
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.ikigaidigital.model.PlanType
import org.ikigaidigital.store.Store
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import java.math.BigDecimal

@DataR2dbcTest
@Import(TestcontainersConfiguration::class, Store::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class UpdateTimeDepositBalanceDataSourceIT(
    private val sut: UpdateTimeDepositBalanceDataSource,
    private val timeDepositRepository: TimeDepositRepository,
) {
    @AfterEach
    fun cleanUp() =
        runTest {
            timeDepositRepository.deleteAll()
        }

    @Test
    fun `should store new balance and last interest date and increment version when balance update is given`() =
        runTest {
            // prepare
            val basicDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val premiumDeposit =
                timeDepositRepository.save(
                    createTimeDepositEntity(planType = PlanType.PREMIUM.code, balance = BigDecimal("10000.00"), startDate = daysAgo(90)),
                )

            // execute
            sut.updateBalances(
                listOf(
                    createBalanceUpdate(
                        id = basicDeposit.id,
                        balance = 1235.59,
                        version = basicDeposit.version!!,
                        lastInterestDate = daysAgo(1),
                    ),
                ),
            )

            // verify
            val updatedBasicDeposit = timeDepositRepository.findById(basicDeposit.id)!!
            val untouchedPremiumDeposit = timeDepositRepository.findById(premiumDeposit.id)!!
            assertThat(updatedBasicDeposit.balance).isEqualByComparingTo(BigDecimal("1235.59"))
            assertThat(updatedBasicDeposit.lastInterestDate).isEqualTo(daysAgo(1))
            assertThat(updatedBasicDeposit.version).isEqualTo(basicDeposit.version + 1)
            assertThat(untouchedPremiumDeposit).isEqualTo(premiumDeposit)
        }

    @Test
    fun `should keep concurrent change when time deposit changed after it was read`() =
        runTest {
            // prepare
            val readDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val concurrentlyChangedDeposit = timeDepositRepository.save(readDeposit.copy(balance = BigDecimal("50.00")))

            // execute
            sut.updateBalances(listOf(createBalanceUpdate(id = readDeposit.id, balance = 1235.59, version = readDeposit.version!!)))

            // verify
            assertThat(timeDepositRepository.findById(readDeposit.id)).isEqualTo(concurrentlyChangedDeposit)
        }

    @Test
    fun `should leave time deposits unchanged when no balance updates are given`() =
        runTest {
            // prepare
            val basicDeposit = timeDepositRepository.save(createTimeDepositEntity())

            // execute
            sut.updateBalances(emptyList())

            // verify
            assertThat(timeDepositRepository.findById(basicDeposit.id)).isEqualTo(basicDeposit)
        }
}
