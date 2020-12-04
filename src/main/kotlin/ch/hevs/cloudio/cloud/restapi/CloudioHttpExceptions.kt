package ch.hevs.cloudio.cloud.restapi

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

object CloudioHttpExceptions {
    const val CLOUDIO_BLOCKED_ENDPOINT = "The endpoint you are trying to access is blocked"

    @ResponseStatus(HttpStatus.OK)
    class OK(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    class BadRequest(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.FORBIDDEN)
    class Forbidden(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    class Timeout(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class NotFound(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class Conflict(message: String): RuntimeException(message)

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    class InternalServerError(message: String): java.lang.RuntimeException(message)
}
