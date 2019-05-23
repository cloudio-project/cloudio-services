package ch.hevs.cloudio2.cloud

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service


@Service
class UpdateService {

    companion object {
        private val log = LoggerFactory.getLogger(UpdateService::class.java)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "updateTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@update.#"])])
    fun handleOnlineMessage(message: Message)
    {
        log.info("@update.#")
    }
}