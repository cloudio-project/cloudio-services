package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import io.swagger.annotations.Api
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/v1/endpoints")
@Api(
        tags = ["Endpoint remote job execution"],
        description = "Run job remotely on endpoints."
)
class EndpointJobsController(
        private val endpointRepository: EndpointRepository,
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate
) {
    @PostMapping("/{uuid}/exec")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    fun runJobOnEndpointByUUIDAndJobName(
            @PathVariable uuid: UUID,
            @RequestBody body: JobExecEntity
    ): SseEmitter? {
        val serializationFormat = serializationFormats.fromIdentifiers(endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.dataModel.supportedFormats)
                ?: throw CloudioHttpExceptions.InternalServerError("Endpoint does not support any serialization format.")
        val jobParameter = JobParameter(
                jobURI = body.jobURI,
                sendOutput = body.enableOutput,
                correlationID = body.correlationID,
                data = body.arguments
        )
        return if (body.enableOutput) {
            TODO("Implement with output - waiting for new feature on java endpoint")
            /*
            val emitter = SseEmitter()

                executor.execute {
                    try {
                        //create a listener for the correct execOutput topic
                        val execOutputNotifier = object : ExecOutputNotifier(connectionFactory, "@execOutput." + jobExecuteRequest.endpointUuid) {
                            override fun notifyExecOutput(jobsLineOutput: JobsLineOutput) {
                                if (jobsLineOutput.correlationID == jobExecuteRequest.correlationID)
                                //send the output as a Sse event
                                    emitter.send(SseEmitter.event().id(jobsLineOutput.correlationID).data(jobsLineOutput.data))
                            }
                        }
                        JobsUtil.executeJob(rabbitTemplate, jobExecuteRequest)
                        Thread.sleep(jobExecuteRequest.timeout)
                        emitter.complete()
                        execOutputNotifier.deleteQueue()

                    } catch (e: Exception) {
                        emitter.completeWithError(e)
                    }
                }
                return emitter
             */
        } else {
            rabbitTemplate.convertAndSend("amq.topic", "@exec.$uuid", serializationFormat.serializeJobParameter(jobParameter))
            null
        }
    }
}
