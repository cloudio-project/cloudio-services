package ch.hevs.cloudio.cloud

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import java.nio.charset.Charset

abstract class MqttNotifier(connectionFactory: ConnectionFactory, topic: String) {

    companion object {
        private val log = LogFactory.getLog(MqttNotifier::class.java)
    }

    init {
        //create a new queue with parameter topic and bind it to default amq.topic exchange
        val connection = connectionFactory.createConnection()
        val channel = connection.createChannel(false)
        val queueName = channel.queueDeclare().getQueue()
        channel.queueBind(queueName, "amq.topic", topic)

        //create a callback or the queue
        val deliverCallback = DeliverCallback { _, delivery ->
            val message = String(delivery.body, Charset.defaultCharset())

            notifyMqttMessage(message)
            channel.queueDelete(queueName)

        }
        channel.basicConsume(queueName, true, deliverCallback, CancelCallback {})
    }

    open fun notifyMqttMessage(message: String) {
        log.error("function not overridden")
    }

}