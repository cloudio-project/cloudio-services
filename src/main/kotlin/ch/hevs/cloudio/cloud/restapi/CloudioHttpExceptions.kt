package ch.hevs.cloudio.cloud.restapi

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

object CloudioHttpExceptions{
    const val CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE = "You don't have http admin right to access this function"
    const val CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE ="You don't have http admin right to access this function or aren't requesting password change for your own account"
    const val CLOUDIO_BLOCKED_ENDPOINT ="The endpoint you are trying to access is blocked"
    const val CLOUDIO_SUCCESS_MESSAGE = "Success"

    @ResponseStatus(HttpStatus.OK)
    class OkException(message : String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    class BadRequestException(message : String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.FORBIDDEN)
    class ForbiddenException(message : String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    class TimeoutException(message : String) : RuntimeException(message)
}