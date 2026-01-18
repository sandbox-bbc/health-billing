package com.linx.health.exception

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton

/**
 * Standard error response format.
 */
@Serdeable
data class ErrorResponse(
    val message: String,
    val errorCode: String
)

/**
 * Unified exception handler for all domain exceptions.
 * Maps exception types to appropriate HTTP status codes.
 */
@Produces
@Singleton
@Requires(classes = [DomainException::class, ExceptionHandler::class])
class DomainExceptionHandler : ExceptionHandler<DomainException, HttpResponse<ErrorResponse>> {
    
    override fun handle(request: HttpRequest<*>, exception: DomainException): HttpResponse<ErrorResponse> {
        val status = when (exception) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is ConflictException -> HttpStatus.CONFLICT
            is BadRequestException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return HttpResponse.status<ErrorResponse>(status).body(
            ErrorResponse(
                message = exception.message ?: "An error occurred",
                errorCode = exception.errorCode
            )
        )
    }
}
