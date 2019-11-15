package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.*
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
        //create endpoint in endpoint parameters repo
        endpointParametersRepository.save(toReturn)
        return toReturn
    }

    fun getEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointParametersRepository: EndpointParametersRepository, endpointRequest: EndpointRequest): EndpointAnswer? {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointRequest.endpointUuid)
        return if(endpointEntity == null)
            null
        else    //return endpoint entity and endpoint friendly name
            EndpointAnswer(endpointParametersRepository.findById(endpointRequest.endpointUuid).get().friendlyName, endpointEntity)
    }

    fun getEndpointFriendlyName(endpointParametersRepository: EndpointParametersRepository, endpointRequest: EndpointRequest): EndpointFriendlyName?{
        val endpointParameters = endpointParametersRepository.findByIdOrNull(endpointRequest.endpointUuid)
        return if(endpointParameters == null)
            null
        else
            EndpointFriendlyName(endpointParameters.friendlyName)
    }

    fun getNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest): Node? {
        val splitTopic = nodeRequest.nodeTopic.split("/")
        return endpointEntityRepository.findByIdOrNull(splitTopic[0])?.endpoint?.nodes?.get(splitTopic[1])
    }

    fun getWotNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest, host: String): WotNode? {
        val splitTopic = nodeRequest.nodeTopic.split("/")
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splitTopic[0])!!

        return JsonWotSerializationFormat.wotNodeFromCloudioNode(endpointEntity.endpoint, endpointEntity.endpointUuid, splitTopic[1], host)
    }

    fun getObject(endpointEntityRepository: EndpointEntityRepository, objectRequest: ObjectRequest): CloudioObject? {
        val splitTopic = Stack<String>()
        splitTopic.addAll(objectRequest.objectTopic.split("/").toList().reversed())
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splitTopic.pop())
        if (endpointEntity != null) {
            val node = endpointEntity.endpoint.nodes[splitTopic.pop()]
            if (node != null) {
                return CloudioModelUtils.findObjectInNode(node, splitTopic)
            }
        }
        return null
    }

    fun getAttribute(endpointEntityRepository: EndpointEntityRepository, attributeRequest: AttributeRequest): Attribute? {
        val splitTopic = Stack<String>()
        splitTopic.addAll(attributeRequest.attributeTopic.split("/").toList().reversed())
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splitTopic.pop())
        if (endpointEntity != null) {
            val node = endpointEntity.endpoint.nodes[splitTopic.pop()]
            if (node != null) {
                return CloudioModelUtils.findAttributeInNode(node, splitTopic)
            }
        }
        return null
    }

    fun setAttribute(rabbitTemplate: RabbitTemplate, endpointEntityRepository: EndpointEntityRepository, attributeSetRequest: AttributeSetRequest): ApiActionAnswer{

        val attribute = getAttribute(endpointEntityRepository, AttributeRequest(attributeSetRequest.attributeTopic))

        //only set attribute if setpoint or parameter
        if (attribute == null)
            return ApiActionAnswer(false, "Attribute doesn't exist")
        else if(attribute.constraint != AttributeConstraint.SetPoint && attribute.constraint != AttributeConstraint.Parameter)
            return ApiActionAnswer(false, "Attribute is nor a SetPoint, neither a Parameter")
        else {
            //send message to amq.topic queue
            rabbitTemplate.convertSendAndReceive("amq.topic",
                    "@set." + attributeSetRequest.attributeTopic.replace("/", "."),
                    serializeAttribute(attributeSetRequest.attribute))

            return ApiActionAnswer(true, "")
        }
    }

    fun getOwnedEndpoints(userRepository: UserRepository, userGroupRepository: UserGroupRepository, endpointParametersRepository: EndpointParametersRepository, userName: String): OwnedEndpointsAnswer{
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val ownedEndpointsSet : MutableSet<String> = mutableSetOf()
        //search for permission looking like topic = endpointUuid/#, permission = OWN
        permissionMap.forEach { (topic, prioritizedPermission) ->
            val splitTopic = topic.split("/")
            if(splitTopic.size==2 && splitTopic.getOrNull(1).equals("#") && prioritizedPermission.permission == Permission.OWN){
                ownedEndpointsSet.add(splitTopic[0])
            }
        }
        val ownedEndpointParametersSet: MutableSet<EndpointParameters> = mutableSetOf()
        //search for friendly name of each owned endpoint
        ownedEndpointsSet.forEach { s ->
            val endpointParameter = endpointParametersRepository.findByIdOrNull(s)
                    ?: EndpointParameters(s, "Endpoint not in the database")
            ownedEndpointParametersSet.add(endpointParameter)
        }
        return OwnedEndpointsAnswer(ownedEndpointParametersSet)
    }

    fun getAccessibleAttributes(userRepository: UserRepository, userGroupRepository: UserGroupRepository, endpointEntityRepository: EndpointEntityRepository, userName: String): AccessibleAttributesAnswer{
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val endpointsSet : MutableSet<String> = mutableSetOf()
        //get every endpointUuid found in the permission map of user
        permissionMap.forEach { (topic, _) ->
            endpointsSet.add(topic.split("/")[0])
        }

        val toReturn : MutableMap<String, Permission> = mutableMapOf()
        //will search for every attributes in the endpointUuid set
        endpointsSet.forEach { endpointUuid ->
            val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointUuid)
            if (endpointEntity != null)
                toReturn.putAll(PermissionUtils.getAccessibleAttributesFromEndpoint(permissionMap, endpointEntity))
        }
        return AccessibleAttributesAnswer(toReturn)
    }

}

