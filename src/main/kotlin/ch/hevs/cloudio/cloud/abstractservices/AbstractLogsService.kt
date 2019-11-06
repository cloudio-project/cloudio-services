package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.CloudioLogMessage
import ch.hevs.cloudio.cloud.model.LogParameter
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractLogsService{

    companion object {
        private val log = LogFactory.getLog(AbstractSetService::class.java)
    }

    @RabbitListener(
            bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@logsLevel.#"])])
    fun handleLogLevelMessage(message: Message)
    {
        log.info("@logsLevel.#")

        val endpointUuid = message.messageProperties.receivedRoutingKey.removePrefix("@logsLevel.")

        val data = message.body
        val messageFormat = JsonSerializationFormat.detect(data)
        if (messageFormat) {
            val logParameter = LogParameter()
            JsonSerializationFormat.deserializeLogParameter(logParameter, data)
            logLevelChange(endpointUuid, logParameter)
        } else {
            log.error("Unrecognized message format in @logsLevel message from $endpointUuid")
        }
    }

    @RabbitListener(
            bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@logs.#"])])
    fun handleLogsMessage(message: Message)
    {
        log.info("@logs.#")

        val endpointUuid = message.messageProperties.receivedRoutingKey.removePrefix("@logs.")

        val data = message.body
        val messageFormat = JsonSerializationFormat.detect(data)
        if (messageFormat) {
            val cloudioLog = CloudioLogMessage()
            JsonSerializationFormat.deserializeCloudioLog(cloudioLog, data)
            if (cloudioLog.timestamp != -1.0)
                newLog(endpointUuid, cloudioLog)
        } else {
            log.error("Unrecognized message format in @logs message from $endpointUuid")
        }
    }

    //Abstract method to handle log level change messages
    abstract fun logLevelChange(endpointUuid: String, logParameter: LogParameter)
    //Abstract method to handle logs messages
    abstract fun newLog(endpointUuid: String, cloudioLogMessage: CloudioLogMessage)
}
