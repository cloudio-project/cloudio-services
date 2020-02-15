package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('HTTP_ADMIN')")
class UserManagementController(var userRepository: UserRepository) {

    @RequestMapping("/createUser", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createUser(@RequestBody user: User) {
        try {
            UserManagementUtil.createUser(userRepository, user)
        } catch (exception: CloudioApiException) {
            throw CloudioHttpExceptions.Conflict("Couldn't create user: " + exception.message)
        }
    }

    @RequestMapping("/getUser", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.OK)
    fun getUser(@RequestBody userRequest: UserRequest): User = userRepository.findById(userRequest.userName).orElseGet {
        throw CloudioHttpExceptions.NotFound("User not found")
    }

    @RequestMapping("/getUser/{userNameRequest}", method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getUser(@PathVariable userNameRequest: String): User = userRepository.findById(userNameRequest).orElseGet {
        throw CloudioHttpExceptions.NotFound("User not found")
    }

    @RequestMapping("/deleteUser", method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@RequestBody userRequest: UserRequest) {
        if (userRepository.existsById(userRequest.userName)) {
            userRepository.deleteById(userRequest.userName)
        } else {
            throw CloudioHttpExceptions.NotFound("User not found")
        }
    }

    // TODO: We should not pass a hash, the hash should be generated on the server.
    // TODO: Give the user the possibility to change his own password without needing ADMIN atuhority.
    @RequestMapping("/modifyUserPassword", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun modifyUserPassword(@RequestBody userPasswordRequest: UserPasswordRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name

        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN) &&
                (userName != userPasswordRequest.userName))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.modifyUserPassword(userRepository, userPasswordRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't modify user password: " + e.message)
            }
        }
    }

    @RequestMapping("/addUserAuthority", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addUserAuthority(@RequestBody addAuthorityRequest: AddAuthorityRequest) {
        try {
            UserManagementUtil.addUserAuthority(userRepository, addAuthorityRequest)
            throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't add user authority: " + e.message)
        }
    }

    @RequestMapping("/removeUserAuthority", method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeUserAuthority(@RequestBody removeAuthorityRequest: RemoveAuthorityRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.removeUserAuthority(userRepository, removeAuthorityRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't remove user authority: " + e.message)
            }
        }
    }

    @RequestMapping("/getUserList", method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getUserList(): UserListAnswer = UserManagementUtil.getUserList(userRepository)
}