package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat.serializeAttribute
import ch.hevs.cloudio.cloud.serialization.JsonWotSerializationFormat
import ch.hevs.cloudio.cloud.serialization.wot.WotNode
import ch.hevs.cloudio.cloud.utils.CloudioModelUtils
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import java.util.*


object  EndpointManagementUtil{

    fun createEndpoint(endpointParametersRepository: EndpointParametersRepository, endpointCreateRequest: EndpointCreateRequest): EndpointParameters {
        val toReturn = EndpointParameters(UUID.randomUUID().toString(), endpointCreateRequest.endpointFriendlyName)

        endpointParametersRepository.save(toReturn)

        return toReturn
    }

    fun getEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): EndpointEntity? {
        return endpointEntityRepository.findByIdOrNull(endpointRequest.endpointUuid)
    }

    fun getNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest): Node? {
        val splittedTopic = nodeRequest.nodeTopic.split("/")
        return endpointEntityRepository.findByIdOrNull(splittedTopic[0])?.endpoint?.nodes?.get(splittedTopic[1])
    }

    fun getWotNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest, host: String): WotNode? {

        val splitedTopic = nodeRequest.nodeTopic.split("/")
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splitedTopic[0])!!

        return JsonWotSerializationFormat.wotNodeFromCloudioNode(endpointEntity.endpoint, endpointEntity.id, splitedTopic[1], host)
    }

    fun getObject(endpointEntityRepository: EndpointEntityRepository, objectRequest: ObjectRequest): CloudioObject? {
        val splittedTopic = Stack<String>()
        splittedTopic.addAll(objectRequest.objectTopic.split("/").toList().reversed())
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splittedTopic.pop())
        if (endpointEntity != null) {
            val node = endpointEntity.endpoint.nodes[splittedTopic.pop()]
            if (node != null) {
                return CloudioModelUtils.findObjectInNode(node, splittedTopic)
            }
        }
        return null

    }

    fun getAttribute(endpointEntityRepository: EndpointEntityRepository, attributeRequest: AttributeRequest): Attribute? {
        val splittedTopic = Stack<String>()
        splittedTopic.addAll(attributeRequest.attributeTopic.split("/").toList().reversed())
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splittedTopic.pop())
        if (endpointEntity != null) {
            val node = endpointEntity.endpoint.nodes[splittedTopic.pop()]
            if (node != null) {
                return CloudioModelUtils.findAttributeInNode(node, splittedTopic)
            }
        }
        return null
    }

    fun setAttribute(rabbitTemplate: RabbitTemplate, endpointEntityRepository: EndpointEntityRepository, attributeSetRequest: AttributeSetRequest): ApiActionAnswer{

        val attribute = getAttribute(endpointEntityRepository, AttributeRequest(attributeSetRequest.attributeTopic))

        if (attribute == null)
            return ApiActionAnswer(false, "Attribute doesn't exist")
        else if(attribute.constraint != AttributeConstraint.SetPoint && attribute.constraint != AttributeConstraint.Parameter)
            return ApiActionAnswer(false, "Attribute is nor a SetPoint, neither a Parameter")
        else {
            rabbitTemplate.convertSendAndReceive("amq.topic",
                    "@set." + attributeSetRequest.attributeTopic.replace("/", "."), serializeAttribute(attributeSetRequest.attribute))

            return ApiActionAnswer(true, "")
        }
    }

    fun getOwnedEndpoints(userRepository: UserRepository, userGroupRepository: UserGroupRepository, userName: String): OwnedEndpointsAnswer{
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val ownedEndpointsSet : MutableSet<String> = mutableSetOf()
        permissionMap.forEach { (topic, prioritizedPermission) ->
            val splitTopic = topic.split("/")
            if(splitTopic.size==2 && splitTopic.getOrNull(1).equals("#") && prioritizedPermission.permission == Permission.OWN){
                ownedEndpointsSet.add(splitTopic[0])
            }
        }
        return OwnedEndpointsAnswer(ownedEndpointsSet)
    }

    fun getAccessibleAttributes(userRepository: UserRepository, userGroupRepository: UserGroupRepository, endpointEntityRepository: EndpointEntityRepository, userName: String): AccessibleAttributesAnswer{
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val endpointsSet : MutableSet<String> = mutableSetOf()
        permissionMap.forEach { (topic, _) ->
            endpointsSet.add(topic.split("/")[0])
        }

        val toReturn : MutableMap<String, Permission> = mutableMapOf()

        endpointsSet.forEach { endpointUuid ->
            val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointUuid)
            if (endpointEntity != null)
                toReturn.putAll(PermissionUtils.getAccessibleAttributesFromEndpoint(permissionMap, endpointEntity))
        }
        return AccessibleAttributesAnswer(toReturn)
    }
}

