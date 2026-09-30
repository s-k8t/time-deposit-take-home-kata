package org.ikigaidigital.query.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import org.ikigaidigital.TestFixtures.createTimeDepositResponse
import org.ikigaidigital.query.service.GetTimeDepositsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
        coEvery { getTimeDepositsService.getTimeDeposits(null, null) } returns listOf(expectedDeposit)

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
        coEvery { getTimeDepositsService.getTimeDeposits(null, null) } returns emptyList()

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

    @Test
    fun `should return requested page when page and size are given`() {
        // prepare
        val expectedDeposit = createTimeDepositResponse(id = 3)
        coEvery { getTimeDepositsService.getTimeDeposits(1, 2) } returns listOf(expectedDeposit)

        // execute
        val result =
            webTestClient
                .get()
                .uri("/v1/deposits?page=1&size=2")
                .exchange()

        // verify
        result
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1)
            .jsonPath("$[0].id")
            .isEqualTo(expectedDeposit.id.value)
        coVerify(exactly = 1) { getTimeDepositsService.getTimeDeposits(1, 2) }
    }

    @ParameterizedTest(name = "should return 400 rejecting {1} = {2} when query = \"{0}\"")
    @CsvSource(
        "'page=-1&size=2', page, -1",
        "size=0, size, 0",
        "size=-1, size, -1",
    )
    fun `should return bad request with rejected parameter and not query time deposits when pagination parameters are invalid`(
        query: String,
        expectedField: String,
        expectedRejectedValue: Int,
    ) {
        // execute
        val result =
            webTestClient
                .get()
                .uri("/v1/deposits?$query")
                .exchange()

        // verify
        result
            .expectStatus()
            .isBadRequest
            .expectBody()
            .jsonPath("$.message.length()")
            .isEqualTo(1)
            .jsonPath("$.message[0].field")
            .isEqualTo(expectedField)
            .jsonPath("$.message[0].rejectedValue")
            .isEqualTo(expectedRejectedValue)
            .jsonPath("$.message[0].message")
            .isNotEmpty
        coVerify(exactly = 0) { getTimeDepositsService.getTimeDeposits(any(), any()) }
    }
}
