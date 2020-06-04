package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.hibernate.Hibernate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.transaction.Transactional

@Api(tags = ["User Management"], description = "Allows an admin user to manage users.")
@RestController
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserManagementController(
        private var userRepository: UserRepository,
        private var groupRepository: UserGroupRepository,
        private var passwordEncoder: PasswordEncoder
) {
    @ApiOperation("List all users.")
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    fun getAllUsers() = userRepository.findAll().map { it.userName }

    @ApiOperation("Create a new user.")
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

    @ApiOperation("Get user information.")
    @GetMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getUserByUserName(@PathVariable userName: String) = userRepository.findByUserName(userName).orElseThrow {
        CloudioHttpExceptions.NotFound("User '$userName' not found.")
    }.run {
        Hibernate.initialize(groupMemberships)
        UserEntity(
                name = userName,
                email = emailAddress.toString(),
                authorities = authorities,
                banned = banned,
                groupMemberships = groupMemberships.map { it.groupName }.toSet(),
                metadata = metaData
        )
    }

    @ApiOperation("Modify user information.")
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
            Hibernate.initialize(groupMemberships)
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

    @ApiOperation("Change user's password.")
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

    @ApiOperation("Delete user.")
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
