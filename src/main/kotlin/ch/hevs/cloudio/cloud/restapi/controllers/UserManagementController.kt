package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserManagementController(var userRepository: UserRepository) {

    @RequestMapping("/createUser", method = [RequestMethod.POST])
    fun createUser(@RequestBody user: User) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.createUser(userRepository, user)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't create user: " + e.message)
            }
        }
    }

    @RequestMapping("/getUser", method = [RequestMethod.POST])
    fun getUser(@RequestBody userRequest: UserRequest): User {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val user = UserManagementUtil.getUser(userRepository, userRequest)
            if (user == null)
                throw CloudioHttpExceptions.BadRequestException("User doesn't exist")
            else
                return user
        }
    }

    @RequestMapping("/deleteUser", method = [RequestMethod.DELETE])
    fun deleteUser(@RequestBody userRequest: UserRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.deleteUser(userRepository, userRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't delete user: " + e.message)
            }
        }
    }

    @RequestMapping("/modifyUserPassword", method = [RequestMethod.POST])
    fun modifyUserPassword(@RequestBody userPasswordRequest: UserPasswordRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name

        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN) &&
                (userName != userPasswordRequest.userName))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_OWN_ACCOUNT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.modifyUserPassword(userRepository, userPasswordRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't modify user password: " + e.message)
            }
        }
    }

    @RequestMapping("/addUserAuthority", method = [RequestMethod.POST])
    fun addUserAuthority(@RequestBody addAuthorityRequest: AddAuthorityRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.addUserAuthority(userRepository, addAuthorityRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't add user authority: " + e.message)
            }
        }
    }

    @RequestMapping("/removeUserAuthority", method = [RequestMethod.DELETE])
    fun removeUserAuthority(@RequestBody removeAuthorityRequest: RemoveAuthorityRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserManagementUtil.removeUserAuthority(userRepository, removeAuthorityRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't remove user authority: " + e.message)
            }
        }
    }

    @RequestMapping("/getUserList", method = [RequestMethod.GET])
    fun getUserList(): UserListAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            return UserManagementUtil.getUserList(userRepository)
        }
    }
}