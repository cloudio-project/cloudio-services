package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.logging.LogFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Profile("delayed", "default")
class DelayedService(
        private val influx: InfluxDB,
        private val serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties,
        private val rabbitTemplate: RabbitTemplate
) {
    private val log = LogFactory.getLog(DelayedService::class.java)
    private val mapper by lazy { Jackson2ObjectMapperBuilder.json().build<ObjectMapper>() }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@delayed.*"]
        )
    ])
    fun handleDelayedMessage(message: Message) {
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
        if (attribute.timestamp != null) {
            // create the influxDB point
            val point = Point
                    .measurement(modelIdentifier.toInfluxSeriesName())
                    .time((attribute.timestamp!! * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                    .tag("constraint", attribute.constraint.toString())
                    .tag("type", attribute.type.toString())

            try {
                // complete the point depending on attribute type
                when (attribute.type) {
                    AttributeType.Boolean -> point.addField("value", attribute.value as Boolean)
                    AttributeType.Integer -> point.addField("value", attribute.value as Int)
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
                //write the actual point in influx
                val myPoint = point.build()

                //if batch enabled, save point in set, either send it
                influx.write(influxProperties.database, "autogen", myPoint)

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
        influx.write(influxProperties.database, "autogen", Point
                .measurement("$endpointUuid.logs")
                .time((logMessage.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .addField("level", logMessage.level.ordinal)
                .addField("message", logMessage.message)
                .addField("loggerName", logMessage.loggerName)
                .addField("logSource", logMessage.logSource)
                .build())
    }
}
