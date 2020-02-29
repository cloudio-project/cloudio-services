package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
@Authority.HttpAdmin
class UserManagementController(
        private var userRepository: UserRepository,
        private var groupRepository: UserGroupRepository,
        private var passwordEncoder: PasswordEncoder
) {
    @PostMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun postUserByUserName(@PathVariable userName: String, @RequestBody body: PostUserBody) {
        if (userRepository.existsById(userName)) {
            throw CloudioHttpExceptions.Conflict("Could not create user '$userName' - user exists.")
        }
        userRepository.save(body.toUser(userName, passwordEncoder))
    }

    @GetMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.OK)
    fun getUserByUserName(@PathVariable userName: String) = UserBody(userRepository.findById(userName).orElseThrow {
        CloudioHttpExceptions.NotFound("User '$userName' not found.")
    })

    @PutMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putUserByUserName(@PathVariable userName: String, @RequestBody body: UserBody) {
        if (userName != body.name) {
            throw CloudioHttpExceptions.Conflict("User name in URL and body do not match")
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

    @DeleteMapping("/users/{userName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable userName: String) {
        if (!userRepository.existsById(userName)) {
            throw CloudioHttpExceptions.NotFound("User '$userName' not found.")
        }
        userRepository.deleteById(userName)
    }

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

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    fun getAllUsers() = userRepository.findAll().map { it.userName }
}
