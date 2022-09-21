package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointPermission
import ch.hevs.cloudio.cloud.serialization.wot.WotSerializationFormat
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.influxdb.InfluxDB
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

//@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Data Access")
@RequestMapping("/api/v1/wot")
@SecurityRequirement(name = "basicAuth")
class EndpointWOTAccessController(private val endpointRepository: EndpointRepository,
                                  private val permissionManager: CloudioPermissionManager,
                                  private val influxDB: InfluxDB,
                                  private val influxProperties: CloudioInfluxProperties) {
    private val antMatcher = AntPathMatcher()

    @GetMapping("/**", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Read access to endpoint's data in a WOT compatible manner.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint WOT data at the given path.", responseCode = "200", content = [Content(schema = Schema(type = "object"))]),
            ApiResponse(description = "Invalid model identifier.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Endpoint not found or model element not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getModelElement(
        @Parameter(hidden = true) authentication: Authentication,
        @Parameter(hidden = true) request: HttpServletRequest
    ): Any {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/data/**", request.requestURI))
        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }

        // Check if the endpoint exists.
        if (!endpointRepository.existsById(modelIdentifier.endpoint)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // Resolve the access level the user has to the endpoint and fail if the user has no access to the endpoint.
        val endpointPermission = permissionManager.resolveEndpointPermission(authentication.userDetails(), modelIdentifier.endpoint)
        if (!endpointPermission.fulfills(EndpointPermission.ACCESS)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        // Retrieve endpoint from repository.
        val endpoint = endpointRepository.findById(modelIdentifier.endpoint).orElseThrow{
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // If the model path is empty, we return the whole data model of the endpoint.
        val data = modelIdentifier.resolve(endpoint.dataModel).orElseThrow {
            CloudioHttpExceptions.NotFound("Model element not found.")
        }

        // If the user only has partial access to the endpoint's model, filter the data model accordingly.
        if (!endpointPermission.fulfills(EndpointPermission.ACCESS)) {
            // TODO: Filter endpoint data based on model element permissions.
        }

        // Fill data from influxDB.
        return when(data) {

            is Node -> {
                data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
                WotSerializationFormat.wotNodeFromCloudioNode(endpoint.dataModel, endpoint.uuid.toString(), modelIdentifier.last(), request.baseURL())
                        ?: throw CloudioHttpExceptions.InternalServerError("WOT serialization error")
            }
            else -> {
                throw CloudioHttpExceptions.BadRequest("Requested element is not a node.")
            }
        }
    }

    private fun HttpServletRequest.baseURL(): String {
        val scheme = getHeader("x-forwarded-proto") ?: scheme
        val port = (getHeader("x-forwarded-port") ?: localPort).let {
            when {
                it == 80 && scheme == "http" -> ""
                it == 443 && scheme == "https" -> ""
                else -> ":$it"
            }
        }
        return "$scheme://${serverName}${port}"
    }
}
