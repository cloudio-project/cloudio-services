package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile("set-version-mapper", "default")
class SetVersionMapperService(val endpointEntityRepository: EndpointEntityRepository, val rabbitTemplate: RabbitTemplate) {
    private val log = LogFactory.getLog(SetVersionMapperService::class.java)

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@set.#"]
        )
    ])
    fun handleSetMessage(message: Message) {
        try {
            val splitTopic = message.messageProperties.receivedRoutingKey.split(".")

            val uuid = UUID.fromString(splitTopic[1])

            val endpointEntity = endpointEntityRepository.findByIdOrNull(uuid)

            if (endpointEntity != null) {
                if (endpointEntity.endpoint.version == "v0.1" && splitTopic[2] != "node") {
                    var topic = ""
                    splitTopic.forEachIndexed { i, topicPart ->
                        topic += when (i) {
                            0, 1 -> ("$topicPart.")                        //@set and uudi
                            2 -> ("node.$topicPart.")                      // add "node." before node name
                            splitTopic.size - 1 -> ("attribute.$topicPart")// add "attribute." before node attribute
                            else -> ("object.$topicPart.")                 // add "object." before node object
                        }
                    }
                    rabbitTemplate.convertAndSend("amq.topic", topic, message)
                }
            }
        } catch (exception: Exception) {

            log.error("Exception during @set message translation from v0.2 to v0.1:", exception)
        }
    }
}