package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import io.swagger.v3.oas.annotations.media.Schema

// TODO: Document.
@Schema(name = "JobExec")
data class JobExecEntity(
        val jobURI: String,
        val enableOutput: Boolean,
        val correlationID: String,
        val arguments: String = "",
        val timeout: Long
)
