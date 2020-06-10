package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.repo.MONOGOEndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.serialization.wot.NodeThingDescription
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.context.request.async.DeferredResult
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest

//@RestController
//@RequestMapping("/api/v1")
class EndpointManagementController_(var connectionFactory: ConnectionFactory, var influx: InfluxDB, var userGroupRepository: MONGOUserGroupRepository, var userRepository: MONGOUserRepository, var endpointEntityRepository: MONOGOEndpointEntityRepository, val influxProperties: CloudioInfluxProperties) {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/notifyAttributeChange", method = [RequestMethod.POST])
    fun notifyAttributeChange(@RequestBody attributeRequestLongpoll: AttributeRequestLongpoll): DeferredResult<Attribute> {
        val userName = SecurityContextHolder.getContext().authentication.name

        return notifyAttributeChange(userName, attributeRequestLongpoll)
    }

    @RequestMapping("/notifyAttributeChange/{attributeTopic}/{timeout}", method = [RequestMethod.GET])
    fun notifyAttributeChange(@PathVariable attributeTopic: String, @PathVariable timeout: Long): DeferredResult<Attribute> {
        val userName = SecurityContextHolder.getContext().authentication.name

        return notifyAttributeChange(userName, AttributeRequestLongpoll(attributeTopic.replace(".", "/"), timeout))
    }

    fun notifyAttributeChange(userName: String, attributeRequestLongpoll: AttributeRequestLongpoll): DeferredResult<Attribute> {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val splitTopic = attributeRequestLongpoll.attributeTopic.split("/")
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this attribute")

        if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
            throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
        else {
            try {
                val attribute = EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest(attributeRequestLongpoll.attributeTopic))
                if (attribute != null) {

                    val result = DeferredResult<Attribute>(attributeRequestLongpoll.timeout,
                            CloudioHttpExceptions.Timeout("No attribute change after " + attributeRequestLongpoll.timeout + "ms on topic: " + attributeRequestLongpoll.attributeTopic))

                    if (attribute.constraint == AttributeConstraint.Measure || attribute.constraint == AttributeConstraint.Status) {
                        CompletableFuture.runAsync {
                            object : AttributeChangeNotifier(connectionFactory, "@update." + attributeRequestLongpoll.attributeTopic.replace("/", ".")) {
                                override fun notifyAttributeChange(attribute: Attribute) {
                                    result.setResult(attribute)
                                }
                            }
                        }
                    } else if (attribute.constraint == AttributeConstraint.Parameter || attribute.constraint == AttributeConstraint.SetPoint) {
                        CompletableFuture.runAsync {
                            object : AttributeChangeNotifier(connectionFactory, "@set." + attributeRequestLongpoll.attributeTopic.replace("/", ".")) {
                                override fun notifyAttributeChange(attribute: Attribute) {
                                    result.setResult(attribute)
                                }
                            }
                        }
                    } else
                        throw CloudioHttpExceptions.BadRequest("Attribute with constraint ${attribute.constraint} can't send notification")

                    return result
                } else
                    throw CloudioHttpExceptions.BadRequest("Attribute doesn't exist")
            } catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequest("Couldn't get Attribute: " + e.message)
            }
        }
    }
}
