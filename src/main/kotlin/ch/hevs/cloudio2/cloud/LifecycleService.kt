package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.model.Endpoint
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.repo.EndpointEntity
import ch.hevs.cloudio2.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio2.cloud.serialization.SerializationFormatFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.core.env.Environment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class LifecycleService(val env: Environment, val influx: InfluxDB, val endpointEntityRepository: EndpointEntityRepository){

    companion object {
        private val log = LoggerFactory.getLogger(LifecycleService::class.java)
    }
    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @PostConstruct
    fun initialize()
    {
        // for testing only
        if(!influx.databaseExists(database))
            influx.createDatabase(database)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "onlineTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@online.*"])])
    fun handleOnlineMessage(message: Message)
    {
        log.info("@online.*")

        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = SerializationFormatFactory.serializationFormat(data)
            if (messageFormat != null) {
                val endpoint = Endpoint()
                messageFormat.deserializeEndpoint(endpoint, data)
                endpointIsOnlineMongo(endpointId, endpoint)
                endpointIsOnlineInflux(endpointId, endpoint)
            } else {
                log.error("Unrecognized message format in @online message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @online message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "offlineTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@offline.*"])])
    fun handleOfflineMessage(message: Message)
    {
        log.info("@offline.*")

        try {
            endpointIsOfflineMongo(message.messageProperties.receivedRoutingKey.split(".")[1])
            endpointIsOfflineInflux(message.messageProperties.receivedRoutingKey.split(".")[1])
        } catch (exception: Exception) {
            log.error("Exception during @offline message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "nodeAddedTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@nodeAdded.*.nodes.*"])])
    fun handleNodeAddedMessage(message: Message)
    {
        log.info("@nodeAdded.*.nodes.*")
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val nodeName = message.messageProperties.receivedRoutingKey.split(".")[3]
            val data = message.body
            val messageFormat = SerializationFormatFactory.serializationFormat(data)
            if (messageFormat != null) {
                val node = Node()
                messageFormat.deserializeNode(node, data)
                nodeAddedMongo(endpointId, nodeName, node)
                nodeAddedInflux(endpointId, nodeName, node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "nodeRemovedTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@nodeRemoved.*.nodes.*"])])
    fun handleNodeRemovedMessage(message: Message)
    {
        log.info("@nodeRemoved.*.nodes.*")

        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val nodeName = message.messageProperties.receivedRoutingKey.split(".")[3]
            nodeRemovedMongo(endpointId, nodeName)
            nodeRemovedInflux(endpointId, nodeName)
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    fun endpointIsOnlineMongo(endpointId: String, endpoint: Endpoint) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId) ?: EndpointEntity(endpointId)
        endpointEntity.online = true
        endpointEntity.endpoint = endpoint
        endpointEntityRepository.save(endpointEntity)
    }

    fun endpointIsOfflineMongo(endpointId: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.online = false
            endpointEntityRepository.save(endpointEntity)
        }
    }

    fun nodeAddedMongo(endpointId: String, nodeName: String, node: Node) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes[nodeName] = node
            endpointEntityRepository.save(endpointEntity)
        }
    }

    fun nodeRemovedMongo(endpointId: String, nodeName: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes.remove(nodeName)
            endpointEntityRepository.save(endpointEntity)
        }
    }

    fun endpointIsOfflineInflux(endpointId: String) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "offline")
                .build())
    }

    fun endpointIsOnlineInflux(endpointId: String, endpoint: Endpoint) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "online")
                .build())
    }

    fun nodeAddedInflux(endpointId: String, nodeName: String, node: Node) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "added")
                .build())
    }

    fun nodeRemovedInflux(endpointId: String, nodeName: String) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "removed")
                .build())
    }
}