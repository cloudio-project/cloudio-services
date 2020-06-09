package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.extension.fillFromInfluxDB
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.MONOGOEndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.serialization.wot.NodeThingDescription
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest

//@RestController
//@RequestMapping("/api/v1")
class EndpointManagementController_(var connectionFactory: ConnectionFactory, var influx: InfluxDB, var userGroupRepository: MONGOUserGroupRepository, var userRepository: MONGOUserRepository, var endpointEntityRepository: MONOGOEndpointEntityRepository, val influxProperties: CloudioInfluxProperties) {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/getWotNode", method = [RequestMethod.POST])
    fun getWotNode(@RequestBody nodeRequest: NodeRequest, request: HttpServletRequest): NodeThingDescription {
        val host = request.requestURL.toString().replace("/api/v1/getWotNode", "")

        val userName = SecurityContextHolder.getContext().authentication.name

        return getWotNode(userName, host, nodeRequest)

    }

    @RequestMapping("/getWotNode/{nodeTopic}", method = [RequestMethod.GET])
    fun getWotNode(@PathVariable nodeTopic: String, request: HttpServletRequest): NodeThingDescription {
        val host = request.requestURL.toString().replace("/api/v1/getWotNode/$nodeTopic", "")

        val userName = SecurityContextHolder.getContext().authentication.name

        return getWotNode(userName, host, NodeRequest(nodeTopic.replace(".","/")))
    }

    fun getWotNode(userName: String, host: String, nodeRequest: NodeRequest): NodeThingDescription {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        val splitTopic = genericTopic.split("/")
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this node")

        val nodeThingDescription: NodeThingDescription?
        try {
            nodeThingDescription = EndpointManagementUtil.getWotNode(endpointEntityRepository, nodeRequest, host)
        } catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couldn't get WoT Node: " + e.message)
        }

        if (nodeThingDescription == null) {
            throw CloudioHttpExceptions.BadRequest("Couldn't get WoT Node")
        } else {
            if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
                throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else
                return nodeThingDescription
        }
    }

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

    @RequestMapping("/blockEndpoint", method = [RequestMethod.POST])
    fun blockEndpoint(@RequestBody endpointRequest: EndpointRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name

        //if http admin, can block any endpoint
        if (userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN)) {
            val success = EndpointManagementUtil.blockEndpoint(endpointEntityRepository, endpointRequest)
            if (success)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequest("Couldn't block endpoint")
        } else {
            //else need to check user permission on endpoint --> user need to own
            val permissionMap = PermissionUtils
                    .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
            val genericTopic = endpointRequest.endpointUuid + "/#"

            val endpointGeneralPermission = permissionMap.get(genericTopic)
            if (endpointGeneralPermission?.permission == Permission.OWN) {
                val success = EndpointManagementUtil.blockEndpoint(endpointEntityRepository, endpointRequest)
                if (success)
                    throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
                else
                    throw CloudioHttpExceptions.BadRequest("Couldn't block endpoint")
            } else {
                if (endpointGeneralPermission == null)
                    throw CloudioHttpExceptions.BadRequest("This endpoint doesn't exist")
                else
                    throw CloudioHttpExceptions.BadRequest("You don't own this endpoint")
            }
        }
    }

    @RequestMapping("/unblockEndpoint", method = [RequestMethod.POST])
    fun unblockEndpoint(@RequestBody endpointRequest: EndpointRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name

        //if http admin, can unblock any endpoint
        if (userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN)) {
            val success = EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, endpointRequest)
            if (success)
                throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequest("Couldn't block endpoint")
        } else {
            //else need to check user permission on endpoint --> user need to own
            val permissionMap = PermissionUtils
                    .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
            val genericTopic = endpointRequest.endpointUuid + "/#"

            val endpointGeneralPermission = permissionMap.get(genericTopic)
            if (endpointGeneralPermission?.permission == Permission.OWN) {
                val success = EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, endpointRequest)
                if (success)
                    throw CloudioHttpExceptions.OK(CLOUDIO_SUCCESS_MESSAGE)
                else
                    throw CloudioHttpExceptions.BadRequest("Couldn't unblock endpoint")
            } else {
                if (endpointGeneralPermission == null)
                    throw CloudioHttpExceptions.BadRequest("This endpoint doesn't exist")
                else
                    throw CloudioHttpExceptions.BadRequest("You don't own this endpoint")
            }
        }
    }

    @RequestMapping("/getAccessibleAttributes", method = [RequestMethod.GET])
    fun getAccessibleAttributes(): AccessibleAttributesAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name
        return EndpointManagementUtil.getAccessibleAttributes(userRepository, userGroupRepository, endpointEntityRepository, userName)
    }
}
