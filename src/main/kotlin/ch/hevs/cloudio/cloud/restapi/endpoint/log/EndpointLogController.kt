package ch.hevs.cloudio.cloud.restapi.endpoint.log

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxQLQueryApi
import com.influxdb.client.domain.InfluxQLQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Log Access", description = "Access log output of an endpoint.")
@RequestMapping("/api/v1/endpoints")
@SecurityRequirement(name = "basicAuth")
class EndpointLogController(
    private val influx: InfluxDBClient,
    private val influxProperties: CloudioInfluxProperties
) {
    @GetMapping("/{uuid}/log", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Retrieve log output of a given endpoint in JSON format.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Log output for endpoint.", responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = LogMessageEntity::class)))]),
            ApiResponse(description = "Time-series database error.", responseCode = "500", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getEndpointLogsByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Log level threshold.", schema = Schema(defaultValue = "WARN")) threshold: LogLevel?,
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no start date (all log output).", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no end date (all log output).", required = false) to: String?,
        @RequestParam @Parameter(description = "Maximal number of log entries to return.", required = false, schema = Schema(defaultValue = "1000")) max: Int?
    ): List<LogMessageEntity> {

        val queryApi: InfluxQLQueryApi = influx.influxQLQueryApi

        val result = queryApi.query(InfluxQLQuery(
                "SELECT time, level, message, logSource, loggerName FROM \"$uuid.logs\" " +
                        "WHERE level <= ${(threshold ?: LogLevel.WARN).ordinal} " +
                        (from?.let { "AND time >= '${it.toDate().toRFC3339()}' " } ?: "") +
                        (to?.let { "AND time <= '${it.toDate().toRFC3339()}' " } ?: "") +
                        "ORDER BY time DESC " +
                        "LIMIT ${max ?: 1000}",
            influxProperties.database))
        /*//TODO update to influx 2.x.hasError() doesn't exist anymore
        if (result.hasError()) {
            throw CloudioHttpExceptions.InternalServerError("InfluxDB error: ${result.error}")
        }
         */

        return result.results.firstOrNull()?.series?.firstOrNull()?.values?.map {
            LogMessageEntity(
                time = it.values[0] as String,
                level = LogLevel.values()[Integer.parseInt(it.values[1] as String)],
                message = it.values[2] as String,
                loggerName = it.values[3] as String,
                logSource = it.values[4] as String
            )
        } ?: emptyList()
    }

    @GetMapping("/{uuid}.log", produces = ["text/plain"])
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Retrieve log output of a given endpoint as text file.")
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Log output for endpoint.", responseCode = "200", content = [Content(
                    schema = Schema(
                        type = "string", example =
                        "2015-08-18T00:00:00Z (Thread-283) [ch.hevs.bluetooth.rfcomm.RfCommConnection]: Could not connect to Bluetooth device 04:74:28:05:05\n" +
                                "2015-08-19T00:00:00Z (Thread-286) [ch.hevs.bluetooth.rfcomm.RfCommConnection]: Could not connect to Bluetooth device 02:38:95:44:97"
                    )
                )]
            ),
            ApiResponse(description = "Time-series database error.", responseCode = "500", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getEndpointLogFileByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Log level threshold.", schema = Schema(defaultValue = "WARN")) threshold: LogLevel?,
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no start date (all log output).", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no end date (all log output).", required = false) to: String?,
        @RequestParam @Parameter(description = "Maximal number of log entries to return.", required = false, schema = Schema(defaultValue = "1000")) max: Int?
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
