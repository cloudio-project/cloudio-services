package ch.hevs.cloudio.cloud.restapi.endpoint.permission

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.AccessTokenManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Permissions", description = "Allows users to manage permissions for their owned endpoints or endpoints for which they have the GRANT permission.")
@RequestMapping("/api/v1/endpoints")
@SecurityRequirements(value = [
    SecurityRequirement(name = "basicAuth"),
    SecurityRequirement(name = "tokenAuth")
])
class EndpointPermissionController(
    private val userRepository: UserRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
    private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository,
    private val accessTokenManager: AccessTokenManager
) {
    private val antMatcher = AntPathMatcher()

    @PutMapping("/{uuid}/grant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Grant permission to the endpoint to another user or user group.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Permission granted to user or group.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "OWN permission can not be granted or either userName or groupName has to be provided.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "User or group not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun grantPermissionByUUID(
        @PathVariable @Parameter(description = "UUID of endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "User name to grant the permission to.", required = false) userName: String?,
        @RequestParam @Parameter(description = "Group name to grant the permission to.", required = false) groupName: String?,
        @RequestParam @Parameter(
            description = "Permission to grant.",
            schema = Schema(allowableValues = ["DENY", "ACCESS", "BROWSE", "READ", "WRITE", "CONFIGURE", "GRANT"])
        ) permission: EndpointPermission
    ) = when {
        permission == EndpointPermission.OWN -> throw CloudioHttpExceptions.BadRequest("OWN permission can not be granted.")
        userName != null -> userRepository.findByUserName(userName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("User not found.")
        }.id.let { userID ->
            when {
                permission == EndpointPermission.DENY -> userEndpointPermissionRepository.deleteByUserIDAndEndpointUUID(userID, uuid)
                //if user has a write permission or higher, the modelPermissions are useless
                permission.fulfills(EndpointPermission.WRITE) -> {
                    val endpointPermission = userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, uuid).orElse(UserEndpointPermission(userID, uuid, permission))
                    endpointPermission.modelPermissions.clear()
                    userEndpointPermissionRepository.save(endpointPermission)
                }

                else -> {
                    val endpointPermission = userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, uuid).orElse(UserEndpointPermission(userID, uuid, permission))
                    endpointPermission.permission = permission
                    userEndpointPermissionRepository.save(endpointPermission)
                }
            }
        }

        groupName != null -> userGroupRepository.findByGroupName(groupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Group not found.")
        }.id.let { groupID ->
            when {
                permission == EndpointPermission.DENY -> userGroupEndpointPermissionRepository.deleteByUserGroupIDAndEndpointUUID(groupID, uuid)
                //if the usergroup has a write permission or higher, the modelPermissons are useless
                permission.fulfills(EndpointPermission.WRITE) -> {
                    val endpointPermission = userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, uuid).orElse(UserGroupEndpointPermission(groupID, uuid, permission))
                    endpointPermission.modelPermissions.clear()
                    userGroupEndpointPermissionRepository.save(endpointPermission)
                }

                else -> {
                    val endpointPermission = userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, uuid).orElse(UserGroupEndpointPermission(groupID, uuid, permission))
                    endpointPermission.permission = permission
                    userGroupEndpointPermissionRepository.save(endpointPermission)
                }
            }
        }

        else -> throw CloudioHttpExceptions.BadRequest("Either userName or groupName has to be provided.")
    }

    @GetMapping("/{uuid}/token", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Generate an access token for the given endpoint.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Access token generated.", responseCode = "200", content = [Content(schema = Schema(type = "string",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJlbmRwb2ludCIsInV1aWQiOiI4ZGQwZjgzNS1jZjQwLTRiM2YtYjRmYi0xMDhiMDBjYmI2ZWMiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6MTUyNjIzOTAyMn0.dPzU5suQ_UKpbeQcXbtIbPahZK04tEa4DOxdE1zc3ew"))]),
            ApiResponse(description = "Token can only be generated for READ, WRITE and CONFIGURE permission.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getAccessTokenByUUID(
        @PathVariable @Parameter(description = "UUID of endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Permission to grant.", schema = Schema(allowableValues = ["READ", "WRITE", "CONFIGURE"])) permission: EndpointPermission,
        @RequestParam @Parameter(description = "Expiration date and time for the token in ISO-8601 format (yyyy-MM-dd HH:mm:ss).", schema = Schema(type = "string", example = "2042-01-01 07:15:00")) expires: Date,
        @Parameter(hidden = true) authentication: Authentication?
    ) = authentication?.let {
        when (permission) {
            EndpointPermission.READ, EndpointPermission.WRITE, EndpointPermission.CONFIGURE -> accessTokenManager.generateEndpointPermissionAccessToken(it.userDetails(), uuid, permission, expires)
            else -> throw CloudioHttpExceptions.BadRequest("Token can only be generated for READ, WRITE and CONFIGURE permission.")
        }
    } ?: throw CloudioHttpExceptions.Forbidden("User not found.")

    @PutMapping("/{uuid}/grant/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @Operation(summary = "Grant permission to element of endpoint's data model to another user or user group.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Permission granted to user or group.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Either userName or groupName has to be provided.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "User or group not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun grantModelPermissionByUUID(
        @PathVariable @Parameter(description = "UUID of endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "User name to grant the permission to.", required = false) userName: String?,
        @RequestParam @Parameter(description = "Group name to grant the permission to.", required = false) groupName: String?,
        @RequestParam @Parameter(description = "Permission to grant.") permission: EndpointModelElementPermission,
        @Parameter(hidden = true) request: HttpServletRequest
    ) = when {
        userName != null -> userRepository.findByUserName(userName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("User not found.")
        }.id.let { userID ->
            when (permission) {
                EndpointModelElementPermission.DENY -> userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, uuid).ifPresent {
                    it.modelPermissions.remove(antMatcher.extractPathWithinPattern("/api/v1/endpoints/$uuid/grant/**", request.requestURI))
                    userEndpointPermissionRepository.save(it)
                }

                else -> userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, uuid).orElse(UserEndpointPermission(userID, uuid, EndpointPermission.ACCESS)).let {
                    it.modelPermissions[antMatcher.extractPathWithinPattern("/api/v1/endpoints/$uuid/grant/**", request.requestURI)] = permission
                    userEndpointPermissionRepository.save(it)
                }
            }
        }

        groupName != null -> userGroupRepository.findByGroupName(groupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Group not found.")
        }.id.let { groupID ->
            when (permission) {
                EndpointModelElementPermission.DENY -> userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, uuid).ifPresent {
                    it.modelPermissions.remove(antMatcher.extractPathWithinPattern("/api/v1/endpoints/$uuid/grant/**", request.requestURI))
                    userGroupEndpointPermissionRepository.save(it)
                }

                else -> userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, uuid).orElse(UserGroupEndpointPermission(groupID, uuid, EndpointPermission.ACCESS)).let {
                    it.modelPermissions[antMatcher.extractPathWithinPattern("/api/v1/endpoints/$uuid/grant/**", request.requestURI)] = permission
                    userGroupEndpointPermissionRepository.save(it)
                }
            }
        }

        else -> throw CloudioHttpExceptions.BadRequest("Either userName or groupName has to be provided.")
    }
}
