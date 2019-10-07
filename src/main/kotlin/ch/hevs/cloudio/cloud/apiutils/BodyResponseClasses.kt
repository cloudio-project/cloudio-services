package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.model.Priority

data  class UserRequest(val userName : String)

data  class UserTopicRequest(val userName : String, val topic : String)

data class UserRightTopic(val topic: String, val permission : Permission, val priority : Priority)

data class UserRightRequestList(val userName : String, val userRights : Set<UserRightTopic>)

data class UserRightRequest(val userName : String, val userRight : UserRightTopic)

