package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@Api(tags = ["Endpoint Management"], description = "Allows users to manage their endpoints.")
@RestController
@RequestMapping("/api/v1")
class EndpointManagementController(
        private val endpointRepository: EndpointRepository,
        private val permissionManager: CloudioPermissionManager,
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository
) {
    @GetMapping("/endpoints", produces=["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)

    @ApiOperation(
            "List all endpoints accessible to the currently authenticated user.",
            response = ListEndpointEntity::class,
            responseContainer = "List")

    fun getAllAccessibleEndpoints(
            @ApiIgnore authentication: Authentication
    ) = permissionManager.resolvePermissions(authentication.userDetails()).mapNotNull { perm ->
        endpointRepository.findById(perm.endpointUUID).orElse(null)?.let {
            ListEndpointEntity(
                    uuid = perm.endpointUUID,
                    friendlyName = it.friendlyName,
                    blocked = it.blocked,
                    online = it.online,
                    permission = perm.permission
            )
        }
    }


    @GetMapping("/endpoints/{uuid}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get detailed information for a given endpoint.")

    fun getEndpointByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @ApiIgnore authentication: Authentication
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.let {
        EndpointEntity(
                uuid = it.uuid,
                friendlyName = it.friendlyName,
                blocked = it.blocked,
                online = it.online,
                metaData = it.metaData,
                version = it.dataModel.version,
                supportedFormats = it.dataModel.supportedFormats
        )
    }


    @GetMapping("/endpoints/{uuid}/friendlyName", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get user-friendly name of given endpoint.")

    fun getEndpointFriendlyNameByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.friendlyName


    @GetMapping("/endpoints/{uuid}/blocked", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get blocked state of given endpoint.")

    fun getEndpointBlockedByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.blocked


    @GetMapping("/endpoints/{uuid}/online", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get online state of given endpoint.")

    fun getEndpointOnlineByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.online


    @GetMapping("/endpoints/{uuid}/configuration", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get configuration parameters of given endpoint.")

    fun getEndpointConfigurationByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.") uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow{
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.let {
        EndpointConfigurationEntity(
                properties = it.properties,
                clientCertificate = it.clientCertificate,
                privateKey = it.privateKey,
                logLevel = it.logLevel
        )
    }

    @GetMapping("/endpoints/{uuid}/configuration/properties", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get configuration properties of given endpoint.")

    fun getEndpointConfigurationPropertiesByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.") uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow{
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.properties


    @GetMapping("/endpoints/{uuid}/metaData", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get meta data of given endpoint.")

    fun getEndpointMetaDataByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.metaData


    @PostMapping("/endpoints", produces=["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional

    @Authority.HttpEndpointCreation

    @ApiOperation(
            "Create a new endpoint.",
            responseReference = "UUID assigned to the newly created endpoint."
    )

    fun createEndpointByFriendlyName(
            @RequestParam @ApiParam("Name of the endpoint.", defaultValue = "New endpoint") friendlyName: String = "New Endpoint",
            @ApiIgnore authentication: Authentication
    ) = authentication.userDetails().let { user ->
        val endpoint = endpointRepository.save(Endpoint(
                friendlyName = friendlyName
        ))
        userEndpointPermissionRepository.save(UserEndpointPermission(
                userID = user.id,
                endpointUUID = endpoint.uuid,
                permission = EndpointPermission.OWN
        ))
        EndpointEntity(
                uuid = endpoint.uuid,
                friendlyName = endpoint.friendlyName,
                blocked = endpoint.blocked,
                online = endpoint.online,
                metaData = endpoint.metaData,
                version = endpoint.dataModel.version,
                supportedFormats = endpoint.dataModel.supportedFormats
        )
    }


    @DeleteMapping("/endpoints/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).OWN)")

    @ApiOperation("Deletes the given endpoint.")

    fun deleteEndpointByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) {
        endpointRepository.deleteById(uuid)
        userEndpointPermissionRepository.deleteByEndpointUUID(uuid)
        userGroupEndpointPermissionRepository.deleteByEndpointUUID(uuid)
    }
}
