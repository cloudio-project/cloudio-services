package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserGroupAccessControlController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/getUserGroupAccessRight", method = [RequestMethod.GET])
    fun getUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRequest): Map<String, PrioritizedPermission> {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val userRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
            if (userRight == null)
                throw CloudioHttpExceptions.BadRequestException("Coudln't return userGroup Right")
            else
                return userRight

        }
    }

    @RequestMapping("/addUserGroupAccessRight", method = [RequestMethod.POST])
    fun addUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository, userGroupRightRequestList)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't add user group access right: " + e.message)
            }

        }
    }

    @RequestMapping("/modifyUserGroupAccessRight", method = [RequestMethod.POST])
    fun modifyUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRightRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't modify user group access right: " + e.message)
            }
        }
    }

    @RequestMapping("/removeUserGroupAccessRight", method = [RequestMethod.DELETE])
    fun removeUserGroupAccessRight(@RequestBody userGroupTopicRequest: UserGroupTopicRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, userGroupTopicRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couldn't delete user group access right: " + e.message)
            }
        }
    }

    @RequestMapping("/giveUserGroupAccessRight", method = [RequestMethod.POST])
    fun giveUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name

        try {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository, userGroupRightRequestList, userName)
            throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequestException("Couldn't add user group access right: " + e.message)
        }
    }
}