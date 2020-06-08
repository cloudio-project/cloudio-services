package ch.hevs.cloudio.cloud.restapi.endpoint

import ch.hevs.cloudio.cloud.apiutils.CloudioApiException
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
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import javax.servlet.http.HttpServletRequest

@Api(tags = ["Data Model Access"], description = "Allows an user to access data models of his endpoints.")
@RestController
@RequestMapping("/api/v1/access")
class EndpointAccessController(private val endpointRepository: EndpointRepository,
                               private val permissionManager: CloudioPermissionManager,
                               private val influxDB: InfluxDB,
                               private val influxProperties: CloudioInfluxProperties,
                               private val serializationFormats: Collection<SerializationFormat>,
                               private val rabbitTemplate: RabbitTemplate) {
    private val antMatcher = AntPathMatcher()

    @ApiOperation("Read access to the endpoint's data model.")
    @GetMapping("/**")
    @ResponseStatus(HttpStatus.OK)
    fun getModelElement(
            @ApiIgnore authentication: Authentication,
            @ApiIgnore request: HttpServletRequest): Any {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/access/**", request.requestURI))
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
        val endpoint = endpointRepository.findById(modelIdentifier.endpoint).orElseThrow{
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
        when(data) {
            is Endpoint -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, endpoint.uuid)
            is Node -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
            is CloudioObject -> data.fillAttributesFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
            is Attribute -> data.fillFromInfluxDB(influxDB, influxProperties.database, modelIdentifier.toInfluxSeriesName())
        }

        return data
    }

    @ApiOperation("Write access to the endpoint's data model.")
    @PutMapping("/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putAttribute(
            @RequestParam @ApiParam() value: String,
            @ApiIgnore authentication: Authentication,
            @ApiIgnore request: HttpServletRequest) {

        // Extract model identifier and check it for validity.
        val modelIdentifier = ModelIdentifier(antMatcher.extractPathWithinPattern("/api/v1/access/**", request.requestURI))

        if (!modelIdentifier.valid || modelIdentifier.action != ActionIdentifier.NONE) {
            throw CloudioHttpExceptions.BadRequest("Invalid model identifier.")
        }
        modelIdentifier.action = ActionIdentifier.ATTRIBUTE_SET

        // Resolve the access level the user has to the element.
        if (!permissionManager.hasEndpointModelElementPermission(authentication.userDetails(), modelIdentifier, EndpointModelElementPermission.WRITE)) {
            throw CloudioHttpExceptions.Forbidden("Forbidden.")
        }

        // Retrieve endpoint from repository.
        val endpoint = endpointRepository.findById(modelIdentifier.endpoint).orElseThrow{
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // If the model path is empty, we return the whole data model of the endpoint.
        val data = modelIdentifier.resolve(endpoint.dataModel).orElseThrow {
            CloudioHttpExceptions.NotFound("Model element not found.")
        }

        // Ensure that it is an attribute and send the value using AMQP.
        when(data) {
            is Attribute -> if (data.constraint != AttributeConstraint.SetPoint && data.constraint != AttributeConstraint.Parameter)
                throw CloudioApiException("Attribute is not a SetPoint, nor a Parameter.")
            else {
                // Convert value to target datatype.
                val typedValue = when(data.type) {
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
                        modelIdentifier.toAmqpTopic(),
                        serializationFormat.serializeAttribute(Attribute(
                                data.constraint,
                                data.type,
                                System.currentTimeMillis().toDouble() / 1000,
                                typedValue)))
            }
            else -> throw CloudioHttpExceptions.BadRequest("Only Attributes can be modified.")
        }
    }
}
