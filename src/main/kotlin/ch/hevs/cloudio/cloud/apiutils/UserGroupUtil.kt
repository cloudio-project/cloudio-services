package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserGroupUtil{

    fun createUserGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroup : UserGroup): ApiActionAnswer{
        if(userGroupRepository.findByIdOrNull(userGroup.userGroupName) != null)
            return ApiActionAnswer(false, userGroup.userGroupName+" already exist")

        //check that every user exist
        userGroup.usersList.forEach { userName ->
            if(userRepository.findByIdOrNull(userName) == null)
                return ApiActionAnswer(false, userName+" user doesn't exist")
        }
        //save the userGroup
        userGroupRepository.save(userGroup)

        //add the userGroup to every User
        userGroup.usersList.forEach { userName ->
            val user = userRepository.findById(userName).get()
            val userUserGroup = user.userGroups.toMutableSet()
            userUserGroup.add(userGroup.userGroupName)
            user.userGroups = userUserGroup
            userRepository.save(user)
        }
        return ApiActionAnswer(true,"")
    }

    fun getUserGroup(userGroupRepository: UserGroupRepository, userGroupRequest: UserGroupRequest): UserGroup?{
        return userGroupRepository.findByIdOrNull(userGroupRequest.userGroupName)
    }

    fun getUserGroupList(userGroupRepository: UserGroupRepository): UserGroupList{
        val userGroupNames : MutableSet<String> = mutableSetOf()
        userGroupRepository.findAll().forEach { userGroup ->
            userGroupNames.add(userGroup.userGroupName)
        }
        return UserGroupList(userGroupNames.toSet())
    }

    fun addUserToGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRequestList: UserGroupUserRequestList): ApiActionAnswer{
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRequestList.userGroupName)

        if (userGroup != null) {
            val userList = userGroup.usersList.toMutableSet()

            userGroupRequestList.users.forEach{userName ->
                //add the user in the userGroup
                userList.add(userName)
                userGroup.usersList = userList.toSet()
                userGroupRepository.save(userGroup)

                //add the userGroup in the user
                val user = userRepository.findByIdOrNull(userName)
                if(user!=null){
                    val userUserGroup = user.userGroups.toMutableSet()
                    userUserGroup.add(userGroup.userGroupName)
                    user.userGroups = userUserGroup
                    userRepository.save(user)
                }
            }
            return ApiActionAnswer(true,"")
        }
        else
            return ApiActionAnswer(false,userGroupRequestList.userGroupName+" userGroup doesn't exist")
    }

    fun deleteUserToGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupUserRequest: UserGroupUserRequest): ApiActionAnswer{
        val userGroup = userGroupRepository.findByIdOrNull(userGroupUserRequest.userGroupName)

        if (userGroup != null) {
            val userList = userGroup.usersList.toMutableSet()

            if (userList.contains(userGroupUserRequest.user)){
                //remove the user in the userGroup
                userList.remove(userGroupUserRequest.user)
                userGroup.usersList = userList.toSet()
                userGroupRepository.save(userGroup)

                //remove the userGroup in the user
                val user = userRepository.findByIdOrNull(userGroupUserRequest.user)
                if(user!=null){
                    val userUserGroup = user.userGroups.toMutableSet()
                    userUserGroup.remove(userGroup.userGroupName)
                    user.userGroups = userUserGroup
                    userRepository.save(user)
                }

                return ApiActionAnswer(true,"")
            }
            else{
                return ApiActionAnswer(false, userGroupUserRequest.user+" user doesn't exist")
            }
        }
        else
            return ApiActionAnswer(false,userGroupUserRequest.userGroupName+" userGroup doesn't exist")
    }

    fun deleteUserGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRequest: UserGroupRequest): ApiActionAnswer{
        val userGroupToDelete = userGroupRepository.findByIdOrNull(userGroupRequest.userGroupName)
        return if(userGroupToDelete != null) {

            userGroupToDelete.usersList.forEach{userName ->
                //remove the userGroup in the user
                val user = userRepository.findByIdOrNull(userName)
                if(user!=null){
                    val userUserGroup = user.userGroups.toMutableSet()
                    userUserGroup.add(userGroupRequest.userGroupName)
                    user.userGroups = userUserGroup
                    userRepository.save(user)
                }
            }

            userGroupRepository.delete(userGroupToDelete)
            return ApiActionAnswer(true,"")
        }
        else
            return ApiActionAnswer(false,userGroupRequest.userGroupName+" userGroup doesn't exist")
    }
}
