package org.ikigaidigital.integration

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class UpdateTimeDepositBalancesIT(
    private val webTestClient: WebTestClient,
    private val timeDepositRepository: TimeDepositRepository,
) {
    @AfterEach
    fun cleanUp() =
        runTest {
            timeDepositRepository.deleteAll()
        }

    @ParameterizedTest(name = "should store balance {3} when plan type = \"{0}\", days = {1} and balance = {2}")
    @CsvSource(
        "basic, 30, 1234.56, 1234.56",
        "basic, 31, 1234.56, 1235.59",
        "student, 30, 5000.00, 5000.00",
        "student, 365, 5000.00, 5012.50",
        "student, 366, 5000.00, 5000.00",
        "premium, 45, 10000.00, 10000.00",
        "premium, 46, 10000.00, 10041.67",
        "other, 100, 500.00, 500.00",
    )
    fun `should store balance with monthly interest applied when balances endpoint is called`(
        planType: String,
        days: Int,
        balance: BigDecimal,
        expectedBalance: BigDecimal,
    ) = runTest {
        // prepare
        val timeDeposit = timeDepositRepository.save(createTimeDepositEntity(planType = planType, balance = balance, days = days))

        // execute
        val result =
            webTestClient
                .post()
                .uri("/v1/deposits/balances")
                .exchange()

        // verify
        result.expectStatus().isNoContent
        assertThat(timeDepositRepository.findById(timeDeposit.id)!!.balance).isEqualByComparingTo(expectedBalance)
    }

    @Test
    fun `should keep all balances unchanged when storing any calculated balance fails`() =
        runTest {
            // prepare
            val validTimeDeposit = timeDepositRepository.save(createTimeDepositEntity())
            val overflowingTimeDeposit =
                timeDepositRepository.save(createTimeDepositEntity(balance = BigDecimal("99999999999999999.99")))

            // execute
            val result =
                webTestClient
                    .post()
                    .uri("/v1/deposits/balances")
                    .exchange()

            // verify
            result.expectStatus().is5xxServerError
            assertThat(timeDepositRepository.findById(validTimeDeposit.id)).isEqualTo(validTimeDeposit)
            assertThat(timeDepositRepository.findById(overflowingTimeDeposit.id)).isEqualTo(overflowingTimeDeposit)
        }
}
