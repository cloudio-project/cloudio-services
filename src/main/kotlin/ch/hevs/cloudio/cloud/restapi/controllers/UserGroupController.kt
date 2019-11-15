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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserGroupController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/createUserGroup", method = [RequestMethod.POST])
    fun createUserGroup(@RequestBody userGroup : UserGroup){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            val creatAction = UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroup)
            if(creatAction.success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't create userGroup: "+creatAction.message)
        }
    }

    @RequestMapping("/getUserGroup", method = [RequestMethod.GET])
    fun getUserGroup(@RequestBody  userGroupRequest: UserGroupRequest): UserGroup{
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, userGroupRequest)
            if(userGroup == null)
                throw CloudioHttpExceptions.BadRequestException("UserGroup doesn't exist")
            else
                return userGroup
        }
    }

    @RequestMapping("/getUserGroupList", method = [RequestMethod.GET])
    fun getUserGroupList(): UserGroupList {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            return UserGroupUtil.getUserGroupList(userGroupRepository)
        }
    }

    @RequestMapping("/addUserToGroup", method = [RequestMethod.POST])
    fun addUserToGroup(@RequestBody userGroupRequestList: UserGroupUserRequestList){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            val addAction = UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, userGroupRequestList)
            if(addAction.success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't add user to userGroup: "+addAction.message)
        }
    }

    @RequestMapping("/deleteUserToGroup", method = [RequestMethod.DELETE])
    fun deleteUserToGroup(@RequestBody userGroupUserRequest: UserGroupUserRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            val deleteAction = UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, userGroupUserRequest)
            if(deleteAction.success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't delete userGroup: "+deleteAction.message)
        }
    }

    @RequestMapping("/deleteUserGroup", method = [RequestMethod.DELETE])
    fun deleteUserGroup(@RequestBody userGroupRequest: UserGroupRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioHttpExceptions.ForbiddenException(CLOUDIO_AMIN_RIGHT_ERROR_MESSAGE)
        else{
            val deleteAction = UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, userGroupRequest)
            if(deleteAction.success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't delete userGroup: "+deleteAction.message)
        }
    }

}