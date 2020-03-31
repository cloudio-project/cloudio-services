package ch.hevs.cloudio.cloud.restapi.endpoint

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/api/v1/endpoint")
class EndpointManagementController(
        private val endpointEntityRepository: EndpointEntityRepository,
        private val userRepository: UserRepository,
        private val userGroupRepository: UserGroupRepository,
        private val influxDB: InfluxDB,
        private val influxProperties: CloudioInfluxProperties
) {
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun createEndpoint(@RequestParam friendlyName: String, principal: Principal) = userRepository.findById(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User '${principal.name}' not found.")
    }.let {
        val endpoint = EndpointEntity(UUID.randomUUID(), friendlyName)
        endpointEntityRepository.save(endpoint)
        it.permissions["${endpoint.endpointUuid}/#"] = PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST)
        userRepository.save(it)
        endpoint.endpointUuid.toString()
    }

    @GetMapping("{uuid}")
    fun getEndpoint(@PathVariable uuid: UUID, principal: Principal): EndpointEntity {
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
            it
        }
    }
}
