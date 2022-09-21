package ch.hevs.cloudio.cloud.restapi.endpoint.history

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint History Access", description = "Allows an user to access time series data of endpoints.")
@RequestMapping("/api/v1/history")
@SecurityRequirement(name = "basicAuth")
class EndpointHistoryAccessController(
    private val endpointRepository: EndpointRepository,
    private val permissionManager: CloudioPermissionManager,
    private val influx: InfluxDB,
    private val influxProperties: CloudioInfluxProperties
) {
    private val antMatcher = AntPathMatcher()

    @Operation(summary = "Read access to endpoint's historical values.")
    @GetMapping("/**", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Time-series data for the given attribute.",
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = DataPointEntity::class)))]
            ),
            ApiResponse(description = "Invalid model identifier.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Endpoint not found or model element not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Time-series database error.", responseCode = "500", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getModelElement(
        @Parameter(hidden = true) authentication: Authentication,
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no start date (all data).", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no end date (all data).", required = false) to: String?,
        @RequestParam @Parameter(
            description = "Optional interval to resample data to. The format is a number followed by n, u, ms, s, m, h, d or w. If not given, no resampling is done.",
            required = false
        ) resampleInterval: String?,
        @RequestParam @Parameter(
            description = "Function used to resample data. Only considered when a resample interval was given too.",
            required = false, schema = Schema(implementation = ResampleFunction::class, defaultValue = "MEAN")
        ) resampleFunction: ResampleFunction?,
        @RequestParam @Parameter(
            description = "Value to use fill resampled intervals with no data. Only considered when a resample interval was given too.",
            required = false, schema = Schema(implementation = FillValue::class, defaultValue = "NONE")
        ) fillValue: FillValue?,
        @RequestParam @Parameter(description = "Maximal number of entries to return.", required = false, schema = Schema(defaultValue = "1000")) max: Int?,
        @Parameter(hidden = true) request: HttpServletRequest
    ): Collection<DataPointEntity> {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/history/**", request.requestURI))
        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }

        // Check if the endpoint exists.
        if (!endpointRepository.existsById(modelIdentifier.endpoint)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        //  Check if user has access to the attribute.
        if (!permissionManager.hasEndpointModelElementPermission(authentication.userDetails(), modelIdentifier, EndpointModelElementPermission.READ)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        return queryInflux(modelIdentifier, from, to, resampleInterval, resampleFunction, fillValue, max)?.values?.map {
            DataPointEntity(
                time = it[0] as String,
                value = it[1]
            )
        } ?: emptyList()
    }

    @Operation(summary = "Read access to endpoint's historical values.")
    @GetMapping("/**", produces = ["text/csv"])
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Time-series data for the given attribute in CSV formats.",
                responseCode = "200",
                content = [Content(schema = Schema(type = "string", example = "2015-08-18T00:00:00Z;42\n2015-08-19T00:00:00Z;43"))]
            ),
            ApiResponse(description = "Invalid model identifier.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Endpoint not found or model element not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Time-series database error.", responseCode = "500", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getModelElementAsCSV(
        @Parameter(hidden = true) authentication: Authentication,
        @RequestParam @Parameter(description = "Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no start date (all data).", required = false) from: String?,
        @RequestParam @Parameter(description = "Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'. Default is to no end date (all data).", required = false) to: String?,
        @RequestParam @Parameter(
            description = "Optional interval to resample data to. The format is a number followed by n, u, ms, s, m, h, d or w. If not given, no resampling is done.",
            required = false
        ) resampleInterval: String?,
        @RequestParam @Parameter(
            description = "Function used to resample data. Only considered when a resample interval was given too.",
            required = false, schema = Schema(implementation = ResampleFunction::class, defaultValue = "MEAN")
        ) resampleFunction: ResampleFunction?,
        @RequestParam @Parameter(
            description = "Value to use fill resampled intervals with no data. Only considered when a resample interval was given too.",
            required = false, schema = Schema(implementation = FillValue::class, defaultValue = "NONE")
        ) fillValue: FillValue?,
        @RequestParam @Parameter(description = "Maximal number of entries to return.", required = false, schema = Schema(defaultValue = "1000")) max: Int?,
        @RequestParam @Parameter(description = "CSV Separator.", required = false, schema = Schema(defaultValue = ";")) separator: String?,
        @Parameter(hidden = true) request: HttpServletRequest
    ): String {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/history/**", request.requestURI))
        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }

        // Check if the endpoint exists.
        if (!endpointRepository.existsById(modelIdentifier.endpoint)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        //  Check if user has access to the attribute.
        if (!permissionManager.hasEndpointModelElementPermission(authentication.userDetails(), modelIdentifier, EndpointModelElementPermission.READ)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        return queryInflux(modelIdentifier, from, to, resampleInterval, resampleFunction, fillValue, max)?.values?.
        joinToString(separator = "\n") {
            "${it[0] as String}${separator ?: ";"}${it[1]}"
        }.orEmpty()
    }

    private fun queryInflux(modelIdentifier: ModelIdentifier, from: String?, to: String?, resampleInterval: String?, resampleFunction: ResampleFunction?, fillValue: FillValue?, max: Int?): QueryResult.Series? {
        val result = influx.query(Query(
            "SELECT time, ${
                if (resampleInterval != null) {
                    (resampleFunction ?: ResampleFunction.MEAN).toString() + "(value)"
                } else {
                    "value"
                }
            } FROM \"${modelIdentifier.toInfluxSeriesName()}\" " +
                    (from?.let { "WHERE time >= '${it.toDate().toRFC3339()}' " } ?: "") +
                    (to?.let { "AND time <= '${it.toDate().toRFC3339()}' " } ?: "") +
                    (resampleInterval?.let { "GROUP BY time($resampleInterval) fill(${(fillValue ?: FillValue.NONE).id}) " } ?: "") +
                    "ORDER BY time ASC " +
                    "LIMIT ${max ?: 1000}",
            influxProperties.database))

        if (result.hasError()) {
            throw CloudioHttpExceptions.InternalServerError("InfluxDB error: ${result.error}")
        }

        return result.results.firstOrNull()?.series?.firstOrNull()
    }

    private fun String.toDate() = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(this)
    } catch (exception: Exception) {
        throw CloudioHttpExceptions.BadRequest("Invalid date format.")
    }

    private fun Date.toRFC3339() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(this)
}
