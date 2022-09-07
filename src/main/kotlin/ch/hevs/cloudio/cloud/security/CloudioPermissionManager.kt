package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.model.*
import org.apache.juli.logging.LogFactory
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.io.Serializable
import java.util.*

@Service
class CloudioPermissionManager(
        private val userEndpointPermissionRepository: UserEndpointPermissionRepository,
        private val userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository,
        private val endpointGroupRepository: EndpointGroupRepository,
        private val userEndpointGroupPermissionRepository: UserEndpointGroupPermissionRepository,
        private val endpointRepository: EndpointRepository,
        private val userEndpointGroupModelElementPermissionRepository: UserEndpointGroupPermissionRepository,
        private val userGroupEndpointGroupPermissionRepository: UserGroupEndpointGroupPermissionRepository,
        private val userGroupEndpointGroupModelElementPermissionRepository: UserGroupEndpointGroupPermissionRepository
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
        targetType == "EndpointGroup" && targetId is String && permission is EndpointPermission && authentication.principal is CloudioUserDetails -> hasEndpointGroupPermission(authentication.principal as CloudioUserDetails, targetId, permission)
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
        // Get the global permission of the user for the endpoint.
        val endpointPermission = resolveEndpointPermission(userDetails, modelID.endpoint)

        //Get the model element permissions list user - endpoint
        val permissionList = getAllEndpointModelElementPermissions(userDetails, modelID.endpoint)

        return hasEndpointModelElementPermission(userDetails, modelID, permission, endpointPermission, permissionList)
    }

    private fun hasEndpointModelElementPermission(userDetails: CloudioUserDetails, modelID: ModelIdentifier, permission: EndpointModelElementPermission
                                                  , endpointPermission: EndpointPermission, permissionList: MutableList<Map.Entry<String, EndpointModelElementPermission>>): Boolean {
        // Ensure that the model ID is correct.
        if (!modelID.valid) {
            log.warn("Permission $permission to \"$modelID \"rejected for user \"${userDetails.username}\": Invalid model identifier.")
            return false
        }

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

        //the higher level permission is selected
        var p = EndpointModelElementPermission.DENY
        for (i in 1..modelID.count()){
            permissionList.forEach(){
                if(it.key == temp && it.value.fulfills(p)){
                    p = it.value
                }
            }

            //delete chars util the next "/"
            temp = temp.dropLastWhile { !it.equals('/', ignoreCase = true) }
            //delete the "/"
            temp = temp.dropLast(1)
        }
        return p.fulfills(permission)
    }

    fun hasEndpointGroupPermission(userDetails: CloudioUserDetails, groupName: String, permission: EndpointPermission) = resolveEndpointGroupPermission(userDetails, groupName).fulfills(permission)

    fun resolveEndpointPermission(userDetails: CloudioUserDetails, endpointUUID: UUID): EndpointPermission {
        var permission = EndpointPermission.DENY

        //check user on endpoint
        userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userDetails.id, endpointUUID).ifPresent {
            permission = permission.higher(it.permission)
        }

        //check user group on endpoint
        if (!permission.fulfills(EndpointPermission.GRANT)) {
            userDetails.groupMembershipIDs.forEach { groupID ->
                userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, endpointUUID).ifPresent {
                    permission = permission.higher(it.permission)
                }
            }
        }

        //check user on endpoint group
        if (!permission.fulfills(EndpointPermission.GRANT)) {
            userEndpointGroupPermissionRepository.findByUserID(userDetails.id).forEach { userEndpointGroupPermission ->
                endpointGroupRepository.findById(userEndpointGroupPermission.endpointGroupID).ifPresent { endpointGroup ->
                    endpointRepository.findByGroupMembershipsContains(endpointGroup).forEach {
                        if(it.uuid == endpointUUID){
                            permission = permission.higher(userEndpointGroupPermission.permission)
                        }
                    }
                }
            }
        }

        //check user group on endpoint group
        if (!permission.fulfills(EndpointPermission.GRANT)){
            userDetails.groupMembershipIDs.forEach { userGroupID ->
                userGroupEndpointGroupPermissionRepository.findByUserGroupID(userGroupID).forEach { userGroupEndpointGroupPermission ->
                    endpointGroupRepository.findById(userGroupEndpointGroupPermission.endpointGroupID).ifPresent { endpointGroup ->
                        endpointRepository.findByGroupMembershipsContains(endpointGroup).forEach {
                            if(it.uuid == endpointUUID){
                                permission = permission.higher(userGroupEndpointGroupPermission.permission)
                            }
                        }
                    }
                }
            }
        }

        return permission
    }

    fun resolveEndpointGroupPermission(userDetails: CloudioUserDetails, endpointGroupName: String): EndpointPermission
    {
        var permission = EndpointPermission.DENY

        endpointGroupRepository.findByGroupName(endpointGroupName).ifPresent { endpointGroup ->
            userEndpointGroupPermissionRepository.findByUserIDAndEndpointGroupID(userDetails.id, endpointGroup.id).ifPresent {
                permission = permission.higher(it.permission)
            }
        }

        userDetails.groupMembershipIDs.forEach { userGroupId ->
            userGroupEndpointGroupPermissionRepository.findByUserGroupID(userGroupId).forEach { userGroupEndpointGroupPermission ->
                permission = permission.higher(userGroupEndpointGroupPermission.permission)
            }
        }

        return permission
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
                        existingPermission.modelPermissions[key] = (existingPermission.modelPermissions[key]
                                ?: EndpointModelElementPermission.DENY).higher(permission)
                    }
                }
            } else {
                permissions.add(groupPermission)
            }
        }

        // Add higher permissions from endpoint groups
        resolveEndpointGroupsPermissions(userDetails).forEach { userEndpointGroupPermission ->
            endpointGroupRepository.findById(userEndpointGroupPermission.endpointGroupID).ifPresent { endpointGroup ->
                endpointRepository.findByGroupMembershipsContains(endpointGroup).forEach{ endpoint ->
                    val existingPermission = permissions.find { it.endpointUUID == endpoint.uuid }
                    if(existingPermission != null){
                        existingPermission.permission = existingPermission.permission.higher(userEndpointGroupPermission.permission)
                        if (existingPermission.permission.fulfills(EndpointPermission.WRITE)) {
                            existingPermission.modelPermissions.clear()
                        } else {
                            userEndpointGroupPermission.modelPermissions.forEach { (key, permission) ->
                                existingPermission.modelPermissions[key] = (existingPermission.modelPermissions[key] ?: EndpointModelElementPermission.DENY).higher(permission)
                            }
                        }
                    }
                    else{
                        permissions.add(UserEndpointPermission(userDetails.id, endpoint.uuid, userEndpointGroupPermission.permission))
                    }
                }
            }
        }

        return permissions
    }

    fun resolveEndpointGroupsPermissions(userDetails: CloudioUserDetails): Collection<UserEndpointGroupPermission>
    {
        val permissions = userEndpointGroupPermissionRepository.findByUserID(userDetails.id).toMutableList()

        userGroupEndpointGroupPermissionRepository.findByUserGroupIDIn(userDetails.groupMembershipIDs).forEach { userGroupEndpointGroupPermission ->
            val existingPermission = permissions.find{it.endpointGroupID == userGroupEndpointGroupPermission.endpointGroupID}
            if (existingPermission != null){
                existingPermission.permission = existingPermission.permission.higher(userGroupEndpointGroupPermission.permission)
                if (existingPermission.permission.fulfills(EndpointPermission.WRITE)) {
                    existingPermission.modelPermissions.clear()
                } else {
                    userGroupEndpointGroupPermission.modelPermissions.forEach { (key, permission) ->
                        existingPermission.modelPermissions[key] = (existingPermission.modelPermissions[key] ?: EndpointModelElementPermission.DENY).higher(permission)
                    }
                }
            }
            else{
                permissions.add(UserEndpointGroupPermission(userDetails.id, userGroupEndpointGroupPermission.endpointGroupID, userGroupEndpointGroupPermission.permission))
            }
        }

        return permissions
    }

    fun getAllEndpointModelElementPermissions(userDetails:CloudioUserDetails, endpointUUID:UUID): MutableList<Map.Entry<String, EndpointModelElementPermission>> {
        val allPermissionsList = mutableListOf<Map.Entry<String, EndpointModelElementPermission>>()
        var addedPermissions = mutableMapOf<String, EndpointModelElementPermission>()

        //add all user permissions related to this endpoint to the list
        userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userDetails.id, endpointUUID).ifPresent {
            it.modelPermissions.forEach {
                allPermissionsList.add(it)
            }
            addedPermissions = it.modelPermissions
        }

        //add all group permissions related to this endpoint to the list
        userDetails.groupMembershipIDs.forEach { groupID ->
            userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(groupID, endpointUUID).ifPresent { userGroupEndpointPermission ->
                userGroupEndpointPermission.modelPermissions.forEach { modelPermission ->
                    //add the groups permissions
                    //if a permission for an element already exists, keep the highest permissions level
                    if(!addedPermissions.containsKey(modelPermission.key)){
                        allPermissionsList.add(modelPermission)
                        addedPermissions[modelPermission.key]=modelPermission.value
                    }
                    else if(modelPermission.value.higher(addedPermissions.getOrDefault(modelPermission.key, EndpointModelElementPermission.DENY)) == modelPermission.value){
                        allPermissionsList.forEach {
                            if(it.key == modelPermission.key){
                                allPermissionsList.remove(it)
                                allPermissionsList.add(modelPermission)
                                addedPermissions[modelPermission.key]=modelPermission.value
                            }
                        }
                    }
                }
            }
        }

        //add the user - endpoint group permissions
        endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
            endpoint.groupMemberships.forEach { endpointGroup ->
                userEndpointGroupModelElementPermissionRepository.findByUserIDAndEndpointGroupID(userDetails.id, endpointGroup.id).ifPresent { userEndpointGroupPermission ->
                    userEndpointGroupPermission.modelPermissions.forEach{ modelPermission ->
                        if(!addedPermissions.containsKey(modelPermission.key)){
                            allPermissionsList.add(modelPermission)
                            addedPermissions[modelPermission.key]=modelPermission.value
                        }
                        else if(modelPermission.value.higher(addedPermissions.getOrDefault(modelPermission.key, EndpointModelElementPermission.DENY)) == modelPermission.value){
                            allPermissionsList.forEach {
                                if(it.key == modelPermission.key){
                                    allPermissionsList.remove(it)
                                    allPermissionsList.add(modelPermission)
                                    addedPermissions[modelPermission.key]=modelPermission.value
                                }
                            }
                        }
                    }
                }
            }
        }

        //Add the user group - endpoint group permissions
        endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
            endpoint.groupMemberships.forEach { endpointGroup ->
                userGroupEndpointGroupPermissionRepository.findByUserGroupIDIn(userDetails.groupMembershipIDs).forEach { userGroupEndpointGroupPermission ->
                    userGroupEndpointGroupPermission.modelPermissions.forEach{ modelPermission ->
                        if(!addedPermissions.containsKey(modelPermission.key)){
                            allPermissionsList.add(modelPermission)
                            addedPermissions[modelPermission.key]=modelPermission.value
                        }
                        else if(modelPermission.value.higher(addedPermissions.getOrDefault(modelPermission.key, EndpointModelElementPermission.DENY)) == modelPermission.value){
                            allPermissionsList.forEach {
                                if(it.key == modelPermission.key){
                                    allPermissionsList.remove(it)
                                    allPermissionsList.add(modelPermission)
                                    addedPermissions[modelPermission.key]=modelPermission.value
                                }
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

    /**
     * filter the given structure on the given EndpointModelElementPermission
     */
    fun filter(data: Any, cloudioUserDetails: CloudioUserDetails, modelIdentifier: ModelIdentifier,
               permission: EndpointModelElementPermission): Any? {

        val permissionList = getAllEndpointModelElementPermissions(cloudioUserDetails, modelIdentifier.endpoint)
        val endpointPermission = resolveEndpointPermission(cloudioUserDetails, modelIdentifier.endpoint)

        when (data) {
            is EndpointDataModel -> {
                return filterEndpoint(data, cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)
            }
            is Node -> {
                return filterNode(data, cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)
            }
            is CloudioObject -> {
                return filterObject(data, cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)
            }
            is Attribute -> {
                return filterAttribute(data, cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)
            }
        }

        return null
    }

    private fun filterEndpoint(endpoint: EndpointDataModel, cloudioUserDetails: CloudioUserDetails, modelIdentifier: ModelIdentifier,
                       permission: EndpointModelElementPermission, endpointPermission: EndpointPermission, permissionList: MutableList<Map.Entry<String, EndpointModelElementPermission>>): EndpointDataModel? {
        val e = EndpointDataModel()
        e.messageFormatVersion = endpoint.messageFormatVersion
        e.supportedFormats = endpoint.supportedFormats
        e.version = endpoint.version

        endpoint.nodes.forEach {
            val nodeId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
            val temp = filterNode(it.value, cloudioUserDetails, nodeId, permission, endpointPermission, permissionList)
            if (temp is Node) {
                e.nodes[it.key] = temp
            }
        }

        if (e.nodes.isNotEmpty()) {
            return e
        }

        return null
    }


    private fun filterNode(node: Node, cloudioUserDetails: CloudioUserDetails, modelIdentifier: ModelIdentifier,
                   permission: EndpointModelElementPermission, endpointPermission: EndpointPermission, permissionList: MutableList<Map.Entry<String, EndpointModelElementPermission>>): Node? {

        if (hasEndpointModelElementPermission(cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)) {
            return node
        }
        val n = Node()
        n.online = n.online
        n.implements = n.implements

        node.objects.forEach {
            val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
            val temp = filterObject(it.value,  cloudioUserDetails, objId, permission, endpointPermission, permissionList)
            if (temp is CloudioObject) {
                n.objects[it.key] = temp
            }
        }

        if (n.objects.isNotEmpty()) {
            return n
        }

        return null
    }

    private fun filterObject(obj: CloudioObject, cloudioUserDetails: CloudioUserDetails, modelIdentifier: ModelIdentifier,
                     permission: EndpointModelElementPermission, endpointPermission: EndpointPermission, permissionList: MutableList<Map.Entry<String, EndpointModelElementPermission>>): CloudioObject? {

        if (hasEndpointModelElementPermission(cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)) {
            return obj
        }

        val o = CloudioObject()
        o.conforms = obj.conforms

        obj.objects.forEach {
            val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
            val temp = filterObject(it.value,  cloudioUserDetails, objId, permission, endpointPermission, permissionList)
            if (temp is CloudioObject) {
                o.objects[it.key] = temp
            }
        }

        obj.attributes.forEach {
            val attrId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
            val temp = filterAttribute(it.value,  cloudioUserDetails, attrId, permission, endpointPermission, permissionList)
            if (temp is Attribute) {
                o.attributes[it.key] = temp
            }
        }

        if (o.objects.isNotEmpty() || o.attributes.isNotEmpty()) {
            return o
        }

        return null
    }

    private fun filterAttribute(attribute: Attribute, cloudioUserDetails: CloudioUserDetails, modelIdentifier: ModelIdentifier,
                        permission: EndpointModelElementPermission, endpointPermission: EndpointPermission, permissionList: MutableList<Map.Entry<String, EndpointModelElementPermission>>): Attribute? {

        if (hasEndpointModelElementPermission(cloudioUserDetails,
                        modelIdentifier, permission, endpointPermission, permissionList)) {
            return attribute
        }

        return null
    }

    /**
     * Merge the noDataStructure in data
     * if an element does not exist in data, add it from noDataStructure
     */
    fun merge(data: Any?, noDataStructure: Any?): Any? {
        if(data == null){
            return noDataStructure
        }
        if(noDataStructure == null){
            return data
        }

        when (data) {
            is EndpointDataModel -> {
                if (noDataStructure is EndpointDataModel){
                    return mergeEndpoint(data, noDataStructure)
                }
            }
            is Node -> {
                if (noDataStructure is Node){
                    return mergeNode(data, noDataStructure)
                }
            }
            is CloudioObject -> {
                if (noDataStructure is CloudioObject){
                    return mergeObject(data, noDataStructure)
                }
            }
            is Attribute -> {
                return data
            }
        }

        return null
    }

    private fun mergeEndpoint(data: EndpointDataModel, noDataStructure: EndpointDataModel): EndpointDataModel {
        noDataStructure.nodes.forEach {
            val n = Node()
            n.implements = it.value.implements
            n.online = it.value.online


            data.nodes[it.key] = mergeNode(data.nodes.getOrDefault(it.key, n), it.value)
        }
        return data
    }

    private fun mergeNode(data: Node, noDataStructure: Node): Node {
        noDataStructure.objects.forEach {
            val o = CloudioObject()
            o.conforms = it.value.conforms

            data.objects[it.key] = mergeObject(data.objects.getOrDefault(it.key, o), it.value)
        }
        return data
    }

    private fun mergeObject(data: CloudioObject, noDataStructure: CloudioObject): CloudioObject {
        noDataStructure.objects.forEach {
            val o = CloudioObject()
            o.conforms = it.value.conforms

            data.objects[it.key] = mergeObject(data.objects.getOrDefault(it.key, o), it.value)
        }

        noDataStructure.attributes.forEach {
            data.attributes.putIfAbsent(it.key, it.value)
        }

        return data
    }
}