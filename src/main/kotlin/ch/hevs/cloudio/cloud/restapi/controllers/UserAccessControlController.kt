package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class UserAccessControlController(var userRepository: UserRepository) {

    @RequestMapping("/getUserAccessRight", method = [RequestMethod.POST])
    fun getUserAccessRight(@RequestBody userRequest: UserRequest): Map<String, PrioritizedPermission> {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserAccessRight(userName, userRequest)
    }

    @RequestMapping("/getUserAccessRight/{userNameRequest}", method = [RequestMethod.GET])
    fun getUserAccessRight(@PathVariable userNameRequest: String): Map<String, PrioritizedPermission> {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserAccessRight(userName, UserRequest(userNameRequest))
    }

    fun getUserAccessRight(userName: String, userRequest: UserRequest): Map<String, PrioritizedPermission> {
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val userRight = UserAccessControlUtil.getUserAccessRight(userRepository, userRequest)
            if (userRight == null)
                throw CloudioHttpExceptions.BadRequestException("Coudln't return userRight")
            else
                return userRight
        }
    }

    @RequestMapping("/addUserAccessRight", method = [RequestMethod.POST])
    fun addUserAccessRight(@RequestBody userRightRequestList: UserRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserAccessControlUtil.addUserAccessRight(userRepository, userRightRequestList)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't add user access right: " + e.message)
            }
        }
    }

    @RequestMapping("/modifyUserAccessRight", method = [RequestMethod.POST])
    fun modifyUserAccessRight(@RequestBody userRightRequest: UserRightRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserAccessControlUtil.modifyUserAccessRight(userRepository, userRightRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't modify user access right: " + e.message)
            }
        }
    }

    @RequestMapping("/removeUserAccessRight", method = [RequestMethod.DELETE])
    fun removeUserAccessRight(@RequestBody userTopicRequest: UserTopicRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserAccessControlUtil.removeUserAccessRight(userRepository, userTopicRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't delete user access right: " + e.message)
            }
        }
    }

    @RequestMapping("/giveUserAccessRight", method = [RequestMethod.POST])
    fun giveUserAccessRight(@RequestBody userRightRequestList: UserRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name

        try {
            UserAccessControlUtil.giveUserAccessRight(userRepository, userRightRequestList, userName)
            throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequestException("Couldn't add user access right: " + e.message)
        }
    }
}