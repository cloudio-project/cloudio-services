package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import java.nio.charset.Charset

abstract class AttributeChangeNotifier(connectionFactory: ConnectionFactory, topic: String) {

    companion object {
        private val log = LogFactory.getLog(AttributeChangeNotifier::class.java)
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
                log.error("Unrecognized message format in $topic message")
            }
        }
        channel.basicConsume(queueName, true, deliverCallback, CancelCallback{})
    }

    open fun notifyAttributeChange(attribute: Attribute)
    {
        log.error("function not overridden")
    }

}