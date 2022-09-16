package ch.hevs.cloudio.cloud.restapi.token

data class AccessTokenRequestEntity(
    val username: String,
    val password: String
)
