package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.serialization.JSONSerializationFormat
import org.springframework.amqp.rabbit.core.RabbitTemplate

object JobsUtil {

    fun executeJob(rabbitTemplate: RabbitTemplate, jobExecuteRequest: JobExecuteRequest) {
        val jobsParameter = JobParameter(jobExecuteRequest.jobURI, jobExecuteRequest.correlationID, jobExecuteRequest.data, jobExecuteRequest.getOutput)

        // TODO: Detect actual serialization format from endpoint data model.
        rabbitTemplate.convertAndSend("amq.topic",
                "@exec." + jobExecuteRequest.endpointUuid, JSONSerializationFormat().serializeJobParameter(jobsParameter))

    }
}
