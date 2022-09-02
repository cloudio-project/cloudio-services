package ch.hevs.cloudio.cloud.restapi.endpoint.history

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Datapoint")
data class DataPointEntity(
        val time: String,
        val value: Any?
)
