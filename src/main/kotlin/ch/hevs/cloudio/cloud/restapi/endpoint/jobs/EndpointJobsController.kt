package ch.hevs.cloudio.cloud.restapi.endpoint.jobs

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.JobExecCommand
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.juli.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint remote job execution", description = "Run job (command or script) remotely on endpoints.")
@RequestMapping("/api/v1/endpoints")
class EndpointJobsController(
        private val endpointRepository: EndpointRepository,
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate
) {
    private val log = LogFactory.getLog(EndpointJobsController::class.java)

    @PostMapping("/{uuid}/exec", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Runs the given command or script on an endpoint.")
    @ApiResponses(value = [
        ApiResponse(description = "Job was scheduled for execution. Note that this does not mean that the job exist on the endpoint and that the job execution started.", responseCode = "204"),
        ApiResponse(description = "Endpoint with the given UUID does not exist.", responseCode = "404"),
        ApiResponse(description = "Endpoint job serialization error.", responseCode = "500"),
        ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
    ])
    fun runJobOnEndpointByUUIDAndJobName(
            @PathVariable @Parameter(description = "UUID of the endpoint to run the command on.") uuid: UUID,
            @RequestBody body: JobExecEntity
    ) {
        val serializationFormat = serializationFormats.fromIdentifiers(endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }.dataModel.supportedFormats)
                ?: throw CloudioHttpExceptions.InternalServerError("Endpoint does not support any serialization format.")
        val jobParameter = JobExecCommand(
                jobURI = body.jobURI,
                sendOutput = body.enableOutput ?: false,
                correlationID = body.correlationID,
                data = body.arguments ?: ""
        )

        rabbitTemplate.convertAndSend("amq.topic", "@exec.$uuid", serializationFormat.serializeJobExecCommand(jobParameter))
    }
}
