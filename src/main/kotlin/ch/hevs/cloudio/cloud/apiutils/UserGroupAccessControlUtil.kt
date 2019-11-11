package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserGroupAccessControlUtil{

    fun getUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRequest: UserGroupRequest):Map<String, PrioritizedPermission>? {
        return userGroupRepository.findByIdOrNull(userGroupRequest.userGroupName)?.permissions
    }

    fun addUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequestList: UserGroupRightRequestList): ApiActionAnswer {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequestList.userGroupName)

        if(userGroup!=null){
            val permissions = userGroup.permissions.toMutableMap()

            userGroupRightRequestList.userGroupRights.forEach { userGroupRight ->
                permissions[userGroupRight.topic] = PrioritizedPermission(userGroupRight.permission, userGroupRight.priority)
            }

            userGroup.permissions = permissions.toMap()

            userGroupRepository.save(userGroup)
            return ApiActionAnswer(true,"")
        }
        return ApiActionAnswer(false,"UserGroup "+userGroupRightRequestList.userGroupName+"doesn't exist")
    }

    fun modifyUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequest: UserGroupRightRequest): ApiActionAnswer {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequest.userGroupName)

        if (userGroup != null) {
            val permissions = userGroup.permissions.toMutableMap()

            if (permissions[userGroupRightRequest.userGroupRight.topic] != null){

                permissions[userGroupRightRequest.userGroupRight.topic] = PrioritizedPermission(userGroupRightRequest.userGroupRight.permission, userGroupRightRequest.userGroupRight.priority)
                userGroup.permissions = permissions.toMap()
                userGroupRepository.save(userGroup)
                return ApiActionAnswer(true,"")
            }
            else{
                return ApiActionAnswer(false,userGroupRightRequest.userGroupRight.topic+" permission doesn't exist in "+ userGroupRightRequest.userGroupName)
            }
        }
        return ApiActionAnswer(false,"UserGroup "+userGroupRightRequest.userGroupName+"doesn't exist")
    }

    fun removeUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequest: UserGroupTopicRequest): ApiActionAnswer {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequest.userGroupName)

        if (userGroup != null) {
            val permissions = userGroup.permissions.toMutableMap()

            if (permissions[userGroupRightRequest.topic] != null){
                permissions.remove(userGroupRightRequest.topic)
                userGroup.permissions = permissions.toMap()
                userGroupRepository.save(userGroup)
                return ApiActionAnswer(true,"")
            }
            else{
                return ApiActionAnswer(false, userGroupRightRequest.topic+" permission doesn't exist in "+ userGroupRightRequest.userGroupName)
            }
        }
        return ApiActionAnswer(false,"User "+userGroupRightRequest.userGroupName+"doesn't exist")
    }

    fun giveUserGroupAccessRight(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRightRequestList: UserGroupRightRequestList, requestUserName: String): ApiActionAnswer{

        val requestUser = userRepository.findById(requestUserName).get()

        userGroupRightRequestList.userGroupRights.forEach { userGroupRight ->
            val keyToCheck = userGroupRight.topic.split("/")[0]+"/#"
            if(requestUser.permissions[keyToCheck] == null || requestUser.permissions[keyToCheck]?.permission != Permission.OWN)
                return ApiActionAnswer(false,"You don't have OWN permission on "+userGroupRight.topic)
        }
        return addUserGroupAccessRight(userGroupRepository, userGroupRightRequestList)
    }
}