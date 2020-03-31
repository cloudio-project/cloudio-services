package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserGroupAccessControlUtil {

    fun getUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRequest: UserGroupRequest): Map<String, PrioritizedPermission>? {
        return userGroupRepository.findByIdOrNull(userGroupRequest.userGroupName)?.permissions
    }

    @Throws(CloudioApiException::class)
    fun addUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequestList: UserGroupRightRequestList) {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequestList.userGroupName)

        if (userGroup != null) {
            val permissions = userGroup.permissions

            userGroupRightRequestList.userGroupRights.forEach { userGroupRight ->
                permissions[userGroupRight.topic] = PrioritizedPermission(userGroupRight.permission, userGroupRight.priority)
            }

            userGroup.permissions = permissions

            userGroupRepository.save(userGroup)
        } else
            throw CloudioApiException("UserGroup " + userGroupRightRequestList.userGroupName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun modifyUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequest: UserGroupRightRequest) {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequest.userGroupName)

        if (userGroup != null) {
            val permissions = userGroup.permissions

            if (permissions[userGroupRightRequest.userGroupRight.topic] != null) {

                permissions[userGroupRightRequest.userGroupRight.topic] = PrioritizedPermission(userGroupRightRequest.userGroupRight.permission, userGroupRightRequest.userGroupRight.priority)
                userGroup.permissions = permissions
                userGroupRepository.save(userGroup)
            } else {
                throw CloudioApiException(userGroupRightRequest.userGroupRight.topic + " permission doesn't exist in " + userGroupRightRequest.userGroupName)
            }
        } else
            throw CloudioApiException("UserGroup " + userGroupRightRequest.userGroupName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun removeUserGroupAccessRight(userGroupRepository: UserGroupRepository, userGroupRightRequest: UserGroupTopicRequest) {
        val userGroup = userGroupRepository.findByIdOrNull(userGroupRightRequest.userGroupName)

        if (userGroup != null) {
            val permissions = userGroup.permissions

            if (permissions[userGroupRightRequest.topic] != null) {
                permissions.remove(userGroupRightRequest.topic)
                userGroup.permissions = permissions
                userGroupRepository.save(userGroup)
            } else {
                throw CloudioApiException(userGroupRightRequest.topic + " permission doesn't exist in " + userGroupRightRequest.userGroupName)
            }
        } else
            throw CloudioApiException("User " + userGroupRightRequest.userGroupName + " doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun giveUserGroupAccessRight(userGroupRepository: UserGroupRepository, userRepository: UserRepository, userGroupRightRequestList: UserGroupRightRequestList, requestUserName: String) {

        val requestUser = userRepository.findById(requestUserName).get()

        userGroupRightRequestList.userGroupRights.forEach { userGroupRight ->
            val keyToCheck = userGroupRight.topic.split("/")[0] + "/#"
            if (requestUser.permissions[keyToCheck] == null || requestUser.permissions[keyToCheck]?.permission != Permission.OWN)
                throw CloudioApiException("You don't have OWN permission on " + userGroupRight.topic)
        }
        addUserGroupAccessRight(userGroupRepository, userGroupRightRequestList)
    }
}