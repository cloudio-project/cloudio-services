package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.extension.findAttribute
import ch.hevs.cloudio.cloud.extension.findObject
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat.serializeAttribute
import ch.hevs.cloudio.cloud.serialization.JsonWotSerializationFormat
import ch.hevs.cloudio.cloud.serialization.wot.NodeThingDescription
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import java.util.*


object EndpointManagementUtil {

    fun createEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointCreateRequest: EndpointCreateRequest): EndpointParameters {
        val toReturn = EndpointParameters(UUID.randomUUID(), endpointCreateRequest.endpointFriendlyName)
        //create endpoint in endpoint parameters repo
        endpointEntityRepository.save(EndpointEntity(endpointUuid = toReturn.endpointUuid, friendlyName = toReturn.friendlyName))
        return toReturn
    }

    fun getEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): EndpointEntity? {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointRequest.endpointUuid))
        return if (endpointEntity == null)
            null
        else    //return endpoint entity and endpoint friendly name
            endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointRequest.endpointUuid))
    }

    fun getEndpointFriendlyName(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): EndpointFriendlyName? {
        val endpointParameters = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointRequest.endpointUuid))
        return if (endpointParameters == null)
            null
        else
            EndpointFriendlyName(endpointParameters.friendlyName)
    }

    @Throws(CloudioApiException::class)
    fun getNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest): Node? {
        val splitTopic = nodeRequest.nodeTopic.split("/")
        if (splitTopic.size < 2)
            throw CloudioApiException("Node topic wasn't formatted correctly")
        return endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))?.endpoint?.nodes?.get(splitTopic[1])
    }

    @Throws(CloudioApiException::class)
    fun getWotNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest, host: String): NodeThingDescription? {
        val splitTopic = nodeRequest.nodeTopic.split("/")
        if (splitTopic.size < 2)
            throw CloudioApiException("Node topic wasn't formatted correctly")
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!

        return JsonWotSerializationFormat.wotNodeFromCloudioNode(endpointEntity.endpoint, endpointEntity.endpointUuid.toString(), splitTopic[1], host)
    }

    @Throws(CloudioApiException::class)
    fun getObject(endpointEntityRepository: EndpointEntityRepository, objectRequest: ObjectRequest): CloudioObject? {
        val splitTopic = Stack<String>()
        splitTopic.addAll(objectRequest.objectTopic.split("/").toList().reversed())
        try {
            val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic.pop()))
            if (endpointEntity != null) {
                val node = endpointEntity.endpoint.nodes[splitTopic.pop()]
                if (node != null) {
                    return node.findObject(splitTopic)
                } else
                    throw CloudioApiException("Object doesn't exist")
            } else
                throw CloudioApiException("Endpoint doesn't exist")
        } catch (e: EmptyStackException) {
            throw CloudioApiException("Object topic wasn't formatted correctly")
        }
    }

    @Throws(CloudioApiException::class)
    fun getAttribute(endpointEntityRepository: EndpointEntityRepository, attributeRequest: AttributeRequest): Attribute? {
        val splitTopic = Stack<String>()
        splitTopic.addAll(attributeRequest.attributeTopic.split("/").toList().reversed())
        try {
            val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic.pop()))
            if (endpointEntity != null) {
                val node = endpointEntity.endpoint.nodes[splitTopic.pop()]
                if (node != null) {
                    return node.findAttribute(splitTopic)
                } else
                    throw CloudioApiException("Node doesn't exist")
            } else
                throw CloudioApiException("Endpoint doesn't exist")
        } catch (e: EmptyStackException) {
            throw CloudioApiException("Attribute topic wasn't formatted correctly")
        }
    }

    @Throws(CloudioApiException::class)
    fun setAttribute(rabbitTemplate: RabbitTemplate, endpointEntityRepository: EndpointEntityRepository, attributeSetRequest: AttributeSetRequest) {
        val attribute: Attribute?

        try {
            attribute = getAttribute(endpointEntityRepository, AttributeRequest(attributeSetRequest.attributeTopic))
        } catch (e: CloudioApiException) {
            throw e
        }

        //only set attribute if setpoint or parameter
        if (attribute == null)
            throw CloudioApiException("Attribute doesn't exist")
        else if (attribute.constraint != AttributeConstraint.SetPoint && attribute.constraint != AttributeConstraint.Parameter)
            throw CloudioApiException("Attribute is nor a SetPoint, neither a Parameter")
        else {
            //send message to amq.topic queue
            rabbitTemplate.convertAndSend("amq.topic",
                    "@set." + attributeSetRequest.attributeTopic.replace("/", "."),
                    serializeAttribute(attributeSetRequest.attribute))
        }
    }

    fun blockEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): Boolean {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointRequest.endpointUuid))
        if (endpointEntity != null) {
            endpointEntity.blocked = true
            endpointEntityRepository.save(endpointEntity)
            return true
        } else
            return false
    }

    fun unblockEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): Boolean {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointRequest.endpointUuid))
        if (endpointEntity != null) {
            endpointEntity.blocked = false
            endpointEntityRepository.save(endpointEntity)
            return true
        } else
            return false
    }

    fun getOwnedEndpoints(userRepository: UserRepository, userGroupRepository: UserGroupRepository, endpointEntityRepository: EndpointEntityRepository, userName: String): OwnedEndpointsAnswer {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val ownedEndpointsSet: MutableSet<String> = mutableSetOf()
        //search for permission looking like topic = endpointUuid/#, permission = OWN
        permissionMap.forEach { (topic, prioritizedPermission) ->
            val splitTopic = topic.split("/")
            if (splitTopic.size == 2 && splitTopic.getOrNull(1).equals("#") && prioritizedPermission.permission == Permission.OWN) {
                ownedEndpointsSet.add(splitTopic[0])
            }
        }
        val ownedEndpointParametersSet: MutableSet<EndpointParametersAndBlock> = mutableSetOf()
        //search for friendly name of each owned endpoint
        ownedEndpointsSet.forEach { s ->
            val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(s))
            val friendlyName = endpointEntity?.friendlyName ?: "Endpoint not in the database"
            val blocked = endpointEntity?.blocked
            ownedEndpointParametersSet.add(EndpointParametersAndBlock(s, friendlyName, blocked))
        }
        return OwnedEndpointsAnswer(ownedEndpointParametersSet)
    }

    fun getAccessibleAttributes(userRepository: UserRepository, userGroupRepository: UserGroupRepository, endpointEntityRepository: EndpointEntityRepository, userName: String): AccessibleAttributesAnswer {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val endpointsSet: MutableSet<String> = mutableSetOf()
        //get every endpointUuid found in the permission map of user
        permissionMap.forEach { (topic, _) ->
            endpointsSet.add(topic.split("/")[0])
        }

        val toReturn: MutableMap<String, Permission> = mutableMapOf()
        //will search for every attributes in the endpointUuid set
        endpointsSet.forEach { endpointUuid ->
            val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointUuid))
            if (endpointEntity != null)
                toReturn.putAll(PermissionUtils.getAccessibleAttributesFromEndpoint(permissionMap, endpointEntity))
        }
        return AccessibleAttributesAnswer(toReturn)
    }

}

