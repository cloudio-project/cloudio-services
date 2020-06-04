package ch.hevs.cloudio.cloud.restapi.endpoint

import java.util.*

data class EndpointListEntity(
        val uuid: UUID,
        val friendlyName: String,
        val blocked: Boolean,
        val online: Boolean
)