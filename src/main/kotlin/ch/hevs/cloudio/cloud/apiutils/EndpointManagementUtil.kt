package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat.serializeAttribute
import ch.hevs.cloudio.cloud.utils.CloudioModelUtils
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import java.nio.charset.Charset
import java.util.*


object  EndpointManagementUtil{

    fun createEndpoint(endpointParametersRepository: EndpointParametersRepository, endpointCreateRequest: EndpointCreateRequest): EndpointParameters {
        val toReturn = EndpointParameters(UUID.randomUUID().toString(), endpointCreateRequest.endpointFriendlyName)

        endpointParametersRepository.save(toReturn)

        return toReturn
    }

    fun getEndpoint(endpointEntityRepository: EndpointEntityRepository, endpointRequest: EndpointRequest): EndpointEntity? {
        println((endpointRequest.endpointUuid))
        return endpointEntityRepository.findByIdOrNull(endpointRequest.endpointUuid)
    }

    fun getNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest): Node? {
        val splittedTopic = nodeRequest.nodeTopic.split("/")
        return endpointEntityRepository.findByIdOrNull(splittedTopic[0])?.endpoint?.nodes?.get(splittedTopic[1])
    }

    fun getWotNode(endpointEntityRepository: EndpointEntityRepository, nodeRequest: NodeRequest){

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

    abstract class TopicChangeNotifier(connectionFactory: ConnectionFactory, topic: String) {

        companion object {
            private val log = LogFactory.getLog(TopicChangeNotifier::class.java)
        }

        init {
            //create a new queue with parameter topic and bind it to default amq.topic exchange
            val connection = connectionFactory.newConnection()
            val channel = connection.createChannel()
            val queueName = channel.queueDeclare().getQueue()
            channel.queueBind(queueName, "amq.topic", topic)

            //create a callback or the queue
            val deliverCallback = DeliverCallback { _, delivery ->
                val message = String(delivery.body, Charset.defaultCharset())
                val messageFormat = JsonSerializationFormat.detect(message.toByteArray())
                if (messageFormat) {
                    val attribute = Attribute()
                    JsonSerializationFormat.deserializeAttribute(attribute, message.toByteArray())
                    if (attribute.timestamp != -1.0 && attribute.value != null) {
                        notifyAttributeChange(attribute)
                        channel.queueDelete(queueName)
                    }
                } else {
                    log.error("Unrecognized message format in @set message from "+topic)
                }
            }
            channel.basicConsume(queueName, true, deliverCallback, CancelCallback{})
        }

        open fun notifyAttributeChange(attribute: Attribute)
        {
            log.error("function not overridden")
        }

    }
}

