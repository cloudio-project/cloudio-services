package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity

// ------ Exception used in the API ------------------------------------------------------------------------------------
class CloudioApiException(message : String) : Exception(message)

// ------ User Management data class -----------------------------------------------------------------------------------
data  class UserRequest(val userName : String)

data class UserPasswordRequest(val userName: String,val passwordHash: String)

data class AddAuthorityRequest(val userName: String, val authorities: Set<Authority>)

data class RemoveAuthorityRequest(val userName: String, val authority: Authority)

data class UserListAnswer(val userList: Set<String>)

// ------ User Access Control data class -------------------------------------------------------------------------------
data  class UserTopicRequest(val userName : String, val topic : String)

data class UserRightTopic(val topic: String, val permission : Permission, val priority : PermissionPriority)

data class UserRightRequestList(val userName : String, val userRights : Set<UserRightTopic>)

data class UserRightRequest(val userName : String, val userRight : UserRightTopic)

// ------ User Group data class ----------------------------------------------------------------------------------------
data class UserGroupRequest(val userGroupName : String)

data  class UserGroupTopicRequest(val userGroupName : String, val topic : String)

data class UserGroupUserRequestList(val userGroupName: String, val users: Set<String>)

data class UserGroupUserRequest(val userGroupName: String, val user: String)

data class UserGroupList(val userGroupList: Set<String>)

// ------ User Group Access Control data class -------------------------------------------------------------------------
data class UserGroupRightTopic(val topic: String, val permission : Permission, val priority : PermissionPriority)

data class UserGroupRightRequestList(val userGroupName : String, val userGroupRights : Set<UserGroupRightTopic>)

data class UserGroupRightRequest(val userGroupName : String, val userGroupRight : UserGroupRightTopic)

// ------ Endpoint Management data class --------------------------------------------------------------------------------
data class EndpointCreateRequest(val endpointFriendlyName: String)

data class EndpointRequest(val endpointUuid: String)

data class EndpointAnswer(val endpointFriendlyName: String, val endpointEntity: EndpointEntity)

data class EndpointFriendlyName(val endpointFriendlyName: String)

data class NodeRequest(val nodeTopic: String)

data class ObjectRequest(val objectTopic: String)

data class AttributeRequest(val attributeTopic: String)

data class AttributeRequestLongpoll(val attributeTopic: String, val timeout: Long)

data class AttributeSetRequest(val attributeTopic: String, val attribute: Attribute)

data class EndpointParameters (var endpointUuid: String,var friendlyName: String)

data class EndpointParametersAndBlock (var endpointUuid: String,var friendlyName: String, var blocked: Boolean?)

data class OwnedEndpointsAnswer(val ownedEndpoints: Set<EndpointParametersAndBlock> )

data class AccessibleAttributesAnswer(val accessibleAttributes:  Map<String, Permission> )

// ------ Certificate data class ---------------------------------------------------------------------------------------
data class CertificateAndKeyRequest(val endpointUuid: String)

enum class LibraryLanguage{JAVA}

data class CertificateAndKeyZipRequest(val endpointUuid: String, val libraryLanguage: LibraryLanguage)

data class CertificateFromKeyRequest(val endpointUuid: String, val publicKey: String)

data class CaCertificate(val caCertificate: String)

// ------ History data class -------------------------------------------------------------------------------------------
data class HistoryDefaultRequest(val attributeTopic: String, val maxDataPoints: Long)

data class HistoryDateRequest(val attributeTopic: String,val dateStart: String, val dateStop: String)

data class HistoryWhereRequest(val attributeTopic: String, val where: String)

enum class AggregationInflux{COUNT, DISTINCT, INTEGRAL, MEAN, MEDIAN, MODE, SUM}

enum class FillInflux(val value: String){NULL("null"), NONE("none"), ZERO("0"), PREVIOUS("previous"), LINEAR("linear")}

data class HistoryExpertRequest(val attributeTopic: String, val aggregation: AggregationInflux, val dateStart: String, val dateStop: String, val interval: String, val fill: FillInflux, val maxDataPoints: Long)

// ------ Logs data class ----------------------------------------------------------------------------------------------
data class LogsDefaultRequest(val endpointUuid: String, val maxDataPoints: Long)

data class LogsDateRequest(val endpointUuid: String,val dateStart: String, val dateStop: String)

data class LogsWhereRequest(val endpointUuid: String, val where: String)

data class LogsSetRequest(val endpointUuid: String, val level: LogLevel)

data class LogsGetRequest(val endpointUuid: String)

data class LogsGetAnswer(val level: LogLevel)

// ------ Jobs data class ----------------------------------------------------------------------------------------------
data class JobExecuteRequest(val endpointUuid: String, val jobURI: String, val getOutput: Boolean, val correlationID: String, val data: String = "", val timeout: Long)