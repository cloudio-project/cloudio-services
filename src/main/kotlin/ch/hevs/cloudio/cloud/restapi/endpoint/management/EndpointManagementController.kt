package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointPermission
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@RestController
@Profile("rest-api")
@Api(
        tags = ["Endpoint Management"],
        description = "Allows users to manage their endpoints."
)
@RequestMapping("/api/v1/endpoints")
class EndpointManagementController(
        private val endpointRepository: EndpointRepository,
        private val permissionManager: CloudioPermissionManager,
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository,
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate
) {
    @GetMapping("", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)

    @ApiOperation("List all endpoints accessible by the currently authenticated user.",
            response = EndpointListEntity::class,
            responseContainer = "List")

    fun getAllAccessibleEndpoints(
            @ApiIgnore authentication: Authentication,
            @RequestParam(required = false) @ApiParam("If given the list is filtered by the given friendly name.") friendlyName: String?,
            @RequestParam(required = false) @ApiParam("If given the list is filtered by the given banned status.") banned: Boolean?,
            @RequestParam(required = false) @ApiParam("If given the list is filtered by the given online status.") online: Boolean?
    ) = permissionManager.resolvePermissions(authentication.userDetails()).mapNotNull { perm ->
        endpointRepository.findById(perm.endpointUUID).orElse(null)?.let {
            when {
                friendlyName != null && friendlyName.isNotEmpty() && it.friendlyName != friendlyName -> null
                banned != null && it.banned != banned -> null
                online != null && it.online != online -> null
                else -> EndpointListEntity(
                        uuid = perm.endpointUUID,
                        friendlyName = it.friendlyName,
                        banned = it.banned,
                        online = it.online,
                        permission = perm.permission
                )
            }
        }
    }


    @GetMapping("/{uuid}", produces = ["application/json"])
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
                banned = it.banned,
                online = it.online,
                metaData = it.metaData,
                version = it.dataModel.version,
                messageFormatVersion = it.dataModel.messageFormatVersion,
                supportedFormats = it.dataModel.supportedFormats
        )
    }


    @GetMapping("/{uuid}/friendlyName", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get user-friendly name of given endpoint.")

    fun getEndpointFriendlyNameByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.friendlyName


    @GetMapping("/{uuid}/banned", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Returns true if the endpoint is banned (Can not connect to broker) or false if not.")

    fun getEndpointBlockedByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.banned


    @GetMapping("/{uuid}/online", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get online state of given endpoint.")

    fun getEndpointOnlineByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.online


    @GetMapping("/{uuid}/configuration", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get configuration parameters of given endpoint.")

    fun getEndpointConfigurationByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.let {
        EndpointConfigurationEntity(
                properties = it.properties,
                clientCertificate = it.clientCertificate,
                privateKey = it.privateKey,
                logLevel = it.logLevel
        )
    }

    @GetMapping("/{uuid}/configuration/properties", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get configuration properties of given endpoint.")

    fun getEndpointConfigurationPropertiesByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.properties


    @GetMapping("/{uuid}/configuration/properties/{key}")
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get endpoint's configuration property by key.")

    fun getEndpointConfigurationPropertyByUUIDAndPropertyName(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @PathVariable @ApiParam("Name of the property to retrieve.", required = true) key: String
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.properties.getOrDefault(key, null) ?: throw CloudioHttpExceptions.NotFound("Property not found.")


    @GetMapping("/{uuid}/configuration/logLevel")
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Get endpoint's log level.")

    fun getEndpointLogLevelByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.logLevel


    @GetMapping("/{uuid}/metaData", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")

    @ApiOperation("Get meta data of given endpoint.")

    fun getEndpointMetaDataByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.metaData


    @PostMapping("", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional

    @Authority.HttpEndpointCreation

    @ApiOperation(
            "Create a new endpoint.",
            responseReference = "UUID assigned to the newly created endpoint."
    )

    fun postEndpointByFriendlyName(
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
                banned = endpoint.banned,
                online = endpoint.online,
                metaData = endpoint.metaData,
                version = endpoint.dataModel.version,
                messageFormatVersion = endpoint.dataModel.messageFormatVersion,
                supportedFormats = endpoint.dataModel.supportedFormats
        )
    }


    @PutMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).OWN)")

    @ApiOperation("Update endpoint data.")

    fun putEndpointByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @RequestBody body: EndpointEntity
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.run {
            friendlyName = body.friendlyName
            banned = body.banned
            metaData.clear()
            metaData.putAll(body.metaData)
            endpointRepository.save(this)
        }
    }


    @PutMapping("/{uuid}/friendlyName")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).OWN)")

    @ApiOperation("Update endpoint's user-frienly name.")

    fun putEndpointFriendlyNameByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @RequestParam @ApiParam("User-friendly name to set.", required = true) friendlyName: String
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.friendlyName = friendlyName
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/banned")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).OWN)")

    @ApiOperation("Update if an endpoint is banned (Can not connect to broker) or not.")
    fun putEndpointBlockedByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @RequestParam @ApiParam("true to block endpoint, false to unblock endpoint.", required = true) blocked: Boolean
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.banned = blocked
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/configuration/properties/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Update endpoint's configuration parameter.")

    fun putEndpointConfigurationPropertyByUUIDAndPropertyName(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @PathVariable @ApiParam("Name of the property to change.", required = true) key: String,
            @RequestParam @ApiParam("Value to set the property to.", required = true) value: String
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.configuration.properties[key] = value
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/configuration/logLevel")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")

    @ApiOperation("Change an endpoint's log level.")
    fun putEndpointConfigurationLogLevelByUUID(
            @PathVariable @ApiParam("UUID of the endpoint.", required = true) uuid: UUID,
            @RequestParam @ApiParam("Log level.", required = true) logLevel: LogLevel
    ) {
        val endpoint = endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // Serialize and send the message.
        val serializationFormat = serializationFormats.fromIdentifiers(endpoint.dataModel.supportedFormats)
                ?: throw CloudioHttpExceptions.InternalServerError("Endpoint does not support any serialization format.")
        rabbitTemplate.convertAndSend("amq.topic", "@logsLevel.$uuid", serializationFormat.serializeLogLevel(logLevel))
    }


    @DeleteMapping("/{uuid}")
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
