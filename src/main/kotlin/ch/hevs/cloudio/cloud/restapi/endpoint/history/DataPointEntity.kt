package ch.hevs.cloudio.cloud.restapi.endpoint.history

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Datapoint")
data class DataPointEntity(
        @Schema(description = "Timestamp as string", example = "2015-08-18T00:00:00Z")
        val time: String,

        @Schema(description = "Value", example = "42")
        val value: Any?
)
