package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("execJobs", "default")
class ExecJobsService{

    companion object {
        private val log = LogFactory.getLog(AbstractLifecycleService::class.java)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@exec.*"])])
    fun handleExecMessage(message: Message)
    {
        log.info("@exec.*")

        try {
            message.messageProperties.receivedRoutingKey.split(".")
        } catch (exception: Exception) {
            log.error("Exception during @exec message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@execOutput.*"])])
    fun handleExecOutputMessage(message: Message)
    {
        log.info("@execOutput.*")

        try {
            message.messageProperties.receivedRoutingKey
        } catch (exception: Exception) {
            log.error("Exception during @execOutput message handling:", exception)
        }
    }
}
