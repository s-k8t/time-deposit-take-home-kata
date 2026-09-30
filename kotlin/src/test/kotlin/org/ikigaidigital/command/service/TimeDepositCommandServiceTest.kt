package org.ikigaidigital.command.service

import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.ikigaidigital.TestFixtures.createBalanceUpdate
import org.ikigaidigital.TestFixtures.createTimeDeposit
import org.ikigaidigital.TestFixtures.createTimeDepositToCalculate
import org.ikigaidigital.TestFixtures.daysAgo
import org.ikigaidigital.TimeDeposit
import org.ikigaidigital.TimeDepositCalculator
import org.ikigaidigital.command.datasource.GetTimeDepositsDataSource
import org.ikigaidigital.command.datasource.UpdateTimeDepositBalanceDataSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TimeDepositCommandServiceTest(
    @param:MockK private val getTimeDepositsDataSource: GetTimeDepositsDataSource,
    @param:MockK private val updateTimeDepositBalanceDataSource: UpdateTimeDepositBalanceDataSource,
    @param:MockK private val timeDepositCalculator: TimeDepositCalculator,
    @param:InjectMockKs private val sut: TimeDepositCommandService,
) {
    @Test
    fun `should store balances calculated from days since start date with read versions when time deposits exist`() =
        runTest {
            // prepare
            val timeDeposit = createTimeDepositToCalculate(startDate = daysAgo(45), version = 3)
            val calculatedBalance = 1235.59
            coEvery { getTimeDepositsDataSource.fetchTimeDeposits() } returns listOf(timeDeposit)
            every {
                timeDepositCalculator.updateBalance(
                    listOf(
                        createTimeDeposit(
                            id = timeDeposit.id,
                            planType = timeDeposit.planType,
                            balance = timeDeposit.balance,
                            days = 45,
                        ),
                    ),
                )
            } answers { firstArg<List<TimeDeposit>>().forEach { it.balance = calculatedBalance } }
            coJustRun { updateTimeDepositBalanceDataSource.updateBalances(any()) }

            // execute
            sut.calculateBalances()

            // verify
            coVerify(exactly = 1) {
                updateTimeDepositBalanceDataSource.updateBalances(
                    listOf(createBalanceUpdate(id = timeDeposit.id, balance = calculatedBalance, version = timeDeposit.version)),
                )
            }
        }

    @Test
    fun `should store no balances when no time deposits exist`() =
        runTest {
            // prepare
            coEvery { getTimeDepositsDataSource.fetchTimeDeposits() } returns emptyList()
            every { timeDepositCalculator.updateBalance(emptyList()) } returns Unit
            coJustRun { updateTimeDepositBalanceDataSource.updateBalances(any()) }

            // execute
            sut.calculateBalances()

            // verify
            coVerify(exactly = 1) { updateTimeDepositBalanceDataSource.updateBalances(emptyList()) }
        }
}
