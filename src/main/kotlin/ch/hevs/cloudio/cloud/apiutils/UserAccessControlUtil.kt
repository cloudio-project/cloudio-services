package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserAccessControlUtil{

    fun getUserAccessRight(userRepository: UserRepository, userRequest: UserRequest):Map<String, PrioritizedPermission>? {
        return userRepository.findByIdOrNull(userRequest.userName)?.permissions
    }

    fun addUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList): ApiActionAnswer {
        val user = userRepository.findByIdOrNull(userRightRequestList.userName)

        if(user!=null){
            val permissions = user.permissions.toMutableMap()

            userRightRequestList.userRights.forEach { userRight ->
                permissions[userRight.topic] = PrioritizedPermission(userRight.permission, userRight.priority)
            }

            user.permissions = permissions.toMap()

            userRepository.save(user)
            return ApiActionAnswer(true,"")
        }
        return ApiActionAnswer(false,"User "+userRightRequestList.userName+"doesn't exist")
    }

    fun modifyUserAccessRight(userRepository: UserRepository, userRightRequest: UserRightRequest): ApiActionAnswer {
        val user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            val permissions = user.permissions.toMutableMap()

            if (permissions[userRightRequest.userRight.topic] != null){

                permissions[userRightRequest.userRight.topic] = PrioritizedPermission(userRightRequest.userRight.permission, userRightRequest.userRight.priority)
                user.permissions = permissions.toMap()
                userRepository.save(user)
                return ApiActionAnswer(true,"")
            }
            else{
                return ApiActionAnswer(false,userRightRequest.userRight.topic+" permission doesn't exist in "+ userRightRequest.userName)
            }
        }
        return ApiActionAnswer(false,"User "+userRightRequest.userName+"doesn't exist")
    }

    fun removeUserAccessRight(userRepository: UserRepository, userRightRequest: UserTopicRequest): ApiActionAnswer {
        val user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            val permissions = user.permissions.toMutableMap()

            if (permissions[userRightRequest.topic] != null){
                permissions.remove(userRightRequest.topic)
                user.permissions = permissions.toMap()
                userRepository.save(user)
                return ApiActionAnswer(true,"")
            }
            else{
                return ApiActionAnswer(false, userRightRequest.topic+" permission doesn't exist in "+ userRightRequest.userName)
            }
        }
        return ApiActionAnswer(false,"User "+userRightRequest.userName+"doesn't exist")
    }

    fun giveUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList, requestUserName: String): ApiActionAnswer{

        val requestUser = userRepository.findById(requestUserName).get()

        userRightRequestList.userRights.forEach { userRight ->
            val keyToCheck = userRight.topic.split("/")[0]+"/#"
            if(requestUser.permissions[keyToCheck] == null || requestUser.permissions[keyToCheck]?.permission != Permission.OWN)
                return ApiActionAnswer(false,"You don't have OWN permission on "+userRight.topic)
        }
        return addUserAccessRight(userRepository, userRightRequestList)
    }
}