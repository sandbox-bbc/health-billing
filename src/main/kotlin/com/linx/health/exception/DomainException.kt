package com.linx.health.exception

/**
 * Base exception for all domain-specific errors.
 * Subclasses define specific error scenarios.
 */
abstract class DomainException(
    message: String,
    val errorCode: String
) : RuntimeException(message)

/**
 * Thrown when a requested resource is not found.
 */
class NotFoundException(
    message: String,
    errorCode: String = "NOT_FOUND"
) : DomainException(message, errorCode)

/**
 * Thrown when a request conflicts with existing data.
 * E.g., duplicate NPI number, deleting entity with references.
 */
class ConflictException(
    message: String,
    errorCode: String = "CONFLICT"
) : DomainException(message, errorCode)

/**
 * Thrown when request data is invalid.
 */
class BadRequestException(
    message: String,
    errorCode: String = "BAD_REQUEST"
) : DomainException(message, errorCode)
