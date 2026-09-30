package org.ikigaidigital.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class ControllerExceptionHandler {
    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(ex: WebExchangeBindException): Map<String, Any> {
        val validationErrors =
            ex.bindingResult.fieldErrors.map {
                mapOf(
                    "field" to it.field,
                    "rejectedValue" to it.rejectedValue,
                    "message" to it.defaultMessage,
                )
            }
        return mapOf("message" to validationErrors)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleParameterValidationException(ex: HandlerMethodValidationException): Map<String, Any> {
        val validationErrors =
            ex.parameterValidationResults.flatMap { result ->
                result.resolvableErrors.map {
                    mapOf(
                        "field" to result.methodParameter.parameterName,
                        "rejectedValue" to result.argument,
                        "message" to it.defaultMessage,
                    )
                }
            }
        return mapOf("message" to validationErrors)
    }
}
