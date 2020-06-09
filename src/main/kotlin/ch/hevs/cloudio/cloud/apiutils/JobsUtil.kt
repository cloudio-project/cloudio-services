package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.serialization.CBORSerializationFormat
import ch.hevs.cloudio.cloud.serialization.JSONSerializationFormat
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.util.*

object JobsUtil {

    fun executeJob(rabbitTemplate: RabbitTemplate, jobExecuteRequest: JobExecuteRequest, endpointEntityRepository: EndpointEntityRepository) {
        val jobsParameter = JobParameter(jobExecuteRequest.jobURI, jobExecuteRequest.correlationID, jobExecuteRequest.data, jobExecuteRequest.getOutput)

        val endpointUUID = jobExecuteRequest.endpointUuid
        val endpoint = endpointEntityRepository.findById(UUID.fromString(endpointUUID)).get()
        val serializedJob =
                if(endpoint.endpoint.supportedFormats.contains("JSON")){
                    JSONSerializationFormat().serializeJobParameter(jobsParameter)
                }else if(endpoint.endpoint.supportedFormats.contains("CBOR")){
                    CBORSerializationFormat().serializeJobParameter(jobsParameter)
                }else{//default
                    JSONSerializationFormat().serializeJobParameter(jobsParameter)
                }
        rabbitTemplate.convertAndSend("amq.topic",
                "@exec." + jobExecuteRequest.endpointUuid, serializedJob)

    }
}