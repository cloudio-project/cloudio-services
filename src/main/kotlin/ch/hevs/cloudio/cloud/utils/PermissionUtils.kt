package ch.hevs.cloudio.cloud.utils

import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.AttributeType
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import org.springframework.data.repository.findByIdOrNull


object PermissionUtils {

    fun permissionFromUserAndGroup(userName: String, userRepository: MONGOUserRepository, userGroupRepository: MONGOUserGroupRepository): Map<String, PrioritizedPermission> {
        val initialUserPermission = userRepository.findById(userName).get().permissions
        val userGroupSet = userRepository.findById(userName).get().userGroups

        val toReturn = initialUserPermission.toMutableMap()

        //iter through group
        userGroupSet.forEachIndexed { _, userGroup ->
            //get the group permission
            userGroupRepository.findByIdOrNull(userGroup)?.permissions?.forEach { topicKey, prioritizedPermission ->
                //if topic permission exist
                if (toReturn[topicKey] != null) {
                    if (prioritizedPermission.priority > toReturn[topicKey]!!.priority) //test if priority of permission is higher
                        toReturn[topicKey] = prioritizedPermission
                } else    //if doesn't exist, create it
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
                    listOf("#", "*", "+", value).contains(splitTopic[index])
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
                    .sortedBy { (_, value) -> value.priority }

            permissionList.last().second.permission
        }
    }

    // TODO: This can be done using @PostFilter annotation and PermissionEvaluator bean.
    fun censorNodeFromUserPermission(permissionMap: Map<String, PrioritizedPermission>, topic: String, node: Node) {

        for (cloudioObject in node.objects) {
            val topicObject = topic + cloudioObject.key + "/"
            censorObjectFromUserPermission(permissionMap, topicObject, cloudioObject.value)
        }
    }

    // TODO: This can be done using @PostFilter annotation and PermissionEvaluator bean.
    fun censorObjectFromUserPermission(permissionMap: Map<String, PrioritizedPermission>, topic: String, cloudioObject: CloudioObject) {
        val innerTopic = topic
        for (attribute in cloudioObject.attributes) {
            val innerTopicAttribute = innerTopic + attribute.key
            val endpointPermission = getHigherPriorityPermission(permissionMap, innerTopicAttribute.split("/"))
            if (endpointPermission == Permission.DENY) {
                attribute.value.constraint = AttributeConstraint.Invalid
                attribute.value.type = AttributeType.Invalid
                attribute.value.timestamp = -1.0
                attribute.value.value = "You don't have the right to see this attribute value"
            }
        }

        for (innerCloudioObject in cloudioObject.objects) {
            val innerTopicObject = innerTopic + innerCloudioObject.key + "/"
            censorObjectFromUserPermission(permissionMap, innerTopicObject, innerCloudioObject.value)
        }
    }

    // TODO: This can be done using @PostFilter annotation and PermissionEvaluator bean.
    fun getAccessibleAttributesFromNode(permissionMap: Map<String, PrioritizedPermission>, topic: String, node: Node, previousAttributesRight: MutableMap<String, Permission>): MutableMap<String, Permission> {

        val attributesRight: MutableMap<String, Permission> = mutableMapOf()
        attributesRight.putAll(previousAttributesRight)
        for (cloudioObject in node.objects) {
            val topicObject = topic + cloudioObject.key + "/"
            attributesRight.putAll(getAccessibleAttributesFromObject(permissionMap, topicObject, cloudioObject.value, attributesRight))
        }
        return attributesRight
    }

    // TODO: This can be done using @PostFilter annotation and PermissionEvaluator bean.
    fun getAccessibleAttributesFromObject(permissionMap: Map<String, PrioritizedPermission>, topic: String, cloudioObject: CloudioObject, previousAttributesRight: MutableMap<String, Permission>): MutableMap<String, Permission> {
        val innerTopic = topic
        val attributesRight: MutableMap<String, Permission> = mutableMapOf()
        attributesRight.putAll(previousAttributesRight)
        for (attribute in cloudioObject.attributes) {
            val innerTopicAttribute = innerTopic + attribute.key
            val endpointPermission = getHigherPriorityPermission(permissionMap, innerTopicAttribute.split("/"))
            if (endpointPermission != Permission.DENY)
                attributesRight[innerTopicAttribute] = endpointPermission
        }

        for (innerCloudioObject in cloudioObject.objects) {
            val innerTopicObject = innerTopic + innerCloudioObject.key + "/"
            getAccessibleAttributesFromObject(permissionMap, innerTopicObject, innerCloudioObject.value, attributesRight)
        }
        return attributesRight
    }
}