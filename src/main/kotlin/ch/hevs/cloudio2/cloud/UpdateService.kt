package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.model.AttributeType
import ch.hevs.cloudio2.cloud.model.CloudioObject
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.repo.EndpointEntity
import ch.hevs.cloudio2.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio2.cloud.serialization.SerializationFormatFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.core.env.Environment;
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@Service
class UpdateService(val env: Environment, val influx: InfluxDB, val endpointEntityRepository: EndpointEntityRepository) {

    companion object {
        private val log = LoggerFactory.getLogger(UpdateService::class.java)
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

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "updateTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@update.#"])])
    fun handleUpdateMessage(message: Message)
    {

        val attributeId = message.messageProperties.receivedRoutingKey.removePrefix("@update.")

        log.info("@update.#")

        val data = message.body
        val messageFormat = SerializationFormatFactory.serializationFormat(data)
        if (messageFormat != null) {
            val attribute = Attribute()
            messageFormat.deserializeAttribute(attribute, data)
            if (attribute.timestamp != -1.0 && attribute.value != null) {
                attributeUpdatedInflux(attributeId, attribute)
                attributeUpdatedMongo(attributeId, attribute)

            }
        } else {
            log.error("Unrecognized message format in @update message from $attributeId")
        }
    }

    private fun attributeUpdatedInflux(attributeId: String, attribute: Attribute)
    {
        // create the influxDB point
        val point = Point
                .measurement(attributeId)
                .time((attribute.timestamp *(1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .tag("constraint", attribute.constraint.toString())
                .tag("type", attribute.type.toString())

        // complete the point depending on attribute type
        when (attribute.type) {
            AttributeType.Boolean -> point.addField("value", attribute.value as Boolean)
            AttributeType.Integer, AttributeType.Number -> point.addField("value", attribute.value as Number)
            AttributeType.String -> point.addField("value", attribute.value as String)
            else -> {}
        }

        //write the actual point in influx
        val myPoint =  point.build()
        log.info(myPoint.toString())
        influx.write(database, "autogen",myPoint)
        log.info("Endpoint updated Influx")
    }

    private fun attributeUpdatedMongo(attributeId: String, attribute: Attribute) {
        val path = Stack<String>()
        path.addAll(attributeId.split(".").toList().reversed())
        if (path.size >= 3) {

            val id = path.pop()
            val endpointEntity = endpointEntityRepository.findByIdOrNull(id)
            if (endpointEntity != null && path.pop() == "nodes") {
                log.info("1")
                val node = endpointEntity.endpoint.nodes[path.pop()]
                if (node != null) {
                    log.info("2")
                    val existingAttribute = findAttributeInNode(node, path)
                    if (existingAttribute != null) {
                        log.info("3")
                        existingAttribute.timestamp = attribute.timestamp
                        existingAttribute.constraint = attribute.constraint
                        existingAttribute.type = attribute.type
                        existingAttribute.value = attribute.value
                        endpointEntityRepository.save(endpointEntity)
                        log.info("Endpoint updated Mongo")
                    }
                }
            }
        }
    }

    private fun findAttributeInNode(node: Node, path: Stack<String>): Attribute? {
        if (path.size > 1 && path.pop() == "objects") {
            log.info("objects")
            val obj = node.objects[path.pop()]
            if (obj != null) {
                log.info("obj != null")
                return findAttributeInObject(obj, path)
            }
        }

        return null
    }

    private fun findAttributeInObject(obj: CloudioObject, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            when (path.pop()) {
                "objects" -> {
                    val childObj = obj.objects[path.pop()]
                    if (childObj != null) {
                        return findAttributeInObject(childObj, path)
                    } else {
                        return null
                    }
                }
                "attributes" -> return obj.attributes[path.pop()]
                else -> return null
            }
        } else {
            return null
        }
    }
}