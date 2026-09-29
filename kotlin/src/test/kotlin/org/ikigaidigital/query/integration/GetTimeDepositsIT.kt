package org.ikigaidigital.query.integration

import kotlinx.coroutines.test.runTest
import org.ikigaidigital.TestFixtures.createTimeDepositEntity
import org.ikigaidigital.TestFixtures.createWithdrawalEntity
import org.ikigaidigital.TestcontainersConfiguration
import org.ikigaidigital.domain.PlanType
import org.ikigaidigital.store.repository.TimeDepositRepository
import org.ikigaidigital.store.repository.WithdrawalRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class GetTimeDepositsIT(
    private val webTestClient: WebTestClient,
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
    fun `should return all stored time deposits with withdrawals when get endpoint is called`() =
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

            // execute
            val result =
                webTestClient
                    .get()
                    .uri("/v1/deposits")
                    .exchange()

            // verify
            result
                .expectStatus()
                .isOk
                .expectBody()
                .json(
                    """
                    [
                      {
                        "id": ${basicDeposit.id},
                        "planType": "BASIC",
                        "balance": 1234.56,
                        "days": 45,
                        "withdrawals": [
                          { "id": ${earlierWithdrawal.id}, "amount": 50.25, "date": "2026-08-01" },
                          { "id": ${laterWithdrawal.id}, "amount": 100.0, "date": "2026-09-01" }
                        ]
                      },
                      {
                        "id": ${premiumDeposit.id},
                        "planType": "PREMIUM",
                        "balance": 10000.0,
                        "days": 90,
                        "withdrawals": []
                      }
                    ]
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
        }
}
