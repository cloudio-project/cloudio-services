package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserGroupUtil{

    @Throws(CloudioApiException::class)
    fun createUserGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroup : UserGroup){
        if(userGroupRepository.findByIdOrNull(userGroup.userGroupName) != null)
            throw CloudioApiException(userGroup.userGroupName+" already exist")

        //check that every user exist
        userGroup.usersList.forEach { userName ->
            if(userRepository.findByIdOrNull(userName) == null)
                throw CloudioApiException(userName+" user doesn't exist")
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

    @Throws(CloudioApiException::class)
    fun addUserToGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRequestList: UserGroupUserRequestList){
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
        }
        else
            throw CloudioApiException(userGroupRequestList.userGroupName+" userGroup doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun deleteUserToGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupUserRequest: UserGroupUserRequest){
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
            }
            else{
                throw CloudioApiException(userGroupUserRequest.user+" user not in userGroup")
            }
        }
        else
            throw CloudioApiException(userGroupUserRequest.userGroupName+" userGroup doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun deleteUserGroup(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRequest: UserGroupRequest){
        val userGroupToDelete = userGroupRepository.findByIdOrNull(userGroupRequest.userGroupName)
        return if(userGroupToDelete != null) {

            userGroupToDelete.usersList.forEach{userName ->
                //remove the userGroup in the user
                val user = userRepository.findByIdOrNull(userName)
                if(user!=null){
                    val userUserGroup = user.userGroups.toMutableSet()
                    userUserGroup.remove(userGroupRequest.userGroupName)
                    user.userGroups = userUserGroup
                    userRepository.save(user)
                }
            }

            userGroupRepository.delete(userGroupToDelete)
        }
        else
            throw CloudioApiException(userGroupRequest.userGroupName+" userGroup doesn't exist")
    }
}
