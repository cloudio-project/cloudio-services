package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.Priority

data class ApiActionAnswer(val success: Boolean, val message: String)

data  class UserRequest(val userName : String)

data  class UserTopicRequest(val userName : String, val topic : String)

data class UserRightTopic(val topic: String, val permission : Permission, val priority : Priority)

data class UserRightRequestList(val userName : String, val userRights : Set<UserRightTopic>)

data class UserRightRequest(val userName : String, val userRight : UserRightTopic)

data class UserGroupRequest(val userGroupName : String)

data  class UserGroupTopicRequest(val userGroupName : String, val topic : String)

data class UserGroupUserRequestList(val userGroupName: String, val users: Set<String>)

data class UserGroupUserRequest(val userGroupName: String, val user: String)

data class UserGroupList(val userGroupList: Set<String>)

data class UserGroupRightTopic(val topic: String, val permission : Permission, val priority : Priority)

data class UserGroupRightRequestList(val userGroupName : String, val userGroupRights : Set<UserGroupRightTopic>)

data class UserGroupRightRequest(val userGroupName : String, val userGroupRight : UserGroupRightTopic)
