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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Management", description = "Allows users to list and manage their endpoints.")
@RequestMapping("/api/v1/endpoints")
class EndpointManagementController(
    private val endpointRepository: EndpointRepository,
    private val permissionManager: CloudioPermissionManager,
    private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
    private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository,
    private val serializationFormats: Collection<SerializationFormat>,
    private val rabbitTemplate: RabbitTemplate,
    private val endpointGroupRepository: EndpointGroupRepository
) {
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @Operation(summary = "List all endpoints accessible by the currently authenticated user.")
    @ApiResponses(
        value = [
            ApiResponse(
                description = "List of endpoints accessible for the logged user.",
                responseCode = "200",
                content = [Content(array = ArraySchema(schema = Schema(implementation = EndpointListEntity::class)))]
            ),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getAllAccessibleEndpoints(
        @Parameter(hidden = true) authentication: Authentication,
        @RequestParam(required = false) @Parameter(description = "If given the list is filtered by the given friendly name.") friendlyName: String?,
        @RequestParam(required = false) @Parameter(description = "If given the list is filtered by the given banned status.") banned: Boolean?,
        @RequestParam(required = false) @Parameter(description = "If given the list is filtered by the given online status.") online: Boolean?
    ) = permissionManager.resolvePermissions(authentication.userDetails()).mapNotNull { perm -> // TODO: Maybe resolveEndpointPermission() would be better in this case.
        endpointRepository.findById(perm.endpointUUID).orElse(null)?.let {
            when {
                !friendlyName.isNullOrEmpty() && it.friendlyName != friendlyName -> null
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


    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @Operation(summary = "Get detailed information for a given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint details.", responseCode = "200", content = [Content(schema = Schema(implementation = EndpointEntity::class))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
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
            supportedFormats = it.dataModel.supportedFormats,
            groupMemberships = it.groupMemberships.map { it.groupName }.toSet()
        )
    }


    @GetMapping("/{uuid}/friendlyName", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @Operation(summary = "Get user-friendly name of given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "User-friendly name of the endpoint.", responseCode = "200", content = [Content(schema = Schema(type = "string", example = "My Endpoint"))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointFriendlyNameByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.friendlyName


    @GetMapping("/{uuid}/banned", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @Operation(summary = "Returns true if the endpoint is banned (Can not connect to broker) or false if not.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Banned status.", responseCode = "200", content = [Content(schema = Schema(type = "boolean", example = "false"))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointBlockedByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.banned


    @GetMapping("/{uuid}/online", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @Operation(summary = "Get online state of given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Online status.", responseCode = "200", content = [Content(schema = Schema(type = "boolean", example = "true"))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointOnlineByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.online


    @GetMapping("/{uuid}/configuration", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Get configuration parameters of given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint configuration.", responseCode = "200", content = [Content(schema = Schema(implementation = EndpointConfigurationEntity::class))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointConfigurationByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
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

    @GetMapping("/{uuid}/configuration/properties", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Get configuration properties of given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Endpoint configuration.",
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        type = "object",
                        example = "{\"ch.hevs.cloudio.endpoint.uuid\": \"041c0e1e-b6d4-4f47-92b4-9f63343dbd28\", \"ch.hevs.cloudio.endpoint.hostUri\": \"cloudio.hevs.ch\"}"
                    )
                )]
            ),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointConfigurationPropertiesByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.properties


    @GetMapping("/{uuid}/configuration/properties/{key}", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Get endpoint's configuration property by key.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint configuration property.", responseCode = "200", content = [Content(schema = Schema(type = "string", example = "cloudio.hevs.ch"))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Property not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointConfigurationPropertyByUUIDAndPropertyName(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @PathVariable @Parameter(description = "Name of the property to retrieve.", required = true) key: String
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.properties.getOrDefault(key, null) ?: throw CloudioHttpExceptions.NotFound("Property not found.")


    @GetMapping("/{uuid}/configuration/logLevel", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Get endpoint's log level.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint log level.", responseCode = "200", content = [Content(schema = Schema(implementation = LogLevel::class))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointLogLevelByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.configuration.logLevel


    @GetMapping("/{uuid}/metaData", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    @Operation(summary = "Get metadata of given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Endpoint metadata.",
                responseCode = "200",
                content = [Content(schema = Schema(type = "object", example = "{\"city\": \"Sion\", \"address\": \"Rue de l'industrie 23\"}"))]
            ),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()])
        ]
    )
    fun getEndpointMetaDataByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Endpoint not found.")
    }.metaData


    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @Authority.HttpEndpointCreation
    @Operation(summary = "Create a new endpoint.") // TODO: responseReference = "UUID assigned to the newly created endpoint."
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint details.", responseCode = "200", content = [Content(schema = Schema(implementation = EndpointEntity::class))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun postEndpointByFriendlyName(
        @RequestParam @Parameter(description = "Name of the endpoint.", schema = Schema(type = "string", defaultValue = "New endpoint", example = "My endpoint")) friendlyName: String = "New Endpoint",
        @Parameter(hidden = true) authentication: Authentication
    ) = authentication.userDetails().let { user ->
        val endpoint = endpointRepository.save(
            Endpoint(
                friendlyName = friendlyName
            )
        )
        userEndpointPermissionRepository.save(
            UserEndpointPermission(
                userID = user.id,
                endpointUUID = endpoint.uuid,
                permission = EndpointPermission.OWN
            )
        )
        EndpointEntity(
            uuid = endpoint.uuid,
            friendlyName = endpoint.friendlyName,
            banned = endpoint.banned,
            online = endpoint.online,
            metaData = endpoint.metaData,
            version = endpoint.dataModel.version,
            messageFormatVersion = endpoint.dataModel.messageFormatVersion,
            supportedFormats = endpoint.dataModel.supportedFormats,
            groupMemberships = endpoint.groupMemberships.map {
                endpointGroupRepository.findByGroupName(it.groupName).orElseThrow {
                    CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                }.groupName
            }.toSet()
        )
    }


    @PutMapping("/{uuid}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Update endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint data modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putEndpointByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestBody body: EndpointEntity
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.run {
            friendlyName = body.friendlyName
            banned = body.banned
            metaData.clear()
            metaData.putAll(body.metaData)
            groupMemberships.clear()
            body.groupMemberships.forEach {
                groupMemberships.add(endpointGroupRepository.findByGroupName(it).orElseThrow {
                    CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                })
            }
            endpointRepository.save(this)
        }
    }


    @PutMapping("/{uuid}/friendlyName", consumes = ["text/plain"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Update endpoint's user-friendly name.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint user-friendly name modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putEndpointFriendlyNameByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "User-friendly name to set.", required = true, schema = Schema(type = "string", example = "My endpoint")) friendlyName: String
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.friendlyName = friendlyName
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/banned", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Update if an endpoint is banned (Can not connect to broker) or not.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint banned status modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putEndpointBlockedByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "True to block endpoint, false to unblock endpoint.", required = true) blocked: Boolean
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.banned = blocked
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/configuration/properties/{key}", consumes = ["text/plain"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Update endpoint's configuration parameter.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint configuration parameter modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putEndpointConfigurationPropertyByUUIDAndPropertyName(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @PathVariable @Parameter(description = "Name of the property to change.", required = true, schema = Schema(type = "string", example = "ch.hevs.cloudio.endpoint.hostUri")) key: String,
        @RequestParam @Parameter(description = "Value to set the property to.", required = true, schema = Schema(type = "string", example = "cloudio.hevs.ch")) value: String
    ) {
        endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.let {
            it.configuration.properties[key] = value
            endpointRepository.save(it)
        }
    }


    @PutMapping("/{uuid}/configuration/logLevel", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Change an endpoint's log level.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint log level modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putEndpointConfigurationLogLevelByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Log level.", required = true) logLevel: LogLevel
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
    @Operation(summary = "Deletes the given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Endpoint deleted.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Endpoint not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun deleteEndpointByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID
    ) {
        if (!endpointRepository.existsById(uuid)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }
        endpointRepository.deleteById(uuid)
        userEndpointPermissionRepository.deleteByEndpointUUID(uuid)
        userGroupEndpointPermissionRepository.deleteByEndpointUUID(uuid)
    }
}
