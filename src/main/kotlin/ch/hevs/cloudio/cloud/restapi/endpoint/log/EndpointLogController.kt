package ch.hevs.cloudio.cloud.restapi.endpoint.log

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Log Access", description = "Access endpoint logs.")
@RequestMapping("/api/v1/endpoints")
class EndpointLogController(
    private val influx: InfluxDB,
    private val influxProperties: CloudioInfluxProperties,
    private val serializationFormats: Collection<SerializationFormat>,
    private val amqpAdmin: AmqpAdmin,
    private val connectionFactory: ConnectionFactory
) {
    @GetMapping("/{uuid}/log", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Retrieve log output of a given endpoint in JSON format.")
    fun getEndpointLogsByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Log level threshold, defaults to WARN.") threshold: LogLevel?, // TODO: Doc default value of "WARN"
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) to: String?,
        @RequestParam @Parameter(description = "Maximal number of log entries to return, defaults to 1000.", required = false) max: Int? // TODO: Doc default value of 1000
    ): List<LogMessageEntity> {
        val result = influx.query(Query(
            "SELECT time, level, message, logSource, loggerName FROM \"$uuid.logs\" " +
                    "WHERE level <= ${(threshold ?: LogLevel.WARN).ordinal} " +
                    (from?.let { "AND time >= '${it.toDate().toRFC3339()}' " } ?: "") +
                    (to?.let { "AND time <= '${it.toDate().toRFC3339()}' " } ?: "") +
                    "ORDER BY time DESC " +
                    "LIMIT ${max ?: 1000}",
            influxProperties.database))

        if (result.hasError()) {
            throw CloudioHttpExceptions.InternalServerError("InfluxDB error: ${result.error}")
        }

        return result.results.firstOrNull()?.series?.firstOrNull()?.values?.map {
            LogMessageEntity(
                time = it[0] as String,
                level = LogLevel.values()[(it[1] as Double).toInt()],
                message = it[2] as String,
                loggerName = it[3] as String,
                logSource = it[4] as String
            )
        } ?: emptyList()
    }

    @GetMapping("/{uuid}.log", produces = ["text/plain"])
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Retrieve log output of a given endpoint as text file.")
    fun getEndpointLogFileByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Log level threshold, defaults to WARN.") threshold: LogLevel?, // TODO: Doc default value of "WARN"
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) to: String?,
        @RequestParam @Parameter(description = "Maximal number of log entries to return, defaults to 1000.", required = false) max: Int? // TODO: Doc default value of 1000
    ) = getEndpointLogsByUUID(uuid, threshold, from, to, max).joinToString("\n") {
        "${it.time} [${it.level}] ${it.message}"
    }

    private fun String.toDate() = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(this)
    } catch (exception: Exception) {
        throw CloudioHttpExceptions.BadRequest("Invalid date format.")
    }

    private fun Date.toRFC3339() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(this)
}
