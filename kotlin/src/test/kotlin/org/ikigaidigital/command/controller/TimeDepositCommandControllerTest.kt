package org.ikigaidigital.command.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coJustRun
import io.mockk.coVerify
import org.ikigaidigital.command.service.TimeDepositCommandService
import org.junit.jupiter.api.Test
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(TimeDepositCommandController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class TimeDepositCommandControllerTest(
    private val webTestClient: WebTestClient,
    @MockkBean private val timeDepositCommandService: TimeDepositCommandService,
) {
    @Test
    fun `should calculate balances and return no content when balances endpoint is called`() {
        // prepare
        coJustRun { timeDepositCommandService.calculateBalances() }

        // execute
        val result =
            webTestClient
                .post()
                .uri("/v1/deposits/balances")
                .exchange()

        // verify
        result
            .expectStatus()
            .isNoContent
            .expectBody()
            .isEmpty
        coVerify(exactly = 1) { timeDepositCommandService.calculateBalances() }
    }
}
