package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobsLineOutput
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import org.apache.commons.logging.LogFactory
import java.nio.charset.Charset

abstract class ExecOutputNotifier(connectionFactory: ConnectionFactory, topic: String) {

    companion object {
        private val log = LogFactory.getLog(AttributeChangeNotifier::class.java)
    }

    private val channel: Channel
    private val queueName: String

    init {
        //create a new queue with parameter topic and bind it to default amq.topic exchange
        val connection = connectionFactory.newConnection()
        channel = connection.createChannel()
        queueName = channel.queueDeclare().getQueue()
        channel.queueBind(queueName, "amq.topic", topic)

        //create a callback or the queue
        val deliverCallback = DeliverCallback { _, delivery ->
            val message = String(delivery.body, Charset.defaultCharset())
            val messageFormat = JsonSerializationFormat.detect(message.toByteArray())
            if (messageFormat) {
                val jobsLineOutput = JobsLineOutput()
                JsonSerializationFormat.deserializeJobsLineOutput(jobsLineOutput, message.toByteArray())
                notifyExecOutput(jobsLineOutput)
            } else {
                log.error("Unrecognized message format in $topic message")
            }
        }
        channel.basicConsume(queueName, true, deliverCallback, CancelCallback{})
    }

    open fun notifyExecOutput(jobsLineOutput: JobsLineOutput){
        log.error("function not overridden")
    }

    open fun deleteQueue(){
        channel.queueDelete(queueName)
    }

}