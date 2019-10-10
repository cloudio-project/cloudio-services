package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.restapi.CloudioForbiddenException
import ch.hevs.cloudio.cloud.restapi.CloudioOkException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class UserGroupAccessControlController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/api/v1/getUserGroupAccessRight", method = [RequestMethod.GET])
    fun getUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRequest): Map<String, PrioritizedPermission>{
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val userRight =  UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
            if(userRight == null)
                throw CloudioBadRequestException("Coudln't return userGroup Right")
            else
                return userRight

        }
    }

    @RequestMapping("/api/v1/addUserGroupAccessRight", method = [RequestMethod.POST])
    fun addUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val createAction = UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository, userGroupRightRequestList)
            if(createAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't add user group access right: "+createAction.message)

        }
    }

    @RequestMapping("/api/v1/modifyUserGroupAccessRight", method = [RequestMethod.POST])
    fun modifyUserGroupAccessRight(@RequestBody userGroupRightRequest: UserGroupRightRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val modifyAction = UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository, userGroupRightRequest)
            if(modifyAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't modify user group access right: "+modifyAction.message)
        }
    }

    @RequestMapping("/api/v1/removeUserGroupAccessRight", method = [RequestMethod.DELETE])
    fun removeUserGroupAccessRight(@RequestBody userGroupTopicRequest: UserGroupTopicRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val removeAction = UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, userGroupTopicRequest)
            if(removeAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't delete user group access right: "+removeAction.message)
        }
    }

    @RequestMapping("/api/v1/giveUserGroupAccessRight", method = [RequestMethod.POST])
    fun giveUserGroupAccessRight(@RequestBody userGroupRightRequestList: UserGroupRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name

        val giveRightAction = UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository, userGroupRightRequestList, userName)
        if(giveRightAction.success)
            throw CloudioOkException("Success")
        else
            throw CloudioBadRequestException("Couldn't add user group access right: "+giveRightAction.message)
    }
}