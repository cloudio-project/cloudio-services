package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@Profile("rest-api")
@Tag(name = "User Management", description = "Allows an admin user to manage users.")
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserManagementController(
        private var userRepository: UserRepository,
        private var groupRepository: UserGroupRepository,
        private var passwordEncoder: PasswordEncoder
) {
    @Operation(summary = "List all users.")
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    fun getAllUsers() = userRepository.findAll().map {
        ListUserEntity(
                name = it.userName,
                email = it.emailAddress.toString(),
                authorities = it.authorities,
                banned = it.banned
        )
    }

    @Operation(summary = "Create a new user.")
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun createUser(@RequestBody body: PostUserEntity) {
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
                authorities = body.authorities.toMutableSet(),
                banned = body.banned,
                groupMemberships = body.groupMemberships.map {
                    groupRepository.findByGroupName(it).orElseThrow {
                        CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                    }
                }.toMutableSet(),
                metaData = body.metaData.toMutableMap()
        ))
    }

    @Operation(summary = "Get user information.")
    @GetMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    fun getUserByUserName(@PathVariable userName: String) = userRepository.findByUserName(userName).orElseThrow {
        CloudioHttpExceptions.NotFound("User '$userName' not found.")
    }.run {
        UserEntity(
                name = userName,
                email = emailAddress.toString(),
                authorities = authorities,
                banned = banned,
                groupMemberships = groupMemberships.map { it.groupName }.toSet(),
                metadata = metaData
        )
    }

    @Operation(summary = "Modify user information.")
    @PutMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun updateUserByUserName(@PathVariable userName: String, @RequestBody body: UserEntity) {
        if (userName != body.name) {
            throw CloudioHttpExceptions.BadRequest("User name in URL and body do not match.")
        }
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
            groupMemberships.clear()
            groupMemberships.addAll(body.groupMemberships.map {
                groupRepository.findByGroupName(it).orElseThrow {
                    CloudioHttpExceptions.NotFound("Group '$it' does not exist.")
                }
            })
            metaData = body.metadata.toMutableMap()
            userRepository.save(this)
        }
    }

    @Operation(summary = "Change user's password.")
    @PutMapping("/users/{userName}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changeUserPassword(@PathVariable userName: String, @RequestParam password: String) {
        userRepository.findByUserName(userName).orElseThrow {
            CloudioHttpExceptions.NotFound("Can not change user's password - User '$userName' not found.")
        }.let {
            it.password = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }

    @Operation(summary = "Delete user.")
    @DeleteMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    fun deleteUserByUserName(@PathVariable userName: String) {
        if (!userRepository.existsByUserName(userName)) {
            throw CloudioHttpExceptions.NotFound("Can not delete user - User '$userName' not found.")
        }
        userRepository.deleteByUserName(userName)
    }
}
