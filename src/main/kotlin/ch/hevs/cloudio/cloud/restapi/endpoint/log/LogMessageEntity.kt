package ch.hevs.cloudio.cloud.restapi.endpoint.log

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Endpoint log entry.")
data class LogMessageEntity(
        @ApiModelProperty("Timestamp the log entry was created at the endpoint.", readOnly = true)
        val time: String,

        @ApiModelProperty("Log level.", readOnly = true)
        val level: LogLevel,

        @ApiModelProperty("Log message.", readOnly = true)
        val message: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @ApiModelProperty("Name of the logger.", readOnly = true)
        var loggerName: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @ApiModelProperty("Log source.", readOnly = true)
        var logSource: String
)
