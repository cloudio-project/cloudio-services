package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserAccessControlUtil {

    fun getUserAccessRight(userRepository: UserRepository, userRequest: UserRequest): Map<String, PrioritizedPermission>? {
        return userRepository.findByIdOrNull(userRequest.userName)?.permissions
    }

    @Throws(CloudioApiException::class)
    fun addUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList) {
        val user = userRepository.findByIdOrNull(userRightRequestList.userName)

        if (user != null) {
            userRightRequestList.userRights.forEach { userRight ->
                user.permissions[userRight.topic] = PrioritizedPermission(userRight.permission, userRight.priority)
            }

            userRepository.save(user)
        } else
            throw CloudioApiException("User " + userRightRequestList.userName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun modifyUserAccessRight(userRepository: UserRepository, userRightRequest: UserRightRequest) {
        val user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            if (user.permissions[userRightRequest.userRight.topic] != null) {

                user.permissions[userRightRequest.userRight.topic] = PrioritizedPermission(userRightRequest.userRight.permission, userRightRequest.userRight.priority)
                userRepository.save(user)
            } else {
                throw CloudioApiException(userRightRequest.userRight.topic + " permission doesn't exist in " + userRightRequest.userName)
            }
        } else
            throw CloudioApiException("User " + userRightRequest.userName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun removeUserAccessRight(userRepository: UserRepository, userRightRequest: UserTopicRequest) {
        val user = userRepository.findByIdOrNull(userRightRequest.userName)

        if (user != null) {
            if (user.permissions[userRightRequest.topic] != null) {
                user.permissions.remove(userRightRequest.topic)
                userRepository.save(user)
            } else {
                throw CloudioApiException(userRightRequest.topic + " permission doesn't exist in " + userRightRequest.userName)
            }
        } else
            throw CloudioApiException("User " + userRightRequest.userName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun giveUserAccessRight(userRepository: UserRepository, userRightRequestList: UserRightRequestList, requestUserName: String) {

        val requestUser = userRepository.findById(requestUserName).get()

        userRightRequestList.userRights.forEach { userRight ->
            val keyToCheck = userRight.topic.split("/")[0] + "/#"
            if (requestUser.permissions[keyToCheck] == null || requestUser.permissions[keyToCheck]?.permission != Permission.OWN)
                throw CloudioApiException("You don't have OWN permission on " + userRight.topic)
        }
        addUserAccessRight(userRepository, userRightRequestList)
    }
}