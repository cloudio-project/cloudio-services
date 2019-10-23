package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.springframework.amqp.rabbit.core.RabbitTemplate

object JobsUtil{

    fun executeJob(rabbitTemplate: RabbitTemplate, jobExecuteRequest: JobExecuteRequest){
        val jobsParameter = JobParameter(jobExecuteRequest.jobURI, jobExecuteRequest.getOutput)

        rabbitTemplate.convertAndSend("amq.topic",
                "@exec." + jobExecuteRequest.endpointUuid, JsonSerializationFormat.serializeJobParameter(jobsParameter))

    }
}