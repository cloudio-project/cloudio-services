package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
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
class UserAccessControlController(var userRepository: UserRepository) {

    @RequestMapping("/api/v1/getUserAccessRight", method = [RequestMethod.GET])
    fun getUserAccessRight(@RequestBody userRightRequest: UserRequest): Map<String, PrioritizedPermission>{
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val userRight =  UserAccessControlUtil.getUserAccessRight(userRepository, userRightRequest)
            if(userRight == null)
                throw CloudioBadRequestException("Coudln't return userRight")
            else
                return userRight

        }
    }

    @RequestMapping("/api/v1/addUserAccessRight", method = [RequestMethod.POST])
    fun addUserAccessRight(@RequestBody userRightRequestList: UserRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val createAction = UserAccessControlUtil.addUserAccessRight(userRepository, userRightRequestList)
            if(createAction)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't add user access right")

        }
    }

    @RequestMapping("/api/v1/modifyUserAccessRight", method = [RequestMethod.POST])
    fun modifyUserAccessRight(@RequestBody userRightRequest: UserRightRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val modifyAction = UserAccessControlUtil.modifyUserAccessRight(userRepository, userRightRequest)
            if(modifyAction)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't modify user access right")
        }
    }

    @RequestMapping("/api/v1/removeUserAccessRight", method = [RequestMethod.DELETE])
    fun removeUserAccessRight(@RequestBody userTopicRequest: UserTopicRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val removeAction = UserAccessControlUtil.removeUserAccessRight(userRepository, userTopicRequest)
            if(removeAction)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't delete user access right")
        }
    }

    @RequestMapping("/api/v1/giveUserAccessRight", method = [RequestMethod.POST])
    fun giveUserAccessRight(@RequestBody userRightRequestList: UserRightRequestList) {
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else {
            val giveRightAction = UserAccessControlUtil.giveUserAccessRight(userRepository, userRightRequestList, userName)
            if(giveRightAction)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't add user access right")
        }
    }
}