package ch.hevs.cloudio.cloud.restapi.auth

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserLoginCredentials", description = "User credentials for access token generation.")
data class UserLoginCredentials(
    @Schema(description = "Username.", example = "john.doe")
    val username: String,

    @Schema(description = "User's password.", example = "--> SECRET <--")
    val password: String
)
