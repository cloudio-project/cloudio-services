package ch.hevs.cloudio.cloud.restapi.endpoint.log

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "LogMessage", description = "Endpoint log entry.")
data class LogMessageEntity(
        @Schema(description = "Timestamp the log entry was created at the endpoint.", readOnly = true, example = "2015-08-18T00:00:00Z")
        val time: String,

        @Schema(description = "Log level.", readOnly = true, example = "WARN")
        val level: LogLevel,

        @Schema(description = "Log message.", readOnly = true, example = "Could not connect to Bluetooth device 04:74:28:05:05")
        val message: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Name of the logger.", readOnly = true, example = "ch.hevs.bluetooth.rfcomm.RfCommConnection")
        var loggerName: String?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Log source.", readOnly = true)
        var logSource: String?
)
