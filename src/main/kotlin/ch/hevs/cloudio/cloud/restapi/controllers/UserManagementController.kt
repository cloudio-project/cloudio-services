package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.UserManagementUtil
import ch.hevs.cloudio.cloud.apiutils.UserRequest
import ch.hevs.cloudio.cloud.model.Authority
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
class UserManagementController(var userRepository: UserRepository) {

    @RequestMapping("/api/v1/createUser", method = [RequestMethod.POST])
    fun createUser(@RequestBody user : User){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            UserManagementUtil.createUser(userRepository,user)
            throw CloudioOkException("Success")
        }
    }

    @RequestMapping("/api/v1/getUser", method = [RequestMethod.GET])
    fun getUser(@RequestBody userRequest : UserRequest): User{
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val user = UserManagementUtil.getUser(userRepository, userRequest)
            if(user == null)
                throw CloudioBadRequestException("Coudln't return user")
            else
                return user
        }
    }

    @RequestMapping("/api/v1/deleteUser", method = [RequestMethod.DELETE])
    fun deleteUser(@RequestBody userRequest : UserRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        if (!userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN))
            throw CloudioForbiddenException("You don't have http admin right to access this function")
        else{
            val deleteAction = UserManagementUtil.deleteUser(userRepository,userRequest)
            if(deleteAction)
                throw CloudioOkException("Success")
            else
                throw CloudioBadRequestException("Couldn't delete user")
        }
    }
}