package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat.serializeAttribute
import ch.hevs.cloudio.cloud.serialization.JsonWotSerializationFormat
import ch.hevs.cloudio.cloud.serialization.wot.WotNode
import ch.hevs.cloudio.cloud.utils.CloudioModelUtils
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
}

