package ch.hevs.cloudio.cloud.restapi.endpoint.group

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.endpoint.management.EndpointListEntity
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Group Management", description = "Allows a user to manage endpoint groups.")
@RequestMapping("/api/v1/endpoints")
@SecurityRequirements(value = [
    SecurityRequirement(name = "basicAuth"),
    SecurityRequirement(name = "tokenAuth")
])
class EndpointGroupManagementController(
        private var endpointGroupRepository: EndpointGroupRepository,
        private var endpointRepository: EndpointRepository,
        private var userEndpointGroupPermissionRepository: UserEndpointGroupPermissionRepository,
        private var userEndpointGroupModelElementPermissionRepository: UserEndpointGroupPermissionRepository,
        private var permissionManager: CloudioPermissionManager
) {
    @Operation(summary = "List all endpoint group names.")
    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups(
            @Parameter(hidden = true) authentication: Authentication?
    ) = if (authentication == null) throw CloudioHttpExceptions.Forbidden("No user.")
    else permissionManager.resolveEndpointGroupsPermissions(authentication.userDetails()).mapNotNull { perm ->
        endpointGroupRepository.findById(perm.endpointGroupID).orElse(null)?.let {
            EndpointGroupListEntity(
                name = it.groupName,
                permission = perm.permission
            )
        }
    }

    @Operation(summary = "Create a new endpoint group.")
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Authority.HttpEndpointCreation
    @Transactional
    fun createGroup(@RequestBody body: EndpointGroupEntity, @Parameter(hidden = true) authentication: Authentication?) {
        if (authentication == null) throw CloudioHttpExceptions.Forbidden("No user.")

        if (endpointGroupRepository.existsByGroupName(body.name)) {
            throw CloudioHttpExceptions.Conflict("Group '${body.name}' exists.")
        }
        endpointGroupRepository.save(EndpointGroup(
                groupName = body.name,
                metaData = body.metaData.toMutableMap()
        ))

        //The user creating the group has the OWN permission on it
        endpointGroupRepository.findByGroupName(body.name).ifPresent{
            userEndpointGroupPermissionRepository.save(
                UserEndpointGroupPermission(
                        userID = authentication.userDetails().id,
                        endpointGroupID = it.id,
                        permission = EndpointPermission.OWN
                )
            )
        }
    }

    @Operation(summary = "Get endpoint group information.")
    @GetMapping("/groups/{endpointGroupName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @PreAuthorize("hasPermission(#endpointGroupName, \"EndpointGroup\",T(ch.hevs.cloudio.cloud.security.EndpointPermission).ACCESS)")
    fun getGroupByGroupName(
            @PathVariable endpointGroupName: String,
            @Parameter(hidden = true) authentication: Authentication?
    ) = if (authentication == null) throw CloudioHttpExceptions.Forbidden("No user.")
    else endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$endpointGroupName' not found.")
    }.run {
        EndpointGroupEntity(
                name = endpointGroupName,
                metaData = metaData,
                endpoints = endpointRepository.findByGroupMembershipsContains(this).map {
                    EndpointListEntity(
                            uuid = it.uuid,
                            friendlyName = it.friendlyName,
                            banned = it.banned,
                            online = it.online,
                            permission = permissionManager.resolveEndpointPermission(authentication.userDetails(), it.uuid)
                    )
                }
        )
    }

    @Operation(summary = "Modify endpoint group.")
    @PutMapping("/groups/{endpointGroupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateGroupByGroupName(
            @PathVariable endpointGroupName: String,
            @RequestBody body: EndpointGroupEntity,
            @Parameter(hidden = true) authentication: Authentication?
    )
    {
        if (authentication == null) throw CloudioHttpExceptions.Forbidden("No user.")

        val endpointGroup = endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Endpoint group not found.")
        }
        userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(authentication.userDetails().id,
        endpointGroup.id).ifPresentOrElse(
        {
            if(!it.permission.fulfills(EndpointPermission.CONFIGURE)){
                throw CloudioHttpExceptions.Forbidden("User does not have the CONFIGURE permission on this endpoint group.")
            }
        },
        {
            throw CloudioHttpExceptions.Forbidden("User does not have the CONFIGURE permission on this endpoint group.")
        }
        )
        if (endpointGroupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match.")
        }
        endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$endpointGroupName' not found.")
        }.run {
            metaData = body.metaData.toMutableMap()
            endpointGroupRepository.save(this)
        }
    }

    @Operation(summary = "Deletes endpoint group.")
    @DeleteMapping("/groups/{endpointGroupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteGroupByGroupName(
        @PathVariable endpointGroupName: String,
        @Parameter(hidden = true) authentication: Authentication?
    )
    {
        if (authentication == null) throw CloudioHttpExceptions.Forbidden("No user.")

        val endpointGroup = endpointGroupRepository.findByGroupName(endpointGroupName).orElseThrow {
            throw CloudioHttpExceptions.NotFound("Endpoint group not found.")
        }
        userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(authentication.userDetails().id,
                endpointGroup.id).ifPresentOrElse(
                {
                    if(!it.permission.fulfills(EndpointPermission.OWN)){
                        throw CloudioHttpExceptions.Forbidden("User does not have the OWN permission on this endpoint group.")
                    }
                },
                {
                    throw CloudioHttpExceptions.Forbidden("User does not have the OWN permission on this endpoint group.")
                }
        )
        endpointRepository.findByGroupMembershipsContains(endpointGroup).forEach {
            it.groupMemberships.remove(endpointGroup)
            endpointRepository.save(it)
        }

        userEndpointGroupPermissionRepository.deleteByEndpointGroupID(endpointGroup.id)

        userEndpointGroupModelElementPermissionRepository.deleteByEndpointGroupID(endpointGroup.id)

        endpointGroupRepository.deleteByGroupName(endpointGroupName)
    }
}
