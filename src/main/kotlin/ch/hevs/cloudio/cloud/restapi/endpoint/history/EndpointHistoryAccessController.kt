package ch.hevs.cloudio.cloud.restapi.endpoint.history

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Api(tags = ["Endpoint Model Access"], description = "Allows an user to access time series data of endpoints.")
@RequestMapping("/api/v1/history")
class EndpointHistoryAccessController(
        private val endpointRepository: EndpointRepository,
        private val permissionManager: CloudioPermissionManager,
        private val influx: InfluxDB,
        private val influxProperties: CloudioInfluxProperties
) {
    private val antMatcher = AntPathMatcher()

    @ApiOperation("Read access to endpoint's historical values.")
    @GetMapping("/**")
    @ResponseStatus(HttpStatus.OK)
    fun getModelElement(
            @ApiIgnore authentication: Authentication,
            @RequestParam @ApiParam("Optional start date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) from: String?,
            @RequestParam @ApiParam("Optional end date (UTC) in the format 'yyyy-MM-ddTHH:mm:ss'.", required = false) to: String?,
            @RequestParam @ApiParam("Optional interval to resample data to. The format is a number followed by n, u, ms, s, m, h, d or w.", required = false) resampleInterval: String?,
            @RequestParam @ApiParam("Function used to resample data. Only considered when a resample interval was given too. Defaults to MEAN", required = false) resampleFunction: ResampleFunction?,
            @RequestParam @ApiParam("Value to use fill resampled intervals with no data. Only considered when a resample interval was given too. Defaults to NULL")fillValue: FillValue?,
            @RequestParam @ApiParam("Maximal number of entries to return, defaults to 1000.", required = false, defaultValue = "1000") max: Int?,
            @ApiIgnore request: HttpServletRequest
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
                        (resampleInterval?.let { "GROUP BY time($resampleInterval) fill(${(fillValue ?: FillValue.NULL).id}) " } ?: "") +
                        "ORDER BY time ASC " +
                        "LIMIT ${max ?: 1000}",
                influxProperties.database))

        if (result.hasError()) {
            throw CloudioHttpExceptions.InternalServerError("InfluxDB error: ${result.error}")
        }

        return result.results.firstOrNull()?.series?.firstOrNull()?.values?.map {
            DataPointEntity(
                    time = it[0] as String,
                    value = it[1]
            )
        } ?: emptyList()
    }

    private fun String.toDate() = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(this)
    } catch (exception: Exception) {
        throw CloudioHttpExceptions.BadRequest("Invalid date format.")
    }

    private fun Date.toRFC3339() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(this)
}
