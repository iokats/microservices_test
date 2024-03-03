package se.magnus.microservices.utilities.http

import org.springframework.http.HttpStatus

data class HttpErrorInfo(
    val httpStatus: HttpStatus,
    val path: String,
    val message: String
)
