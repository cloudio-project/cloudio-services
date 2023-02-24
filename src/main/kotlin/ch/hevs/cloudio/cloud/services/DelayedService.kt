package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.WriteApiBlocking
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile("delayed", "default")
class DelayedService(
        private val influx: InfluxDBClient,
        private val serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties,
        private val rabbitTemplate: RabbitTemplate
): AbstractTopicService("${ActionIdentifier.DELAYED_MESSAGES}.*") {
    private val log = LogFactory.getLog(DelayedService::class.java)

    override fun handleMessage(message: Message) {
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {

                val delayed = messageFormat.deserializeDelayedMessages(data)

                delayed.messages.forEach { delayedMessage ->

                    val splitMessage = delayedMessage.topic.split("/")
                    val prefix = splitMessage[0]
                    when (prefix) {
                        "@update" -> {
                            (delayedMessage.data as? Attribute)?.let {
                                handleUpdate(delayedMessage.topic, it)
                            }
                        }
                        "@transaction" -> {
                            (delayedMessage.data as? Transaction)?.let {
                                handleTransaction(endpointId, it, messageFormat)
                            }
                        }
                        "@logs" -> {
                            (delayedMessage.data as? LogMessage)?.let {
                                handleLogMessage(endpointId, it)
                            }
                        }
                        else -> {
                            log.error("Unrecognized message with sub-topic ${delayedMessage.topic} " +
                                    "inside @delayed message from $endpointId")
                        }
                    }
                }
            } else {
                log.error("Unrecognized message format in @delayed message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @delayed message handling:", exception)
        }
    }

    fun handleUpdate(topic: String, attribute: Attribute) {
        val modelIdentifier = ModelIdentifier(topic);
        val writeApi: WriteApiBlocking = influx.writeApiBlocking

        if (attribute.timestamp != null) {
            // create the influxDB point
            //TODO update to influx 2.x
            val point = Point
                    .measurement(modelIdentifier.toInfluxSeriesName())
                    .time((attribute.timestamp!! * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                    .addTag("constraint", attribute.constraint.toString())
                    .addTag("type", attribute.type.toString())

            try {
                // complete the point depending on attribute type
                when (attribute.type) {
                    AttributeType.Boolean -> point.addField("value", attribute.value as Boolean)
                    AttributeType.Integer -> point.addField("value", attribute.value as Long)
                    AttributeType.Number -> {
                        if (attribute.value is Int) {
                            attribute.value = (attribute.value as Int).toFloat()
                        }
                        point.addField("value", attribute.value as Number)
                    }
                    AttributeType.String -> point.addField("value", attribute.value as String)
                    else -> {
                    }
                }

                //if batch enabled, save point in set, either send it
                writeApi.writePoint(influxProperties.database, influxProperties.organization, point)

            } catch (e: ClassCastException) {
                log.error("The attribute $modelIdentifier has been updated with wrong data type")
            }
        }
    }

    fun handleTransaction(endpointUuid: String, transaction: Transaction, messageFormat: SerializationFormat) {
        transaction.attributes.forEach { (topic, attribute) ->
            rabbitTemplate.convertAndSend("amq.topic",
                    "@update.${topic.replace("/", ".")}",
                    messageFormat.serializeAttribute(attribute))
        }
    }

    fun handleLogMessage(endpointUuid: String, logMessage: LogMessage) {
        //TODO update to influx 2.x
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(influxProperties.database, influxProperties.organization, Point
                .measurement("$endpointUuid.logs")
                .time((logMessage.timestamp * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                .addField("level", logMessage.level.ordinal)
                .addField("message", logMessage.message)
                .addField("loggerName", logMessage.loggerName)
                .addField("logSource", logMessage.logSource))
    }
}
