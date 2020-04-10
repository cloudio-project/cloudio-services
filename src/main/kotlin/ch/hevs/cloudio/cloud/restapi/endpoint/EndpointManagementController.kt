package ch.hevs.cloudio.cloud.restapi.endpoint

import ch.hevs.cloudio.cloud.apiutils.EndpointManagementUtil
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.MONOGOEndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.influxdb.InfluxDB
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.security.Principal
import java.util.*

@Api(tags = ["Endpoint Management"], description = "Allows an user to access and manage endpoints and their actual data.")
@RestController
@RequestMapping("/api/v1/endpoints")
class EndpointManagementController(
        private val endpointEntityRepository: MONOGOEndpointEntityRepository,
        private val userRepository: MONGOUserRepository,
        private val userGroupRepository: MONGOUserGroupRepository,
        private val influxDB: InfluxDB,
        private val influxProperties: CloudioInfluxProperties
) {
    @ApiOperation("Create a new endpoint.")
    @Authority.HttpEndpointCreation
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createEndpointByFriendlyName(@RequestParam friendlyName: String, @ApiIgnore principal: Principal) = userRepository.findById(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User '${principal.name}' not found.")
    }.let {
        val endpoint = EndpointEntity(UUID.randomUUID(), friendlyName)
        endpointEntityRepository.save(endpoint)
        it.permissions["${endpoint.endpointUuid}/#"] = PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST)
        userRepository.save(it)
        endpoint.endpointUuid
    }

    @ApiOperation("Get endpoint information.")
    @GetMapping("/{uuid}")
    fun getEndpointByUUID(@PathVariable uuid: UUID, @ApiIgnore principal: Principal): EndpointEntity {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(principal.name, userRepository, userGroupRepository)
        val genericTopic = "$uuid/#"
        val splitTopic = genericTopic.split("/")
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic) == Permission.DENY) {
            throw CloudioHttpExceptions.Forbidden("You do not have permission to access this endpoint.")
        }
        return endpointEntityRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint '$uuid' not found.")
        }.let {
            it.fillAttributesFromInfluxDB(influxDB, influxProperties.database)
            PermissionUtils.censorEndpointFromUserPermission(permissionMap, it)
            // TODO: Exclude data model when returning endpoint.
            it
        }
    }

    // TODO: Update (PUT) Endpoint

    @ApiOperation("Get friendly name of endpoint.")
    @GetMapping("/{uuid}/friendlyName")
    fun getEndpointFriendlyNameByUUID(@PathVariable uuid: UUID, @ApiIgnore principal: Principal) = getEndpointByUUID(uuid, principal).friendlyName

    // TODO: Change (PUT) endpoint friendly name.

    @ApiOperation("Get friendly name of endpoint.")
    @GetMapping("/{uuid}/blocked")
    fun getEndpointBlockedByUUID(@PathVariable uuid: UUID, @ApiIgnore principal: Principal) = getEndpointByUUID(uuid, principal).blocked

    // TODO: Change (PUT) endpoint blocked.

    @ApiOperation("List all accessible endpoints.")
    @GetMapping("")
    // TODO: We should consider to return all endpoints that the user has access to, not just the owned ones.
    fun getAllOwnedEndpoints(@ApiIgnore principal: Principal): Collection<EndpointListEntity> {
        // TODO: Integrate code from util here...
        return EndpointManagementUtil.getOwnedEndpoints(userRepository, userGroupRepository, endpointEntityRepository, principal.name).ownedEndpoints.map {
            EndpointListEntity(UUID.fromString(it.endpointUuid), it.friendlyName, it.blocked ?: false)
        }
    }
}
