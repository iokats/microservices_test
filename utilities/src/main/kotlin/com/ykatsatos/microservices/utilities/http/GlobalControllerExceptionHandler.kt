package com.ykatsatos.microservices.utilities.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import com.ykatsatos.api.exceptions.BadRequestException
import com.ykatsatos.api.exceptions.InvalidInputException
import com.ykatsatos.api.exceptions.NotFoundException

private val LOG: Logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    @ResponseBody fun handleBadRequestExceptions(
        request: ServerHttpRequest, ex: BadRequestException
    ): HttpErrorInfo {

        return createHttpErrorInfo(BAD_REQUEST, request, ex)
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    @ResponseBody fun handleNotFoundExceptions(
        request: ServerHttpRequest, ex: NotFoundException
    ): HttpErrorInfo {

        return createHttpErrorInfo(NOT_FOUND, request, ex)
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException::class)
    @ResponseBody fun handleInvalidInputException(
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