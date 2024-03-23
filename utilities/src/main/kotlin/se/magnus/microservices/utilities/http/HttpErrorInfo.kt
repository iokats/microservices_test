package se.magnus.microservices.utilities.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class HttpErrorInfo(
    val status: HttpStatus,
    val path: String,
    val message: String,
    val timestamp: ZonedDateTime = ZonedDateTime.now()
)
