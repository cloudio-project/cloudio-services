package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "JobExecutionOptions", description = "Job execution options.")
data class JobExecEntity(
        @Schema(description = "URI of the command (cmd://...) or the script (file://...) to execute.", example = "cmd://listJobs")
        val jobURI: String,

        @Schema(description = "If true, the endpoint sends the output of the command to the message broker, otherwise not.", defaultValue = "false", example = "true")
        val enableOutput: Boolean? = null,

        @Schema(description = "The endpoint will send command/script output to @execOutput/<Endpoint UUID>/<Correlation ID>", example = "543t789herg8h98")
        val correlationID: String,

        @Schema(description = "Optional arguments to pass to the command", defaultValue = "", example = "")
        val arguments: String? = null
)
