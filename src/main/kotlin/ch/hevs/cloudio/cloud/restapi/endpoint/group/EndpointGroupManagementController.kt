package ch.hevs.cloudio.cloud.restapi.endpoint.group

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import javax.transaction.Transactional

@RestController
@Profile("rest-api")
@Api(tags = ["Endpoint Group Management"], description = "Allows a user to manage endpoint groups.")
@RequestMapping("/api/v1/endpoints")
class EndpointGroupManagementController(
        private var endpointGroupRepository: EndpointGroupRepository,
        private var endpointRepository: EndpointRepository,
        private var userEndpointGroupPermissionRepository: UserEndpointGroupPermissionRepository,
        private var userEndpointGroupModelElementPermissionRepository: UserEndpointGroupPermissionRepository
) {
    @ApiOperation("List all endpoint group names.")
    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups() = endpointGroupRepository.findAll().map { it.groupName }

    @ApiOperation("Create a new endpoint group.")
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun createGroup(@RequestBody body: EndpointGroupEntity, @ApiIgnore authentication: Authentication) {
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

    @ApiOperation("Get endpoint group information.")
    @GetMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getGroupByGroupName(@PathVariable groupName: String) = endpointGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        EndpointGroupEntity(
                name = groupName,
                metaData = metaData
        )
    }

    @ApiOperation("Modify endpoint group.")
    @PutMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateGroupByGroupName(
            @PathVariable groupName: String,
            @RequestBody body: EndpointGroupEntity,
            @ApiIgnore authentication: Authentication
    )
    {
        val endpointGroup = endpointGroupRepository.findByGroupName(groupName).orElseThrow {
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
        if (groupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match.")
        }
        endpointGroupRepository.findByGroupName(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.run {
            metaData = body.metaData.toMutableMap()
            endpointGroupRepository.save(this)
        }
    }

    @ApiOperation("Deletes endpoint group.")
    @DeleteMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteGroupByGroupName(@PathVariable groupName: String, @ApiIgnore authentication: Authentication)
    {
        val endpointGroup = endpointGroupRepository.findByGroupName(groupName).orElseThrow {
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

        endpointGroupRepository.deleteByGroupName(groupName)
    }
}
