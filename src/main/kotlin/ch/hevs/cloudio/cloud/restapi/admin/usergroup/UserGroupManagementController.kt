package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import ch.hevs.cloudio.cloud.dao.UserGroup
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@CrossOrigin(origins = ["*"])
@RestController
@Profile("rest-api")
@Api(tags = ["Group Management"], description = "Allows an admin user to manage user groups.")
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserGroupManagementController(
        private var userGroupRepository: UserGroupRepository,
        private var userRepository: UserRepository
) {
    @ApiOperation("List all user group names.")
    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK)
    fun getAllGroups() = userGroupRepository.findAll().map { it.groupName }

    @ApiOperation("Create a new user group.")
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun createGroup(@RequestBody body: PostUserGroupEntity) {
        if (userGroupRepository.existsByGroupName(body.name)) {
            throw CloudioHttpExceptions.Conflict("Group '${body.name}' exists.")
        }
        userGroupRepository.save(UserGroup(
                groupName = body.name,
                metaData = body.metaData.toMutableMap()
        ))
    }

    @ApiOperation("Get user group information.")
    @GetMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getGroupByGroupName(@PathVariable groupName: String) = userGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        UserGroupEntity(
                name = groupName,
                metaData = metaData
        )
    }

    @ApiOperation("Modify user group.")
    @PutMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateGroupByGroupName(@PathVariable groupName: String, @RequestBody body: UserGroupEntity) {
        if (groupName != body.name) {
            throw CloudioHttpExceptions.Conflict("Group name in URL and body do not match.")
        }
        userGroupRepository.findByGroupName(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.run {
            metaData = body.metaData.toMutableMap()
            userGroupRepository.save(this)
        }
    }

    @ApiOperation("Deletes user group.")
    @DeleteMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteGroupByGroupName(@PathVariable groupName: String) = userGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        userRepository.findByGroupMembershipsContains(this).forEach {
            it.groupMemberships.remove(this)
            userRepository.save(it)
        }
        userGroupRepository.delete(this)
    }
}
