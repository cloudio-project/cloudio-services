package ch.hevs.cloudio2.cloud.utils

import ch.hevs.cloudio2.cloud.model.Permission
import ch.hevs.cloudio2.cloud.model.PrioritizedPermission
import ch.hevs.cloudio2.cloud.repo.authentication.UserGroupRepository
import org.springframework.data.repository.findByIdOrNull

object PermissionUtils {

    fun permissionFromGroup(initialUserPermission:  Map<String, PrioritizedPermission>,userGroupSet: Set<String>, userGroupRepository: UserGroupRepository): Map<String, PrioritizedPermission>{
        var toReturn = initialUserPermission.toMutableMap()
        //iter through group
        userGroupSet.forEachIndexed{ _, userGroup->
            //get the group permission
            userGroupRepository.findByIdOrNull(userGroup)?.permissions?.forEach { topicKey, prioritizedPermission ->
                //if topic permission exist
                if(toReturn[topicKey] != null) {
                    if (prioritizedPermission.priority > toReturn[topicKey]!!.priority) //test if priority of permission is higher
                        toReturn[topicKey] = prioritizedPermission

                }
                else    //if doesn't exist, create it
                    toReturn[topicKey] = prioritizedPermission

            }
        }
        return toReturn
    }

    fun getRelatedPermission(permissionMap: Map<String, PrioritizedPermission>, formatedTopic: List<String>): Map<String, PrioritizedPermission> {
        //take all the permission that are linked to the topic according
        //to MQTT wildcare syntax
        var permissionMapToReturn = permissionMap
        formatedTopic.forEachIndexed { index, value ->
            permissionMapToReturn = permissionMapToReturn.filterKeys {
                val splitTopic = it.split("/")
                if (splitTopic.size <= index)
                    splitTopic.last() == "#"
                else
                    listOf("#", "*", value).contains(splitTopic[index])
            }
        }
        return permissionMapToReturn
    }

    fun getHigherPriorityPermission(permissionMap: Map<String, PrioritizedPermission>, formatedTopic: List<String>): Permission {

        val relatedPermissionMap = getRelatedPermission(permissionMap, formatedTopic)
        return if (relatedPermissionMap.isEmpty()) {
            Permission.DENY
        } else {
            //sort the permission by it's priority and only get the highest one
            val permissionList = relatedPermissionMap.toList()
                    .sortedBy { (key, value) -> value.priority }

            permissionList.last().second.permission
        }
    }
}