package ch.hevs.cloudio.cloud.restapi

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(HttpStatus.OK)
class CloudioOkException(message : String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class CloudioBadRequestException(message : String) : RuntimeException(message)

@ResponseStatus(HttpStatus.FORBIDDEN)
class CloudioForbiddenException(message : String) : RuntimeException(message)