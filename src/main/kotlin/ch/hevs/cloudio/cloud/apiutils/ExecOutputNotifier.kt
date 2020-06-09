package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobsLineOutput
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory

abstract class ExecOutputNotifier(connectionFactory: ConnectionFactory, topic: String,
                                  serializationFormats: Collection<SerializationFormat>) {

    companion object {
        private val log = LogFactory.getLog(ExecOutputNotifier::class.java)
    }

    private val channel: Channel
    private val queueName: String

    init {
        //create a new queue with parameter topic and bind it to default amq.topic exchange
        val connection = connectionFactory.createConnection()
        channel = connection.createChannel(false)
        queueName = channel.queueDeclare().getQueue()
        channel.queueBind(queueName, "amq.topic", topic)

        //create a callback or the queue
        val deliverCallback = DeliverCallback { _, delivery ->

            val messageFormat = serializationFormats.detect(delivery.body)
            if (messageFormat != null) {
                val jobsLineOutput = JobsLineOutput()
                messageFormat.deserializeJobsLineOutput(jobsLineOutput, delivery.body)
                notifyExecOutput(jobsLineOutput)
            } else {
                log.error("Unrecognized message format in $topic message")
            }
        }
        channel.basicConsume(queueName, true, deliverCallback, CancelCallback {})
    }

    open fun notifyExecOutput(jobsLineOutput: JobsLineOutput) {
        log.error("function not overridden")
    }

    open fun deleteQueue() {
        channel.queueDelete(queueName)
    }

}
