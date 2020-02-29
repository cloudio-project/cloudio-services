package ch.hevs.cloudio.cloud.restapi.admin.group

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class GroupManagementController(
        private var groupRepository: UserGroupRepository,
        private var userRepository: UserRepository
) {
    @PostMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun postGroupByGroupName(@PathVariable groupName: String) {
        if (groupRepository.existsById(groupName)) {
            throw CloudioHttpExceptions.Conflict("Could not create group '$groupName' - group exists.")
        }
        groupRepository.save(UserGroup(groupName))
    }

    @GetMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.OK)
    fun getGroupByGroupName(@PathVariable groupName: String) = GroupBody(
            groupRepository.findById(groupName).orElseThrow {
                CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
            })

    @PutMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putGroupByGroupName(@PathVariable groupName: String, @RequestBody body: GroupBody) {
        if (groupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match")
        }
        groupRepository.findById(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.also {
            body.updateUserGroup(it)
            groupRepository.save(it)
        }
    }

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

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups() = groupRepository.findAll().map { it.userGroupName }
}
