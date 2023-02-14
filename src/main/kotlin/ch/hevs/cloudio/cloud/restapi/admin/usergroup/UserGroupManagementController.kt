package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import ch.hevs.cloudio.cloud.dao.UserGroup
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.admin.user.ListUserEntity
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@RestController
@Profile("rest-api")
@Tag(name = "User Management")
@RequestMapping("/api/v1/admin")
@SecurityRequirements(value = [
    SecurityRequirement(name = "basicAuth"),
    SecurityRequirement(name = "tokenAuth")
])
@Authority.HttpAdmin
class UserGroupManagementController(
        private var userGroupRepository: UserGroupRepository,
        private var userRepository: UserRepository
) {
    @GetMapping("/groups", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List all user group names.")
    @ApiResponses(value = [
        ApiResponse(description = "List of groups", responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(type = "string", example = "IT Department")))]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun getAllGroups() = userGroupRepository.findAll().map { it.groupName }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Create a new user group.")
    @ApiResponses(value = [
        ApiResponse(description = "User group was created", responseCode = "204"),
        ApiResponse(description = "User group with same name exists", responseCode = "409"),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun postGroup(
        @RequestBody body: PostUserGroupEntity
    ) {
        if (userGroupRepository.existsByGroupName(body.name)) {
            throw CloudioHttpExceptions.Conflict("Group '${body.name}' exists.")
        }
        userGroupRepository.save(UserGroup(
                groupName = body.name,
                metaData = (body.metaData ?: emptyMap()).toMutableMap()
        ))
    }

    @GetMapping("/groups/{groupName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @Operation(summary = "Get user group information.")
    @ApiResponses(value = [
        ApiResponse(description = "User group details.", responseCode = "200", content = [Content(schema = Schema(implementation = UserGroupEntity::class))]),
        ApiResponse(description = "User group not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun getGroupByGroupName(
        @PathVariable @Parameter(description = "Name of the group to get details.") groupName: String
    ) = userGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("User group '$groupName' not found.")
    }.let { userGroup ->
        UserGroupEntity(
                name = userGroup.groupName,
                metaData = userGroup.metaData,
                users = userRepository.findByGroupMembershipsContains(userGroup).map {ListUserEntity(it)}
        )
    }

    @PutMapping("/groups/{groupName}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Modify user group.")
    @ApiResponses(value = [
        ApiResponse(description = "User group information modified.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "User group not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun updateGroupByGroupName(
        @PathVariable @Parameter(description = "Name of the group to modify.") groupName: String,
        @RequestBody @Parameter(description = "New group parameters.") body: UserGroupEntity
    ) {
        body.name = groupName
        userGroupRepository.findByGroupName(groupName).orElseThrow {
            CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
        }.run {
            metaData = body.metaData.toMutableMap()
            userGroupRepository.save(this)
        }
    }

    @DeleteMapping("/groups/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Deletes user group.")
    @ApiResponses(value = [
        ApiResponse(description = "User group deleted.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "User group not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun deleteGroupByGroupName(
        @PathVariable @Parameter(description = "Name of the group to delete.") groupName: String
    ) = userGroupRepository.findByGroupName(groupName).orElseThrow {
        CloudioHttpExceptions.NotFound("Group '$groupName' not found.")
    }.run {
        userRepository.findByGroupMembershipsContains(this).forEach {
            it.groupMemberships.remove(this)
            userRepository.save(it)
        }
        userGroupRepository.delete(this)
    }
}
