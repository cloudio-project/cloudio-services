package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
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
class UserGroupController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/api/v1/createUserGroup", method = [RequestMethod.POST])
    fun createUserGroup(@RequestBody userGroup : UserGroup){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val creatAction = UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroup)
            if(creatAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't create userGroup: "+creatAction.message)
        }
    }

    @RequestMapping("/api/v1/getUserGroup", method = [RequestMethod.GET])
    fun getUserGroup(@RequestBody  userGroupRequest: UserGroupRequest): UserGroup{
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, userGroupRequest)
            if(userGroup == null)
                throw CloudioBadRequestException("UserGroup doesn't exist")
            else
                return userGroup
        }
    }

    @RequestMapping("/api/v1/getUserGroupList", method = [RequestMethod.GET])
    fun getUserGroupList(): UserGroupList {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            return UserGroupUtil.getUserGroupList(userGroupRepository)
        }
    }

    @RequestMapping("/api/v1/addUserToGroup", method = [RequestMethod.POST])
    fun addUserToGroup(@RequestBody userGroupRequestList: UserGroupUserRequestList){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val addAction = UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, userGroupRequestList)
            if(addAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't add user to userGroup: "+addAction.message)
        }
    }

    @RequestMapping("/api/v1/deleteUserToGroup", method = [RequestMethod.DELETE])
    fun deleteUserToGroup(@RequestBody userGroupUserRequest: UserGroupUserRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val deleteAction = UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, userGroupUserRequest)
            if(deleteAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't delete userGroup: "+deleteAction.message)
        }
    }

    @RequestMapping("/api/v1/deleteUserGroup", method = [RequestMethod.DELETE])
    fun deleteUserGroup(@RequestBody userGroupRequest: UserGroupRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val deleteAction = UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, userGroupRequest)
            if(deleteAction.success)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't delete userGroup: "+deleteAction.message)
        }
    }

}