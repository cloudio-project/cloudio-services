package ch.hevs.cloudio.cloud.restapi.endpoint.permission

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Api(
        tags = ["Endpoint Permissions"],
        description = "Allows users to manage permissions to their owned endpoints or endpoints they have the GRANT permission."
)
@RequestMapping("/api/v1/endpoints")
class EndpointPermissionController(
        private val userRepository: UserRepository,
        private val userGroupRepository: UserGroupRepository,
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository
) {
    private val antMatcher = AntPathMatcher()

    @PutMapping("/{uuid}/grant")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @ApiOperation("Grant permission to whole endpoint to another user.")
    fun grantPermissionByUUID(
            @PathVariable @ApiParam("UUID of endpoint.", required = true) uuid: UUID,
            @RequestParam @ApiParam("User name to grant the permission to.", required = false) userName: String?,
            @RequestParam @ApiParam("Group name to grant the permission to.", required = false) groupName: String?,
            @RequestParam @ApiParam("Permission to grant.") permission: EndpointPermission
    ) = when {
        permission == EndpointPermission.GRANT -> throw CloudioHttpExceptions.BadRequest("OWM permission can not be granted.")
        userName != null -> userRepository.findByUserName(userName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("User not found.")
        }.id.let {userID ->
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

    @PutMapping("/{uuid}/grant/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).GRANT)")
    @ApiOperation("Grant permission to element of endpoint's data model to another user.")
    fun grantModelPermissionByUUID(
            @PathVariable @ApiParam("UUID of endpoint.", required = true) uuid: UUID,
            @RequestParam @ApiParam("User name to grant the permission to.", required = false) userName: String?,
            @RequestParam @ApiParam("Group name to grant the permission to.", required = false) groupName: String?,
            @RequestParam @ApiParam("Permission to grant.") permission: EndpointModelElementPermission,
            @ApiIgnore request: HttpServletRequest
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
