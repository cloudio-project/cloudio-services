package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.util.*

object JobsUtil {

    fun executeJob(rabbitTemplate: RabbitTemplate, jobExecuteRequest: JobExecuteRequest,
                   endpointEntityRepository: EndpointEntityRepository, serializationFormats: Collection<SerializationFormat>) {
        val jobsParameter = JobParameter(jobExecuteRequest.jobURI, jobExecuteRequest.correlationID, jobExecuteRequest.data, jobExecuteRequest.getOutput)

        val endpointUUID = jobExecuteRequest.endpointUuid
        val endpoint = endpointEntityRepository.findById(UUID.fromString(endpointUUID)).get()


        val serializationFormat = serializationFormats.fromIdentifiers(endpoint.endpoint.supportedFormats)
                ?: throw CloudioHttpExceptions.InternalServerError("Endpoint does not support any serialization format.")

        rabbitTemplate.convertAndSend("amq.topic",
                "@exec." + jobExecuteRequest.endpointUuid,
                serializationFormat.serializeJobParameter(jobsParameter))

    }
}