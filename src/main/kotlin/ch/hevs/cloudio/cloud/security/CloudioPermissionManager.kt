package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.UserGroupEndpointPermissionRepository
import ch.hevs.cloudio.cloud.dao.UserEndpointPermissionRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*

@Component
class CloudioPermissionManager(
        private val userRepository: MONGOUserRepository,
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupRepository: MONGOUserGroupRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository
): PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, subject: Any?, permission: Any?) = when {
        subject is UUID && permission is EndpointPermission -> hasEndpointPermission(authentication.name, subject, permission)
        subject is String && permission is EndpointModelElementPermission -> hasEndpointModelElementPermission(authentication.name, subject, permission)
        else -> false
    }

    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?) = when {
        targetType == "Endpoint" && targetId is UUID && permission is EndpointPermission -> hasEndpointPermission(authentication.name, targetId, permission)
        targetType == "Model" && targetId is String && permission is EndpointModelElementPermission -> hasEndpointModelElementPermission(authentication.name, targetId, permission)
        else -> false
    }

    private fun hasEndpointPermission(userName: String, endpointUUID: UUID, permission: EndpointPermission): Boolean {
        TODO()
    }

    private fun hasEndpointModelElementPermission(username: String, modelPath: String, permission: EndpointModelElementPermission): Boolean {
        TODO()
    }
}
