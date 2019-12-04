package ch.hevs.cloudio.cloud.restapi

import org.springframework.security.core.AuthenticationException

class CloudioAuthorityException(msg: String) : AuthenticationException(msg)