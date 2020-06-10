package ch.hevs.cloudio.cloud.restapi.endpoint.logs

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

@RestController
@RequestMapping("/api/v1/logs")
@Api(
        tags = ["Endpoint Log Access"],
        description = "Access endpoint logs."
)
class EndpointLogController(
        private val influx: InfluxDB,
        private val influxProperties: CloudioInfluxProperties
) {
    @GetMapping("/{uuid}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @ApiOperation("Retrieve log output of a given endpoint in JSON format.")
    fun getEndpointLogsByUUID(
            @PathVariable @ApiParam("UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
            @RequestParam @ApiParam("Log level threshold, defaults to WARN.", defaultValue = "WARN") threshold: LogLevel?,
            @RequestParam @ApiParam("Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) from: String?,
            @RequestParam @ApiParam("Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) to: String?,
            @RequestParam @ApiParam("Maximal number of log entries to return, defaults to 1000.", required = false, defaultValue = "1000") max: Int?
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
    @ApiOperation("Retrieve log output of a given endpoint as text file.")
    fun getEndpointLogFileByUUID(
            @PathVariable @ApiParam("UUID of the endpoint of which the log output should be retrieved.", required = true) uuid: UUID,
            @RequestParam @ApiParam("Log level threshold, defaults to WARN.", defaultValue = "WARN") threshold: LogLevel?,
            @RequestParam @ApiParam("Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) from: String?,
            @RequestParam @ApiParam("Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) to: String?,
            @RequestParam @ApiParam("Maximal number of log entries to return, defaults to 1000.", required = false, defaultValue = "1000") max: Int?
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
