package ch.hevs.cloudio.cloud.restapi.auth

import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.AccessTokenManager
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioUserDetails
import ch.hevs.cloudio.cloud.security.CloudioUserDetailsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.juli.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@Profile("rest-api")
@Tag(name = "Login", description = "Generates access token for the given user.")
@RequestMapping("/api/v1/auth")
class AccessTokenLoginController(
    private val accessTokenManager: AccessTokenManager,
    private val userDetailsService: CloudioUserDetailsService,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LogFactory.getLog(AccessTokenLoginController::class.java)

    @PostMapping("/login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Authenticates user and generates an access token for the user.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Access token generated.", responseCode = "200", content = [Content(schema = Schema(type = "string",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwidWlkIjoyMzQyLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUyNjIzOTAyMn0.nt_g2tRx2sAUYn1p94S2nsbHVpX8CUU6oNSQ19TApC8"))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun login(
        @RequestBody @Parameter(description = "User's credentials.") request: UserLoginCredentials
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
            else -> accessTokenManager.generateUserAccessToken(it as CloudioUserDetails)
        }
    }
}
