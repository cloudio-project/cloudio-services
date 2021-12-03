package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.JobExecCommand
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.DeliverCallback
import io.swagger.annotations.Api
import org.apache.juli.logging.LogFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.scheduling.TaskScheduler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.time.Instant
import java.util.*

@CrossOrigin(origins = ["*"])
@RestController
@Profile("rest-api")
@Api(
    tags = ["Endpoint remote job execution"],
    description = "Run job remotely on endpoints."
)
@RequestMapping("/api/v1/endpoints")
class EndpointJobsController(
        private val endpointRepository: EndpointRepository,
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate,
        private val connectionFactory: ConnectionFactory,
        private val taskScheduler: TaskScheduler
) {
    private val log = LogFactory.getLog(EndpointJobsController::class.java)

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
        val jobParameter = JobExecCommand(
                jobURI = body.jobURI,
                sendOutput = body.enableOutput,
                correlationID = body.correlationID,
                data = body.arguments
        )
        if (body.enableOutput) {
            val emitter = SseEmitter()
            connectionFactory.createConnection().createChannel(false).apply {
                queueDeclare().let {
                    log.error(it.queue)
                    queueBind(it.queue, "amq.topic", "@execOutput.$uuid.${body.correlationID}")
                    val tag = basicConsume(it.queue, true,
                            DeliverCallback { _, message ->
                                serializationFormats.detect(message.body)?.run {
                                    val output = deserializeJobExecOutput(message.body)
                                    emitter.send(SseEmitter.event().data(output.output).build())
                                }
                            },
                            CancelCallback {
                                emitter.complete()
                            }
                    )
                    rabbitTemplate.convertAndSend("amq.topic", "@exec.$uuid", serializationFormat.serializeJobExecCommand(jobParameter))
                    taskScheduler.schedule({
                        try {
                            basicCancel(tag)
                        } catch (exception: IOException) {}

                    }, Instant.now().plusMillis(body.timeout))
                }
            }

            return emitter
        } else {
            rabbitTemplate.convertAndSend("amq.topic", "@exec.$uuid", serializationFormat.serializeJobExecCommand(jobParameter))
            return null
        }
    }
}
