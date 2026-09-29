package org.ikigaidigital.query.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import org.ikigaidigital.TestFixtures.createTimeDepositResponse
import org.ikigaidigital.query.service.GetTimeDepositsService
import org.junit.jupiter.api.Test
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(TimeDepositQueryController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class TimeDepositQueryControllerTest(
    private val webTestClient: WebTestClient,
    @MockkBean private val getTimeDepositsService: GetTimeDepositsService,
) {
    @Test
    fun `should return time deposits`() {
        // prepare
        val expectedDeposit = createTimeDepositResponse()
        coEvery { getTimeDepositsService.getTimeDeposits() } returns listOf(expectedDeposit)

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
                    "id": ${expectedDeposit.id},
                    "planType": "${expectedDeposit.planType}",
                    "balance": ${expectedDeposit.balance},
                    "days": ${expectedDeposit.days},
                    "withdrawals": [
                      { 
                      "id": ${expectedDeposit.withdrawals.first().id}, 
                      "amount": ${expectedDeposit.withdrawals.first().amount}, 
                      "date": "${expectedDeposit.withdrawals.first().date}" }
                    ]
                  }
                ]
                """.trimIndent(),
                JsonCompareMode.STRICT,
            )
    }

    @Test
    fun `should return empty list when no time deposits exist`() {
        // prepare
        coEvery { getTimeDepositsService.getTimeDeposits() } returns emptyList()

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
            .json("[]", JsonCompareMode.STRICT)
    }
}
