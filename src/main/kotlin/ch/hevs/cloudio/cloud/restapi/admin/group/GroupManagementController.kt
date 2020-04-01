package ch.hevs.cloudio.cloud.restapi.admin.group

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Api(tags = ["Group Management"], description = "Allows an admin user to manage user groups.")
@RestController
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class GroupManagementController(
        private var groupRepository: UserGroupRepository,
        private var userRepository: UserRepository
) {
    @ApiOperation("Create a new user group.")
    @PostMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createGroupByGroupName(@PathVariable groupName: String) {
        if (groupRepository.existsById(groupName)) {
            throw CloudioHttpExceptions.Conflict("Could not create group '$groupName' - group exists.")
        }
        groupRepository.save(UserGroup(groupName))
    }

    @ApiOperation("Get user group information.")
    @GetMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.OK)
    fun getGroupByGroupName(@PathVariable groupName: String) = GroupEntity(
            groupRepository.findById(groupName).orElseThrow {
                CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
            })

    @ApiOperation("Modify user group.")
    @PutMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateGroupByGroupName(@PathVariable groupName: String, @RequestBody body: GroupEntity) {
        if (groupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match.")
        }
        groupRepository.findById(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.also {
            body.updateUserGroup(it)
            groupRepository.save(it)
        }
    }

    @ApiOperation("Deletes user group.")
    @DeleteMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteGroupByGroupName(@PathVariable groupName: String) {
        if (!groupRepository.existsById(groupName)) {
            throw CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }
        groupRepository.deleteById(groupName)
        userRepository.findByGroupMembership(groupName).forEach {
            it.userGroups = it.userGroups.toMutableSet().apply { remove(groupName) }
            userRepository.save(it)
        }
    }

    @ApiOperation("List all user group names.")
    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups() = groupRepository.findAll().map { it.userGroupName }
}
