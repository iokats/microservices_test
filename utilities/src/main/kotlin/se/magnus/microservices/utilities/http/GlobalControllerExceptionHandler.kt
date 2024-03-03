package se.magnus.microservices.utilities.http

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import se.magnus.api.exceptions.InvalidInputException
import se.magnus.api.exceptions.NotFoundException

private val LOG: Logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    @ResponseBody
    fun handleNotFoundExceptions(
        request: ServerHttpRequest, ex: NotFoundException
    ): HttpErrorInfo {

        return createHttpErrorInfo(NOT_FOUND, request, ex)
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException::class)
    @ResponseBody
    fun handleInvalidInputException(
        request: ServerHttpRequest, ex: InvalidInputException
    ): HttpErrorInfo {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex)
    }

    private fun createHttpErrorInfo(
        httpStatus: HttpStatus, request: ServerHttpRequest, ex: Exception
    ): HttpErrorInfo {
        val path: String = request.path.pathWithinApplication().value()
        val message = ex.message?:""

        LOG.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message)
        return HttpErrorInfo(httpStatus, path, message)
    }
}