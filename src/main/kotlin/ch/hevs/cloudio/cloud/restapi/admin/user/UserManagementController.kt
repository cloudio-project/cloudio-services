package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@Api(tags = ["User Management"], description = "Allows an admin user to manage users.")
@RestController
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserManagementController(
        private var userRepository: UserRepository,
        private var groupRepository: UserGroupRepository,
        private var passwordEncoder: PasswordEncoder
) {
    @ApiOperation("Create a new user.")
    @PostMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun postUserByUserName(@PathVariable userName: String, @RequestBody body: PostUserBody) {
        if (userRepository.existsById(userName)) {
            throw CloudioHttpExceptions.Conflict("Could not create user '$userName' - User exists.")
        }
        body.groupMemberships.forEach {
            if (!groupRepository.existsById(it)) {
                throw CloudioHttpExceptions.NotFound("Could not create user '$userName' - Group '$it' does not exist.")
            }
        }
        userRepository.save(body.toUser(userName, passwordEncoder))
    }

    @ApiOperation("Get user information.")
    @GetMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.OK)
    fun getUserByUserName(@PathVariable userName: String) = UserBody(userRepository.findById(userName).orElseThrow {
        CloudioHttpExceptions.NotFound("User '$userName' not found.")
    })

    @ApiOperation("Modify user information.")
    @PutMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putUserByUserName(@PathVariable userName: String, @RequestBody body: UserBody) {
        if (userName != body.name) {
            throw CloudioHttpExceptions.Conflict("User name in URL and body do not match.")
        }
        userRepository.findById(userName).orElseThrow {
            CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }.also {
            body.groupMemberships.forEach {
                if (!groupRepository.existsById(it)) {
                    throw CloudioHttpExceptions.NotFound("Group '$it' not found.")
                }
            }
            body.updateUser(it)
            userRepository.save(it)
        }
    }

    @ApiOperation("Delete user.")
    @DeleteMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable userName: String) {
        if (!userRepository.existsById(userName)) {
            throw CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }
        userRepository.deleteById(userName)
    }

    @ApiOperation("Change user's password.")
    @PutMapping("/users/{userName}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putUserPassword(@PathVariable userName: String, @RequestParam password: String) {
        userRepository.findById(userName).orElseThrow {
            CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }.let {
            it.passwordHash = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }

    @ApiOperation("List all user names.")
    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    fun getAllUsers() = userRepository.findAll().map { it.userName }
}
