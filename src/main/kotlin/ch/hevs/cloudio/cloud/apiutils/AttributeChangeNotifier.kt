package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory

abstract class AttributeChangeNotifier(connectionFactory: ConnectionFactory, topic: String,
                                       serializationFormats: Collection<SerializationFormat>) {

    companion object {
        private val log = LogFactory.getLog(AttributeChangeNotifier::class.java)
    }

    init {
        //create a new queue with parameter topic and bind it to default amq.topic exchange
        val connection = connectionFactory.createConnection()
        val channel = connection.createChannel(false)
        val queueName = channel.queueDeclare().getQueue()
        channel.queueBind(queueName, "amq.topic", topic)

        //create a callback or the queue
        val deliverCallback = DeliverCallback { _, delivery ->

            val messageFormat = serializationFormats.detect(delivery.body)
            if (messageFormat != null) {
                val attribute = Attribute()
                messageFormat.deserializeAttribute(attribute, delivery.body)
                if (attribute.timestamp != -1.0 && attribute.value != null) {
                    notifyAttributeChange(attribute)
                    channel.queueDelete(queueName)
                }
            } else {
                log.error("Unrecognized message format in $topic message")
            }
        }
        channel.basicConsume(queueName, true, deliverCallback, CancelCallback {})
    }

    open fun notifyAttributeChange(attribute: Attribute) {
        log.error("function not overridden")
    }

}
