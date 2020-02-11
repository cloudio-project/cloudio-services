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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class UserGroupAccessControlController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/getUserGroupAccessRight", method = [RequestMethod.POST])
    fun getUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRequest): Map<String, PrioritizedPermission> {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserGroupAccessRight(userName, userGroupRightRequest)
    }

    @RequestMapping("/getUserGroupAccessRight/{userGroupName}", method = [RequestMethod.GET])
    fun getUserGroupAccessRight(@PathVariable userGroupName: String): Map<String, PrioritizedPermission> {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserGroupAccessRight(userName, UserGroupRequest(userGroupName))
    }

    fun getUserGroupAccessRight(userName: String, userGroupRightRequest: UserGroupRequest): Map<String, PrioritizedPermission> {
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val userRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
            if (userRight == null)
                throw CloudioHttpExceptions.BadRequest("Coudln't return userGroup Right")
            else
                return userRight
        }
    }

    @RequestMapping("/addUserGroupAccessRight", method = [RequestMethod.POST])
    fun addUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository, userGroupRightRequestList)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't add user group access right: " + e.message)
            }

        }
    }

    @RequestMapping("/modifyUserGroupAccessRight", method = [RequestMethod.POST])
    fun modifyUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRightRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't modify user group access right: " + e.message)
            }
        }
    }

    @RequestMapping("/removeUserGroupAccessRight", method = [RequestMethod.DELETE])
    fun removeUserGroupAccessRight(@RequestBody userGroupTopicRequest: UserGroupTopicRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, userGroupTopicRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't delete user group access right: " + e.message)
            }
        }
    }

    @RequestMapping("/giveUserGroupAccessRight", method = [RequestMethod.POST])
    fun giveUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name

        try {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository, userGroupRightRequestList, userName)
            throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't add user group access right: " + e.message)
        }
    }
}