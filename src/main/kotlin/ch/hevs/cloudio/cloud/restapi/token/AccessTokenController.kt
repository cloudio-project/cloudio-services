package ch.hevs.cloudio.cloud.restapi.token

import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.AccessTokenManager
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioUserDetails
import ch.hevs.cloudio.cloud.security.CloudioUserDetailsService
import io.swagger.v3.oas.annotations.Parameter
import org.apache.juli.logging.LogFactory
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AccessTokenController(
    private val accessTokenManager: AccessTokenManager,
    private val userDetailsService: CloudioUserDetailsService,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LogFactory.getLog(AccessTokenController::class.java)

    @PostMapping("/login", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun login(
        @RequestBody @Parameter() request: AccessTokenRequestEntity
    ): String = userDetailsService.loadUserByUsername(request.username).let {
        when {
            !it.isAccountNonExpired -> {
                log.info("Token refused for user \"${request.username}\" using password authentication - User is banned.")
                throw CloudioHttpExceptions.Forbidden("User is banned.")
            }
            !it.authorities.contains(SimpleGrantedAuthority(Authority.HTTP_ACCESS.toString())) -> {
                log.info("Token refused for user \"${request.username}\" using password authentication - User is missing HTTP_ACCESS role.")
                throw CloudioHttpExceptions.Forbidden("No HTTP access.")
            }
            !passwordEncoder.matches(request.password, it.password) -> {
                log.info("Token refused for user \"${request.username}\" using password authentication - Password is incorrect.")
                throw CloudioHttpExceptions.Forbidden("Wrong password.")
            }
            else -> accessTokenManager.generate(it as CloudioUserDetails)
        }
    }
}
