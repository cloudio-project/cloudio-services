package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Profile("rest-api")
@Tag(name = "User Management", description = "Allows a user with the authority HTTP_ADMIN to manage users.")
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserManagementController(
        private var userRepository: UserRepository,
        private var groupRepository: UserGroupRepository,
        private var passwordEncoder: PasswordEncoder
) {
    @GetMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List all users.")
    @ApiResponses(value = [
        ApiResponse(description = "List of all users.", responseCode = "200", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ListUserEntity::class)))]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun getAllUsers() = userRepository.findAll().map { ListUserEntity(it) }

    @PostMapping("/users",  consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Create a new user.")
    @ApiResponses(value = [
        ApiResponse(description = "User was created", responseCode = "204"),
        ApiResponse(description = "User with same username exists", responseCode = "409"),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun postUser(
        @RequestBody body: PostUserEntity
    ) {
        if (userRepository.existsByUserName(body.name)) {
            throw CloudioHttpExceptions.Conflict("User '${body.name}' exists.")
        }
        userRepository.save(User(
                userName = body.name,
                emailAddress = EmailAddress(body.email).apply {
                    if (!isValid()) {
                        throw CloudioHttpExceptions.BadRequest("Invalid Email address '${body.email}'.")
                    }
                },
                password = passwordEncoder.encode(body.password),
                authorities = (body.authorities ?: Authority.DEFAULT_AUTHORITIES).toMutableSet(),
                banned = body.banned ?: false,
                groupMemberships = (body.groupMemberships ?: emptySet()).map {
                    groupRepository.findByGroupName(it).orElseThrow {
                        CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                    }
                }.toMutableSet(),
                metaData = (body.metaData ?: emptyMap()).toMutableMap()
        ))
    }

    @GetMapping("/users/{userName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @Operation(summary = "Get user information.")
    @ApiResponses(value = [
        ApiResponse(description = "User details.", responseCode = "200", content = [Content(mediaType = "application/json", schema = Schema(implementation = UserEntity::class))]),
        ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun getUserByUserName(
        @PathVariable @Parameter(description = "Username") userName: String
    ) = userRepository.findByUserName(userName).orElseThrow {
        CloudioHttpExceptions.NotFound("User '$userName' not found.")
    }.let { UserEntity(it) }

    @PutMapping("/users/{userName}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Modify user information.")
    @ApiResponses(value = [
        ApiResponse(description = "User information modified.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun updateUserByUserName(
        @PathVariable @Parameter(description = "Username") userName: String,
        @RequestBody body: UserEntity
    ) {
        body.name = userName
        userRepository.findByUserName(userName).orElseThrow {
            CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }.run {
            emailAddress = EmailAddress(body.email).apply {
                if (!isValid()) {
                    throw CloudioHttpExceptions.BadRequest("Invalid Email address '${body.email}'.")
                }
            }
            authorities.apply {
                clear()
                addAll(body.authorities)
            }
            banned = body.banned
            groupMemberships.apply {
                clear()
                addAll(body.groupMemberships.map {
                    groupRepository.findByGroupName(it).orElseThrow {
                        CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                    }
                })
            }
            metaData = body.metadata.toMutableMap()
            userRepository.save(this)
        }
    }

    @PutMapping("/users/{userName}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change user's password.")
    @ApiResponses(value = [
        ApiResponse(description = "User password changed.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun changeUserPassword(
        @PathVariable userName: String,
        @RequestParam password: String
    ) {
        userRepository.findByUserName(userName).orElseThrow {
            CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }.let {
            it.password = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }

    @DeleteMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    @Operation(summary = "Delete user.")
    @ApiResponses(value = [
        ApiResponse(description = "User deleted.", responseCode = "204", content = [Content()]),
        ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun deleteUserByUserName(@PathVariable userName: String) {
        if (!userRepository.existsByUserName(userName)) {
            throw CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }
        userRepository.deleteByUserName(userName)
    }
}
