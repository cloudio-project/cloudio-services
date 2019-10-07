package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserAccessControlUtil{

    fun getUserAccessRight(userRepository: UserRepository, userRequest: UserRequest):Map<String, PrioritizedPermission>? {
        return userRepository.findByIdOrNull(userRequest.userName)?.permissions
    }

    fun addUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList): Boolean {
        var user = userRepository.findByIdOrNull(userRightRequestList.userName)

        if(user!=null){
            var permissions = user.permissions.toMutableMap()

            userRightRequestList.userRights.forEach { userRight ->
                permissions[userRight.topic] = PrioritizedPermission(userRight.permission, userRight.priority)
            }

            user.permissions = permissions.toMap()

            userRepository.save(user)
            return true
        }
        return false
    }

    fun modifyUserAccessRight(userRepository: UserRepository, userRightRequest: UserRightRequest): Boolean {
        var user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            var permissions = user.permissions.toMutableMap()

            if (permissions[userRightRequest.userRight.topic] != null){

                permissions[userRightRequest.userRight.topic] = PrioritizedPermission(userRightRequest.userRight.permission, userRightRequest.userRight.priority)
                user.permissions = permissions.toMap()
                userRepository.save(user)
                return true
            }
            else{
                return false
            }
        }
        return false
    }

    fun removeUserAccessRight(userRepository: UserRepository, userRightRequest: UserTopicRequest): Boolean {
        var user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            var permissions = user.permissions.toMutableMap()

            if (permissions[userRightRequest.topic] != null){
                permissions.remove(userRightRequest.topic)
                user.permissions = permissions.toMap()
                userRepository.save(user)
                return true
            }
            else{
                return false
            }
        }
        return false
    }

    fun giveUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList, requestUserName: String): Boolean{

        val requestUser = userRepository.findById(requestUserName).get()

        userRightRequestList.userRights.forEach { userRight ->
            val keyToCheck = userRight.topic.split("/")[0]+"/#"
            if(requestUser.permissions[keyToCheck] == null || requestUser.permissions[keyToCheck]?.permission != Permission.OWN)
                return false
        }
        return addUserAccessRight(userRepository, userRightRequestList)
    }
}