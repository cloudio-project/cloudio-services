package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("set-version-mapper", "default")
class SetVersionMapperService(
        private val endpointRepository: EndpointRepository,
        private val rabbitTemplate: RabbitTemplate
): AbstractTopicService("${ActionIdentifier.ATTRIBUTE_SET}.#") {
    private val log = LogFactory.getLog(SetVersionMapperService::class.java)

    override fun handleMessage(message: Message) {
        try {
            val topic = message.messageProperties.receivedRoutingKey
            val modelIdentifier = ModelIdentifier(topic)
            if (modelIdentifier.valid) {
                endpointRepository.findById(modelIdentifier.endpoint).ifPresent {
                    //if endpoint v1 and message topic is v2
                    if (it.dataModel.messageFormatVersion == 1 && !(topic.contains("nodes")
                                    && topic.contains("objects") && topic.contains("attributes"))) {
                        rabbitTemplate.convertAndSend("amq.topic", modelIdentifier.toAMQPTopicForMessageFormat1Endpoints(), message)
                    }
                }
            }
        } catch (exception: Exception) {
            log.error("Exception during @set message translation from v0.2 to v0.1:", exception)
        }
    }
}
