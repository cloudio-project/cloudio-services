package ch.hevs.cloudio.cloud.restapi.endpoint

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.dao.Endpoint
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.extension.fillFromInfluxDB
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.apache.juli.logging.LogFactory
import org.influxdb.InfluxDB
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional

@Api(tags = ["Endpoint Access"], description = "Allows an user to access and manage endpoints and their actual data.")
@RestController
@RequestMapping("/api/v1")
class EndpointManagementController(
        private val endpointRepository: EndpointRepository,
        private val permissionManager: CloudioPermissionManager,
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository,
        private val influxDB: InfluxDB,
        private val influxProperties: CloudioInfluxProperties
) {
    private val log = LogFactory.getLog(EndpointManagementController::class.java)
    private val antMatcher = AntPathMatcher()

    @GetMapping("/model/**")
    @ResponseStatus(HttpStatus.OK)
    fun getModelElement(authentication: Authentication, request: HttpServletRequest): Any {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/model/**", request.requestURI))
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
        if (!endpointPermission.fulfills(EndpointPermission.BROWSE)) {
            // TODO: Filter endpoint data based on model element permissions.
        }

        // Fill data from influxDB.
        when(data) {
            is ch.hevs.cloudio.cloud.model.Endpoint -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, endpoint.uuid)
            is Node -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.influxMeasurementName())
            is CloudioObject -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.influxMeasurementName())
            is Attribute -> data.fillFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.influxMeasurementName())
        }

        return data
    }





    @ApiOperation("List all endpoints accessible to the current user.")
    @GetMapping("/endpoints")
    @ResponseStatus(HttpStatus.OK)
    fun getAllAccessibleEndpoints(@ApiIgnore authentication: Authentication): Collection<UUID> = permissionManager.resolvePermissions(authentication.userDetails()).map { it.endpointUUID }

    @ApiOperation("Create a new endpoint.")
    @Authority.HttpEndpointCreation
    @PostMapping("/endpoints")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun createEndpointByFriendlyName(@RequestParam friendlyName: String = "Unnamed Endpoint", @ApiIgnore authentication: Authentication) = authentication.userDetails().let { user ->
        val endpoint = endpointRepository.save(Endpoint(
                friendlyName = friendlyName
        ))
        userEndpointPermissionRepository.save(UserEndpointPermission(
                userID = user.id,
                endpointUUID = endpoint.uuid,
                permission = EndpointPermission.OWN
        ))
        endpoint.uuid
    }

    @ApiOperation("Get endpoint information.")
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @GetMapping("/endpoints/{uuid}")
    fun getEndpointByUUID(@PathVariable uuid: UUID, @ApiIgnore authentication: Authentication) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }
    // TODO: Filter out data model and config depending the actual permission the user has.
    // TODO: Fill in data from InfluxDB.

    // TODO: Update (PUT) Endpoint??




    @ApiOperation("Get friendly name of endpoint.")
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @GetMapping("/endpoints/{uuid}/friendlyName")
    fun getEndpointFriendlyNameByUUID(@PathVariable uuid: UUID) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.friendlyName

    // TODO: Change (PUT) endpoint friendly name.

    @ApiOperation("Get blocked state of endpoint.")
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @GetMapping("/endpoints/{uuid}/blocked")
    fun getEndpointBlockedByUUID(@PathVariable uuid: UUID) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.blocked

    // TODO: Change (PUT) endpoint blocked.
}
