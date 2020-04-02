package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.logging.LogFactory
import org.influxdb.InfluxDB
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service

@Service
@Profile("delayed", "default")
class DelayedService(private val endpointEntityRepository: EndpointEntityRepository, private val influx: InfluxDB,
                     private val influxProperties: CloudioInfluxProperties) {

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
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {

                val delayed = Delayed()

                JsonSerializationFormat.deserializeDelayed(delayed, data)

                delayed.messages.forEach { delayedMessage ->

                    val splitMessage = delayedMessage.topic.split("/")
                    val prefix = splitMessage[0]
                    when(prefix){
                        "@update" -> {
                            val innerData = delayedMessage.data
                            if(innerData is LinkedHashMap<*, *>) {
                                val attribute = mapper.convertValue(innerData, Attribute::class.java)
                                handleUpdate(delayedMessage.topic, attribute)
                            }
                        }
                        "@transaction" -> {
                            val innerData = delayedMessage.data
                            if(innerData is LinkedHashMap<*, *>) {
                                val transaction = mapper.convertValue(innerData, Transaction::class.java)
                                handleTransaction(delayedMessage.topic, transaction)
                            }
                        }
                        "@nodeAdded" -> {
                            val innerData = delayedMessage.data
                            if(innerData is LinkedHashMap<*, *>) {
                                val node = mapper.convertValue(innerData, Node::class.java)
                                handleNodeAdded(delayedMessage.topic, node)
                            }
                        }
                        "@nodeRemoved" -> {
                            handleNodeRemoved(delayedMessage.topic)
                        }
                        "@logs" -> {
                            val innerData = delayedMessage.data
                            if(innerData is LinkedHashMap<*, *>) {
                                val log = mapper.convertValue(innerData, CloudioLogMessage::class.java)
                                handleLogs(delayedMessage.topic, log)
                            }
                        }
                        else -> {}
                    }
                }
            } else {
                log.error("Unrecognized message format in @delayed message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @delayed message handling:", exception)
        }
    }

    fun handleUpdate(topic: String, attribute: Attribute){

    }

    fun handleTransaction(topic: String, transaction: Transaction){

    }

    fun handleNodeAdded(topic: String, node: Node){

    }

    fun handleNodeRemoved(topic: String){

    }

    fun handleLogs(topic: String, log: CloudioLogMessage){

    }

}