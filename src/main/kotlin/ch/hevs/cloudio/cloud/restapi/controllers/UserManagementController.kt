package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class UserManagementController(var userRepository: UserRepository) {

    @RequestMapping("/createUser", method = [RequestMethod.POST])
    @Secured("HTTP_ADMIN")
    fun createUser(@RequestBody user: User) {
        try {
            UserManagementUtil.createUser(userRepository, user)
        } catch (exception: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't create user: " + exception.message)
        }
    }

    @RequestMapping("/getUser", method = [RequestMethod.POST])
    fun getUser(@RequestBody userRequest: UserRequest): User {
        val userName = SecurityContextHolder.getContext().authentication.name
        return (getUser(userName, userRequest))
    }

    @RequestMapping("/getUser/{userNameRequest}", method = [RequestMethod.GET])
    fun getUser(@PathVariable userNameRequest: String): User {
        val userName = SecurityContextHolder.getContext().authentication.name
        return (getUser(userName, UserRequest(userNameRequest)))
    }

    fun getUser(userName: String, userRequest: UserRequest): User {
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val user = UserManagementUtil.getUser(userRepository, userRequest)
            if (user == null)
                throw CloudioHttpExceptions.BadRequest("User doesn't exist")
            else
                return user
        }
    }

    @RequestMapping("/deleteUser", method = [RequestMethod.DELETE])
    fun deleteUser(@RequestBody userRequest: UserRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.deleteUser(userRepository, userRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't delete user: " + e.message)
            }
        }
    }

    @RequestMapping("/modifyUserPassword", method = [RequestMethod.POST])
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
    fun addUserAuthority(@RequestBody addAuthorityRequest: AddAuthorityRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.addUserAuthority(userRepository, addAuthorityRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't add user authority: " + e.message)
            }
        }
    }

    @RequestMapping("/removeUserAuthority", method = [RequestMethod.DELETE])
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
    @Secured("HTTP_ADMIN")
    fun getUserList(): UserListAnswer = UserManagementUtil.getUserList(userRepository)
}