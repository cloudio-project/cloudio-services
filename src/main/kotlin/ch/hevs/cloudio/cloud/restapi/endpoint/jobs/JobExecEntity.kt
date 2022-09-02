package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "JobExecutionOptions", example = "{\"jobURI\": \"cmd://listJobs\", \"correlationID\": \"30ghubgwe4foih\", \"enableOutput\": true, \"arguments\": \"\"}")
data class JobExecEntity(
        @Schema(description = "URI of the command (cmd://...) or the script (file://...) to execute.")
        val jobURI: String,

        @Schema(description = "If true, the endpoint sends the output of the command to the message broker, otherwise not.", defaultValue = "false")
        val enableOutput: Boolean? = null,

        @Schema(description = "The endpoint will send command/script output to @execOutput/<Endpoint UUID>/<Correlation ID>")
        val correlationID: String,

        @Schema(description = "Optional arguments to pass to the command", defaultValue = "\"\"")
        val arguments: String? = null
)
