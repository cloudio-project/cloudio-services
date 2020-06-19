package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import java.util.*

// ------ Exception used in the API ------------------------------------------------------------------------------------
class CloudioApiException(message: String) : Exception(message)

// ------ User Management data class -----------------------------------------------------------------------------------
data class UserRequest(val userName: String)

data class UserPasswordRequest(val userName: String, val passwordHash: String)

data class AddAuthorityRequest(val userName: String, val authorities: Set<Authority>)

data class RemoveAuthorityRequest(val userName: String, val authority: Authority)

data class UserListAnswer(val userList: Set<String>)

// ------ User Access Control data class -------------------------------------------------------------------------------
data class UserTopicRequest(val userName: String, val topic: String)

data class UserRightTopic(val topic: String, val permission: Permission, val priority: PermissionPriority)

data class UserRightRequestList(val userName: String, val userRights: Set<UserRightTopic>)

data class UserRightRequest(val userName: String, val userRight: UserRightTopic)

// ------ User Group data class ----------------------------------------------------------------------------------------
data class UserGroupRequest(val userGroupName: String)

data class UserGroupTopicRequest(val userGroupName: String, val topic: String)

data class UserGroupUserRequestList(val userGroupName: String, val users: Set<String>)

data class UserGroupUserRequest(val userGroupName: String, val user: String)

data class UserGroupList(val userGroupList: Set<String>)

// ------ User Group Access Control data class -------------------------------------------------------------------------
data class UserGroupRightTopic(val topic: String, val permission: Permission, val priority: PermissionPriority)

data class UserGroupRightRequestList(val userGroupName: String, val userGroupRights: Set<UserGroupRightTopic>)

data class UserGroupRightRequest(val userGroupName: String, val userGroupRight: UserGroupRightTopic)

// ------ Jobs data class ----------------------------------------------------------------------------------------------
data class JobExecuteRequest(val endpointUuid: String, val jobURI: String, val getOutput: Boolean, val correlationID: String, val data: String = "", val timeout: Long)