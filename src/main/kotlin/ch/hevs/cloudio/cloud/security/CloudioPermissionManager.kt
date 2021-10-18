package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.UserEndpointPermissionRepository
import ch.hevs.cloudio.cloud.dao.UserGroupEndpointPermissionRepository
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.apache.juli.logging.LogFactory
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.io.Serializable
import java.util.*

@Service
class CloudioPermissionManager(
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository
) : PermissionEvaluator {
    private val log = LogFactory.getLog(CloudioPermissionManager::class.java)

    override fun hasPermission(authentication: Authentication, subject: Any?, permission: Any?) = when {
        subject is UUID && permission is EndpointPermission && authentication.principal is CloudioUserDetails -> hasEndpointPermission(authentication.principal as CloudioUserDetails, subject, permission)
        subject is String && permission is EndpointPermission && authentication.principal is CloudioUserDetails -> try {
            hasEndpointPermission(authentication.principal as CloudioUserDetails, UUID.fromString(subject), permission)
        } catch (e: Exception) {
            log.error("Invalid endpoint UUID: Authentication = $authentication, subject = $subject, permission = $permission")
            false
        }
        subject is ModelIdentifier && permission is EndpointModelElementPermission && authentication.principal is CloudioUserDetails -> hasEndpointModelElementPermission(authentication.principal as CloudioUserDetails, subject, permission)
        subject is String && permission is EndpointModelElementPermission && authentication.principal is CloudioUserDetails -> ModelIdentifier(subject).let {
            if (it.valid) hasEndpointModelElementPermission(authentication.principal as CloudioUserDetails, it, permission) else {
                log.error("Invalid model identifier: Authentication = $authentication, subject = $subject, permission = $permission")
                false
            }
        }
        else -> {
            log.error("Unknown permission request: Authentication = $authentication, subject = $subject, permission = $permission")
            false
        }
    }

    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?) = when {
        targetType == "Endpoint" && targetId is UUID && permission is EndpointPermission && authentication.principal is CloudioUserDetails -> hasEndpointPermission(authentication.principal as CloudioUserDetails, targetId, permission)
        targetType == "Model" && targetId is ModelIdentifier && permission is EndpointModelElementPermission && authentication.principal is CloudioUserDetails -> hasEndpointModelElementPermission(authentication.principal as CloudioUserDetails, targetId, permission)
        else -> {
            log.error("Unknown permission request: Authentication = $authentication, targetId = $targetId, permission = $permission")
            false
        }
    }

    fun hasEndpointPermission(userDetails: CloudioUserDetails, endpointUUID: UUID, permission: EndpointPermission) = if (resolveEndpointPermission(userDetails, endpointUUID).fulfills(permission)) {
        log.debug("Permission $permission on endpoint $endpointUUID granted for user ${userDetails.username}")
        true
    } else {
        log.warn("Permission $permission on endpoint $endpointUUID rejected for user ${userDetails.username}")
        false
    }

    fun hasEndpointModelElementPermission(userDetails: CloudioUserDetails, modelID: ModelIdentifier, permission: EndpointModelElementPermission): Boolean {
        // Ensure that the model ID is correct.
        if (!modelID.valid) {
            log.warn("Permission $permission to \"$modelID \"rejected for user \"${userDetails.username}\": Invalid model identifier.")
            return false
        }

        // Get the global permission of the user for the endpoint.
        val endpointPermission = resolveEndpointPermission(userDetails, modelID.endpoint)

        // If the user has no access to the endpoint, reject any access.
        if (!endpointPermission.fulfills(EndpointPermission.ACCESS)) {
            log.debug("Permission $permission to \"$modelID\" rejected for user \"${userDetails.username}\": No access to endpoint.")
            return false
        }

        // Check if the user has the permission on the endpoint globally.
        when (permission) {
            EndpointModelElementPermission.DENY -> return true
            EndpointModelElementPermission.VIEW -> if (endpointPermission.fulfills(EndpointPermission.BROWSE)) {
                log.debug("Permission VIEW to \"$modelID\" granted for user \"${userDetails.username}\": BROWSE+ permission on endpoint.")
                return true
            }
            EndpointModelElementPermission.READ -> if (endpointPermission.fulfills(EndpointPermission.READ)) {
                log.debug("Permission READ to \"$modelID\" granted for user \"${userDetails.username}\" READ+ permission on endpoint.")
                return true
            }
            EndpointModelElementPermission.WRITE -> if (endpointPermission.fulfills(EndpointPermission.WRITE)) {
                log.debug("Permission WRITE to \"$modelID\" granted for user \"${userDetails.username}\" WRITE+ permission on endpoint.")
                return true
            }
        }

        // Check for element specific permission.
        return resolveEndpointModelElementPermission(userDetails, modelID).fulfills(permission)
    }

    fun resolveEndpointPermission(userDetails: CloudioUserDetails, endpointUUID: UUID): EndpointPermission {
        var permission = EndpointPermission.DENY

        userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userDetails.id, endpointUUID).ifPresent {
            permission = permission.higher(it.permission)
        }

        if (!permission.fulfills(EndpointPermission.GRANT)) {
            userDetails.groupMembershipIDs.forEach { groupID ->
                userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, endpointUUID).ifPresent {
                    permission = permission.higher(it.permission)
                }
            }
        }

        return permission
    }

    fun resolveEndpointModelElementPermission(userDetails: CloudioUserDetails, modelID: ModelIdentifier): EndpointModelElementPermission {
        var allPermissionsList = getAllEndpointModelElementPermissions(userDetails, modelID.endpoint)

        //add the permissions with the most precise modelPath to the permissionsList
        var temp = modelID.toString()

        //if a @ is the first letter, delete the first element
        if(temp.first().equals('@', true)){
            //drop the @something and the "/"
            temp = temp.dropWhile { !it.equals('/', ignoreCase = true) }
            temp = temp.drop(1)
        }

        //drop the uuid and the "/"
        temp = temp.dropWhile { !it.equals('/', ignoreCase = true) }
        temp = temp.drop(1)

        //the permission with the longer modelId is the most precise
        for (i in 1..modelID.count()){
            allPermissionsList.forEach(){
                if(it.key == temp){
                    return it.value
                }
            }

            //delete chars util the next "/"
            temp = temp.dropLastWhile { !it.equals('/', ignoreCase = true) }
            //delete the "/"
            temp = temp.dropLast(1)
        }
        return EndpointModelElementPermission.DENY
    }

    fun resolvePermissions(userDetails: CloudioUserDetails): Collection<AbstractEndpointPermission> {
        // First get all endpoint permission the user itself has.
        val permissions: MutableList<AbstractEndpointPermission> = userEndpointPermissionRepository.findByUserID(userDetails.id).toMutableList()

        // Add all permissions that are higher as the user's permissions from it's group memberships.
        userGroupEndpointPermissionRepository.findByUserGroupIDIn(userDetails.groupMembershipIDs).forEach { groupPermission ->
            val existingPermission = permissions.find { it.endpointUUID == groupPermission.endpointUUID }
            if (existingPermission != null) {
                existingPermission.permission = existingPermission.permission.higher(groupPermission.permission)
                if (existingPermission.permission.fulfills(EndpointPermission.WRITE)) {
                    existingPermission.modelPermissions.clear()
                } else {
                    groupPermission.modelPermissions.forEach { (key, permission) ->
                        existingPermission.modelPermissions[key] = (existingPermission.modelPermissions[key] ?: EndpointModelElementPermission.DENY).higher(permission)
                    }
                }
            } else {
                permissions.add(groupPermission)
            }
        }

        return permissions
    }

    fun getAllEndpointModelElementPermissions(userDetails:CloudioUserDetails, endpointUUID:UUID): MutableList<Map.Entry<String, EndpointModelElementPermission>> {
        var allPermissionsList = mutableListOf<Map.Entry<String, EndpointModelElementPermission>>()
        var addedPermissions = mutableMapOf<String, EndpointModelElementPermission>()

        //Note: the user permissions are added to the list before the groups permissions
        //add all user permissions related to this endpoint to the list
        userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userDetails.id, endpointUUID).ifPresent {
            it.modelPermissions.forEach {
                allPermissionsList.add(it)
            }
            addedPermissions = it.modelPermissions
        }

        //add all group permissions related to this endpoint to the list
        userDetails.groupMembershipIDs.forEach { groupID ->
            userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, endpointUUID).ifPresent {
                it.modelPermissions.forEach { modelPermission ->
                    //add the groups permissions
                    //if a permission for an element already exists, keep the highest permissions level
                    if(!addedPermissions.containsKey(modelPermission.key)){
                        allPermissionsList.add(modelPermission)
                        addedPermissions[modelPermission.key]=modelPermission.value
                    }
                    else if(modelPermission.value.higher(addedPermissions.getOrDefault(modelPermission.key, EndpointModelElementPermission.DENY)) == modelPermission.value){
                        allPermissionsList.forEach {
                            if(it.key.equals(modelPermission.key)){
                                allPermissionsList.remove(it)
                                allPermissionsList.add(modelPermission)
                                addedPermissions[modelPermission.key]=modelPermission.value
                            }
                        }
                    }
                }
            }
        }

        allPermissionsList.forEach {
            if(it.key.endsWith("/#")){
                it.key.dropLast(2)
            }
        }

        //sort by the count of '/' in the key
        allPermissionsList.sortBy { it.key.filter { it == '/' }.count() }

        return allPermissionsList
    }
}