package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("set-version-mapper", "default")
class SetVersionMapperService(
        private val endpointRepository: EndpointRepository,
        private val rabbitTemplate: RabbitTemplate
) {
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
            val modelIdentifier = ModelIdentifier(message.messageProperties.receivedRoutingKey)
            if (modelIdentifier.valid) {
                endpointRepository.findById(modelIdentifier.endpoint).ifPresent {
                    if (it.dataModel.version == "v0.1") {
                        rabbitTemplate.convertAndSend("amq.topic", modelIdentifier.toAMQPTopicForVersion01Endpoints(), message)
                    }
                }
            }
        } catch (exception: Exception) {
            log.error("Exception during @set message translation from v0.2 to v0.1:", exception)
        }
    }
}