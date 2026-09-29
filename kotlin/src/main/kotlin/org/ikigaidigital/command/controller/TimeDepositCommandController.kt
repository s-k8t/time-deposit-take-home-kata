package org.ikigaidigital.command.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.ikigaidigital.api.TimeDepositCommandFacade
import org.ikigaidigital.command.service.TimeDepositCommandService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/deposits")
@Tag(name = "Time deposits")
internal class TimeDepositCommandController(
    private val timeDepositCommandService: TimeDepositCommandService,
) : TimeDepositCommandFacade {
    @PostMapping("/balances")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Calculate time deposit balances",
        description = "This API is used to apply monthly interest to the balances of all time deposits",
        responses = [
            ApiResponse(
                responseCode = "204",
            ),
        ],
    )
    override suspend fun calculateBalances() = timeDepositCommandService.calculateBalances()
}
