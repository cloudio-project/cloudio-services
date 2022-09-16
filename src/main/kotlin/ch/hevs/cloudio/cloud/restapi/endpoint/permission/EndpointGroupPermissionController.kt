package ch.hevs.cloudio.cloud.restapi.endpoint.permission

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Permissions")
@RequestMapping("/api/v1/endpoints/groups")
@SecurityRequirement(name = "basicAuth")
class EndpointGroupPermissionController(
    private val userRepository: UserRepository,
    private val userEndpointGroupPermissionRepository: UserEndpointGroupPermissionRepository,
    private val endpointGroupRepository: EndpointGroupRepository,
    private val userEndpointGroupModelElementPermissionRepository: UserEndpointGroupPermissionRepository,
    private val userGroupRepository: UserGroupRepository,
    private val userGroupEndpointGroupPermissionRepository: UserGroupEndpointGroupPermissionRepository,
    private val userGroupEndpointGroupModelElementPermissionRepository: UserGroupEndpointGroupPermissionRepository
) {
    private val antMatcher = AntPathMatcher()

    @PutMapping("/{endpointGroupName}/grant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Grant permission to an endpoint group to another user or user group.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Permission granted to user or group.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "OWN permission can not be granted or either userName or groupName has to be provided.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "User or group not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun grantPermissionByEndpointGroup(
        @Parameter(hidden = true) authentication: Authentication,
        @PathVariable @Parameter(description = "The endpoint group name.", required = true) endpointGroupName: String,
        @RequestParam @Parameter(description = "User name to grant the permission to.", required = false) userName: String?,
        @RequestParam @Parameter(description = "User group name to grant the permission to.", required = false) userGroupName: String?,
        @RequestParam @Parameter(
            description = "Permission to grant.",
            schema = Schema(allowableValues = ["DENY", "ACCESS", "BROWSE", "READ", "WRITE", "CONFIGURE", "GRANT"])
        ) permission: EndpointPermission
    ) {
        if (permission == EndpointPermission.OWN) {
            throw CloudioHttpExceptions.Forbidden("OWN permission can not be granted.")
        }

        val endpointGroup = endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Endpoint group not found.")
        }

        //get the authenticated user permission
        userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(authentication.userDetails().id, endpointGroup.id)
            .ifPresentOrElse(
                {
                    if (!it.permission.fulfills(EndpointPermission.GRANT)) {
                        throw CloudioHttpExceptions.Forbidden("User does not have the GRANT permission on this endpoint group.")
                    }
                },
                {
                    throw CloudioHttpExceptions.Forbidden("User does not have the GRANT permission on this endpoint group.")
                }
            )

        if (userName != null) {
            val user = userRepository.findByUserName(userName).orElseThrow {
                throw CloudioHttpExceptions.NotFound("User not found.")
            }

            when (permission) {
                EndpointPermission.DENY -> userEndpointGroupPermissionRepository.deleteByUserIDAndEndpointGroupID(user.id, endpointGroup.id)
                else -> {
                    val endpointGroupPermission = userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(user.id, endpointGroup.id)
                        .orElse(UserEndpointGroupPermission(user.id, endpointGroup.id, permission))
                    endpointGroupPermission.permission = permission
                    userEndpointGroupPermissionRepository.save(endpointGroupPermission)
                }
            }
        }
        if (userGroupName != null) {
            val userGroup = userGroupRepository.findByGroupName(userGroupName).orElseThrow {
                throw CloudioHttpExceptions.NotFound("User Group not found.")
            }

            when (permission) {
                EndpointPermission.DENY -> userGroupEndpointGroupPermissionRepository.deleteByUserGroupIDAndEndpointGroupID(userGroup.id, endpointGroup.id)
                else -> {
                    val endpointGroupPermission = userGroupEndpointGroupPermissionRepository.findByUserGroupIDAndEndpointGroupID(userGroup.id, endpointGroup.id)
                        .orElse(UserGroupEndpointGroupPermission(userGroup.id, endpointGroup.id, permission))
                    endpointGroupPermission.permission = permission
                    userGroupEndpointGroupPermissionRepository.save(endpointGroupPermission)
                }
            }
        }
        if (userGroupName == null && userName == null) {
            throw CloudioHttpExceptions.Forbidden("Either userName or groupName has to be provided.")
        }
    }

    @PutMapping("/{endpointGroupName}/grant/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Grant permission to element of endpoint group's data model to another user or user group.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Permission granted to user or group.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "Either userName or groupName has to be provided.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "User or group not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun grantModelPermissionByUUID(
        @Parameter(hidden = true) authentication: Authentication,
        @PathVariable @Parameter(description = "The endpoint group name.", required = true) endpointGroupName: String,
        @RequestParam @Parameter(description = "User name to grant the permission to.", required = false) userName: String?,
        @RequestParam @Parameter(description = "Group name to grant the permission to.", required = false) userGroupName: String?,
        @RequestParam @Parameter(description = "Permission to grant.") permission: EndpointModelElementPermission,
        @Parameter(hidden = true) request: HttpServletRequest
    ) {
        val endpointGroup = endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Endpoint group not found.")
        }

        //get the authenticated user permission
        userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(authentication.userDetails().id, endpointGroup.id)
            .ifPresentOrElse(
                {
                    if (!it.permission.fulfills(EndpointPermission.GRANT)) {
                        throw CloudioHttpExceptions.Forbidden("User does not have the GRANT permission on this endpoint group.")
                    }
                },
                {
                    throw CloudioHttpExceptions.Forbidden("User does not have the GRANT permission on this endpoint group.")
                }
            )

        if (userName != null) {
            val user = userRepository.findByUserName(userName).orElseThrow {
                throw CloudioHttpExceptions.NotFound("User not found.")
            }

            val modelPath = antMatcher.extractPathWithinPattern("/api/v1/endpoints/groups/$endpointGroupName/grant/**", request.requestURI)

            when (permission) {
                EndpointModelElementPermission.DENY -> userEndpointGroupModelElementPermissionRepository
                    .findByUserIDAndEndpointGroupID(user.id, endpointGroup.id).ifPresent {
                        it.modelPermissions.remove(modelPath)
                        userEndpointGroupModelElementPermissionRepository.save(it)
                    }

                else -> {
                    userEndpointGroupModelElementPermissionRepository.findByUserIDAndEndpointGroupID(user.id, endpointGroup.id)
                        .orElse(UserEndpointGroupPermission(user.id, endpointGroup.id, EndpointPermission.ACCESS)).let {
                            it.modelPermissions[modelPath] = permission
                            userEndpointGroupModelElementPermissionRepository.save(it)
                        }
                }
            }
        }

        if (userGroupName != null) {
            val userGroup = userGroupRepository.findByGroupName(userGroupName).orElseThrow {
                throw CloudioHttpExceptions.NotFound("User group not found.")
            }

            val modelPath = antMatcher.extractPathWithinPattern("/api/v1/endpoints/groups/$endpointGroupName/grant/**", request.requestURI)

            when (permission) {
                EndpointModelElementPermission.DENY -> userGroupEndpointGroupModelElementPermissionRepository
                    .findByUserGroupIDAndEndpointGroupID(userGroup.id, endpointGroup.id).ifPresent {
                        it.modelPermissions.remove(modelPath)
                        userGroupEndpointGroupModelElementPermissionRepository.save(it)
                    }

                else -> {
                    userGroupEndpointGroupModelElementPermissionRepository.findByUserGroupIDAndEndpointGroupID(userGroup.id, endpointGroup.id)
                        .orElse(UserGroupEndpointGroupPermission(userGroup.id, endpointGroup.id, EndpointPermission.ACCESS)).let {
                            it.modelPermissions[modelPath] = permission
                            userGroupEndpointGroupModelElementPermissionRepository.save(it)
                        }
                }
            }
        }
        if (userGroupName == null && userName == null) {
            throw CloudioHttpExceptions.Forbidden("Username and groupName cannot be null.")
        }
    }
}