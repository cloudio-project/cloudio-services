package ch.hevs.cloudio.cloud.restapi.endpoint.log

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "LogMessage", description = "Endpoint log entry.")
data class LogMessageEntity(
        @Schema(description = "Timestamp the log entry was created at the endpoint.", readOnly = true)
        val time: String,

        @Schema(description = "Log level.", readOnly = true)
        val level: LogLevel,

        @Schema(description = "Log message.", readOnly = true)
        val message: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Name of the logger.", readOnly = true)
        var loggerName: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Log source.", readOnly = true)
        var logSource: String
)
