package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.extension.fillFromInfluxDB
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.fromIdentifiers
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.influxdb.InfluxDB
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import springfox.documentation.annotations.ApiIgnore
import javax.servlet.http.HttpServletRequest

@RestController
@Profile("rest-api")
@Api(tags = ["Endpoint Model Access"], description = "Allows an user to access data models of endpoints.")
@RequestMapping("/api/v1/data")
class EndpointDataAccessController(
        private val endpointRepository: EndpointRepository,
        private val permissionManager: CloudioPermissionManager,
        private val influxDB: InfluxDB,
        private val influxProperties: CloudioInfluxProperties,
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate,
        private val amqpAdmin: AmqpAdmin,
        private val connectionFactory: ConnectionFactory
) {
    private val antMatcher = AntPathMatcher()

    @ApiOperation("Read access to endpoint's data model.")
    @GetMapping("/**")
    @ResponseStatus(HttpStatus.OK)
    fun getModelElement(
            @ApiIgnore authentication: Authentication,
            @ApiIgnore request: HttpServletRequest): Any {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/data/**", request.requestURI))
        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }

        // Check if the endpoint exists.
        if (!endpointRepository.existsById(modelIdentifier.endpoint)) {
            throw CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // Resolve the access level the user has to the endpoint and fail if the user has no access to the endpoint.
        val endpointPermission = permissionManager.resolveEndpointPermission(authentication.userDetails(), modelIdentifier.endpoint)
        if (!endpointPermission.fulfills(EndpointPermission.ACCESS)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        // Retrieve endpoint from repository.
        val endpoint = endpointRepository.findById(modelIdentifier.endpoint).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // If the model path is empty, we return the whole data model of the endpoint.
        val data = modelIdentifier.resolve(endpoint.dataModel).orElseThrow {
            CloudioHttpExceptions.NotFound("Model element not found.")
        }

        // If the user only has partial access to the endpoint's model, filter the data model accordingly.
        if (!endpointPermission.fulfills(EndpointPermission.ACCESS)) {
            // TODO: Filter endpoint data based on model element permissions.
        }

        // Fill data from influxDB.
        when (data) {
            is EndpointDataModel -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, endpoint.uuid)
            is Node -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
            is CloudioObject -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
            is Attribute -> data.fillFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
        }

        // TODO: We should create an entity class to return.
        return data
    }

    @ApiOperation("Write access to all endpoint's data model.")
    @PutMapping("/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putAttribute(
            @RequestParam @ApiParam("Value to set.") value: String,
            @ApiIgnore authentication: Authentication,
            @ApiIgnore request: HttpServletRequest) {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/data/**", request.requestURI))

        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }
        modelIdentifier.action = ActionIdentifier.ATTRIBUTE_SET

        // Resolve the access level the user has to the element.
        if (!permissionManager.hasEndpointModelElementPermission(authentication.userDetails(), modelIdentifier, EndpointModelElementPermission.WRITE)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        // Retrieve endpoint from repository.
        val endpoint = endpointRepository.findById(modelIdentifier.endpoint).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // If the model path is empty, we return the whole data model of the endpoint.
        val data = modelIdentifier.resolve(endpoint.dataModel).orElseThrow {
            CloudioHttpExceptions.NotFound("Model element not found.")
        }

        // Ensure that it is an attribute and send the value using AMQP.
        when (data) {
            is Attribute -> if (data.constraint != AttributeConstraint.SetPoint && data.constraint != AttributeConstraint.Parameter)
                throw CloudioHttpExceptions.BadRequest("Attribute is not a SetPoint, nor a Parameter.")
            else {
                // Convert value to target datatype.
                val typedValue: Any = when (data.type) {
                    AttributeType.Invalid -> throw CloudioHttpExceptions.InternalServerError("Invalid datatype.")
                    AttributeType.Boolean -> value.toBoolean()
                    AttributeType.Integer -> value.toLong()
                    AttributeType.Number -> value.toDouble()
                    AttributeType.String -> value
                }

                // Serialize and send the message.
                val serializationFormat = serializationFormats.fromIdentifiers(endpoint.dataModel.supportedFormats)
                        ?: throw CloudioHttpExceptions.InternalServerError("Endpoint does not support any serialization format.")
                rabbitTemplate.convertAndSend("amq.topic",
                        if (endpoint.dataModel.messageFormatVersion != 1) modelIdentifier.toAMQPTopic() else modelIdentifier.toAMQPTopicForMessageFormat1Endpoints(),
                        serializationFormat.serializeAttribute(Attribute(
                                data.constraint,
                                data.type,
                                System.currentTimeMillis().toDouble() / 1000,
                                typedValue)))
            }
            else -> throw CloudioHttpExceptions.BadRequest("Only Attributes can be modified.")
        }
    }

    @ApiOperation("Subscribe to changes of multiple attributes.")
    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.OK)
    fun subscribe(
        @RequestBody @ApiParam("List of attributes to subscribe to.", required = true) ids: Array<String>,
        @RequestParam(required = false, defaultValue = "300000") @ApiParam("Optional timeout in  milliseconds.", required = false) timeout: Long
    ): SseEmitter {
        return Subscription(ids.map {
            val id = ModelIdentifier(it)
            if (!id.valid) {
                throw CloudioHttpExceptions.BadRequest("Invalid id")
            }
            id.action = ActionIdentifier.ATTRIBUTE_UPDATE
            // TODO: Check access rights.
            id
        }, timeout, serializationFormats, amqpAdmin, connectionFactory)
    }
}
