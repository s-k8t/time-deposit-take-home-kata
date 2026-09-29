package org.ikigaidigital.query.service

import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDepositOutput
import org.ikigaidigital.TestFixtures.createTimeDepositResponse
import org.ikigaidigital.api.model.PlanTypeName
import org.ikigaidigital.query.datasource.GetAllTimeDepositsDataSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
internal class GetTimeDepositsServiceTest(
    @param:MockK private val getAllTimeDepositsDataSource: GetAllTimeDepositsDataSource,
    @param:InjectMockKs private val sut: GetTimeDepositsService,
) {
    @Test
    fun `should map all time deposits with withdrawals to responses when time deposits exist`() =
        runTest {
            // prepare
            coEvery { getAllTimeDepositsDataSource.fetAllDeposits() } returns listOf(createTimeDepositOutput())
            val expected = listOf(createTimeDepositResponse())

            // execute
            val result = sut.getTimeDeposits()

            // verify
            assertThat(result).isEqualTo(expected)
        }

    @ParameterizedTest(name = "should map plan type to {1} when stored plan type = \"{0}\"")
    @CsvSource(
        "basic, BASIC",
        "student, STUDENT",
        "premium, PREMIUM",
        "other, UNDEFINED",
        "BASIC, UNDEFINED",
    )
    fun `should map stored plan type to plan type name when time deposit exists`(
        storedPlanType: String,
        expectedPlanTypeName: PlanTypeName,
    ) = runTest {
        // prepare
        coEvery { getAllTimeDepositsDataSource.fetAllDeposits() } returns listOf(createTimeDepositOutput(planType = storedPlanType))

        // execute
        val result = sut.getTimeDeposits()

        // verify
        assertThat(result.single().planType).isEqualTo(expectedPlanTypeName)
    }

    @Test
    fun `should return empty list when no time deposits exist`() =
        runTest {
            // prepare
            coEvery { getAllTimeDepositsDataSource.fetAllDeposits() } returns emptyList()

            // execute
            val result = sut.getTimeDeposits()

            // verify
            assertThat(result).isEmpty()
        }
}
