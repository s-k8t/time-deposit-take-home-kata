package org.ikigaidigital.query.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.ikigaidigital.api.TimeDepositQueryFacade
import org.ikigaidigital.api.model.TimeDepositResponse
import org.ikigaidigital.query.service.GetTimeDepositsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/deposits")
@Tag(name = "Time deposits")
internal class TimeDepositQueryController(
    private val getTimeDepositsService: GetTimeDepositsService,
) : TimeDepositQueryFacade {
    @GetMapping
    @Operation(
        summary = "Get time deposits",
        description = "This API is used to get all time deposits",
        responses = [
            ApiResponse(
                responseCode = "200",
            ),
        ],
    )
    override suspend fun getTimeDeposits(
        @RequestParam page: Int?,
        @RequestParam size: Int?,
    ): List<TimeDepositResponse> = getTimeDepositsService.getTimeDeposits(page, size)
}
