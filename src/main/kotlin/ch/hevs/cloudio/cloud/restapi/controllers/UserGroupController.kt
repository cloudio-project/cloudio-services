package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class UserGroupController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/createUserGroup", method = [RequestMethod.POST])
    fun createUserGroup(@RequestBody userGroup: UserGroup) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroup)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't create userGroup: " + e.message)
            }
        }
    }

    @RequestMapping("/getUserGroup", method = [RequestMethod.POST])
    fun getUserGroup(@RequestBody userGroupRequest: UserGroupRequest): UserGroup {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserGroup(userName, userGroupRequest)
    }

    @RequestMapping("/getUserGroup/{userGroupName}", method = [RequestMethod.GET])
    fun getUserGroup(@PathVariable userGroupName: String): UserGroup {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getUserGroup(userName, UserGroupRequest(userGroupName))
    }

    fun getUserGroup(userName:String, userGroupRequest: UserGroupRequest): UserGroup {
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, userGroupRequest)
            if (userGroup == null)
                throw CloudioHttpExceptions.BadRequest("UserGroup doesn't exist")
            else
                return userGroup
        }
    }

    @RequestMapping("/getUserGroupList", method = [RequestMethod.GET])
    fun getUserGroupList(): UserGroupList {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            return UserGroupUtil.getUserGroupList(userGroupRepository)
        }
    }

    @RequestMapping("/addUserToGroup", method = [RequestMethod.POST])
    fun addUserToGroup(@RequestBody userGroupRequestList: UserGroupUserRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, userGroupRequestList)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't add user to userGroup: " + e.message)
            }
        }
    }

    @RequestMapping("/deleteUserToGroup", method = [RequestMethod.DELETE])
    fun deleteUserToGroup(@RequestBody userGroupUserRequest: UserGroupUserRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, userGroupUserRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't delete user from userGroup: " + e.message)
            }
        }
    }

    @RequestMapping("/deleteUserGroup", method = [RequestMethod.DELETE])
    fun deleteUserGroup(@RequestBody userGroupRequest: UserGroupRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.Forbidden(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else {
            try {
                UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, userGroupRequest)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't delete userGroup: " + e.message)
            }
        }
    }

}