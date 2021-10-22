package ch.hevs.cloudio.cloud.restapi.endpoint.event

import ch.hevs.cloudio.cloud.abstractservices.AbstractUpdateSetService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.serialization.JSONSerializationFormat
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Api(tags = ["Endpoint Model Access"], description = "Allows an user to subscribe to events using SSE.")
@RequestMapping("/api/v1/event")
class EndpointEventAccessController(
        private val endpointRepository: EndpointRepository,
        serializationFormats: Collection<SerializationFormat>,
        private val permissionManager: CloudioPermissionManager) : AbstractUpdateSetService(serializationFormats)  {

    private var sses = ConcurrentHashMap<ModelIdentifier, ArrayList<SseEmitter>>()
    private val antMatcher = AntPathMatcher()

    @ApiOperation("Subscribe to endpoint's attribute.")
    @GetMapping("/**")
    @ResponseStatus(HttpStatus.OK)
    fun createSSE(
        @ApiIgnore authentication: Authentication,
        @ApiIgnore request: HttpServletRequest): Any {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/event/**", request.requestURI))
        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }

        // Check if the endpoint exists.
        if (!endpointRepository.existsById(modelIdentifier.endpoint)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        //  Check if user has access to the attribute.
        if (!permissionManager.hasEndpointModelElementPermission(authentication.userDetails(), modelIdentifier, EndpointModelElementPermission.READ)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        //TODO use RabbitListener

        var sse = SseEmitter(60 * 1000L)

        synchronized(sses) {
            sses.getOrPut(modelIdentifier, { java.util.ArrayList<SseEmitter>() }).add(sse)
        }

        return sse
    }

    override fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String) {
        val modelIdentifier = ModelIdentifier(attributeId)
        val serializer = JSONSerializationFormat()

        synchronized(sses) {
            if (sses.contains(modelIdentifier)) {
                sses[modelIdentifier]?.forEach {
                    it.send(serializer.serializeAttribute(attribute))
                }
            }
        }
    }
}