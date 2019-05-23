package ch.hevs.cloudio2.cloud

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeBuilder
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class LifecycleService {

    companion object {
        private val log = LoggerFactory.getLogger(LifecycleService::class.java)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "onlineTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@online.*"])])
    fun handleOnlineMessage(message: Message)
    {
        log.info("@online.*")
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "offlineTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@offline.*"])])
    fun handleOfflineMessage(message: Message)
    {
        log.info("@offline.*")
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "nodeAddedTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@nodeAdded.*.nodes.*"])])
    fun handleNodeAddedMessage(message: Message)
    {
        log.info("@nodeAdded.*.nodes.*")
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(value = "nodeRemovedTopic"),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@nodeRemoved.*.nodes.*"])])
    fun handleNodeRemovedMessage(message: Message)
    {
        log.info("@nodeRemoved.*.nodes.*")
    }
}