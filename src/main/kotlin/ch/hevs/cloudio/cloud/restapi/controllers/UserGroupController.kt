package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@Authority.HttpAdmin
class UserGroupController(var userRepository: UserRepository, var userGroupRepository: UserGroupRepository) {

    @RequestMapping("/createUserGroup", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createUserGroup(@RequestBody userGroup: UserGroup) {
        try {
            UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroup)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't create userGroup: " + e.message)
        }
    }

    @RequestMapping("/getUserGroup", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.OK)
    fun getUserGroup(@RequestBody userGroupRequest: UserGroupRequest) = getUserGroup(userGroupRequest.userGroupName)

    @RequestMapping("/getUserGroup/{userGroupName}", method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getUserGroup(@PathVariable userGroupName: String): UserGroup = userGroupRepository.findById(userGroupName).orElseGet {
        throw CloudioHttpExceptions.NotFound("User group not found")
    }

    @RequestMapping("/getUserGroupList", method = [RequestMethod.GET])
    @ResponseStatus(HttpStatus.OK)
    fun getUserGroupList() = UserGroupList(userGroupRepository.findAll().map(UserGroup::userGroupName).toSet())

    @RequestMapping("/addUserToGroup", method = [RequestMethod.POST])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addUserToGroup(@RequestBody userGroupRequestList: UserGroupUserRequestList) {
        try {
            UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, userGroupRequestList)
        } catch (exception: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't add user to userGroup: " + exception.message)
        }
    }

    @RequestMapping("/deleteUserToGroup", method = [RequestMethod.DELETE])  // TODO: Should it not be FromGroup...
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserToGroup(@RequestBody userGroupUserRequest: UserGroupUserRequest) {
        try {
            UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, userGroupUserRequest)
            throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't delete user from userGroup: " + e.message)
        }
    }

    @RequestMapping("/deleteUserGroup", method = [RequestMethod.DELETE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserGroup(@RequestBody userGroupRequest: UserGroupRequest) {
        try {
            UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, userGroupRequest)
            throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't delete userGroup: " + e.message)
        }
    }
}