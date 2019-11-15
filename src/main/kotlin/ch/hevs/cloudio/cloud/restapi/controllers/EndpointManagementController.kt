package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import ch.hevs.cloudio.cloud.serialization.wot.WotNode
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import com.rabbitmq.client.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1")
class EndpointManagementController(var connectionFactory: ConnectionFactory, var environment: Environment, var userGroupRepository: UserGroupRepository, var userRepository: UserRepository, var endpointEntityRepository: EndpointEntityRepository, var endpointParametersRepository: EndpointParametersRepository) {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/createEndpoint", method = [RequestMethod.POST])
    fun createEndpoint(@RequestBody endpointCreateRequest: EndpointCreateRequest): EndpointParameters {
        val toReturn = EndpointManagementUtil.createEndpoint(endpointParametersRepository, endpointCreateRequest)

        val userName = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findById(userName).get()
        val permissions = user.permissions.toMutableMap()
        permissions.put(toReturn.endpointUuid+"/#", PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST))
        user.permissions = permissions.toMap()
        userRepository.save(user)

        return toReturn

    }

    @RequestMapping("/getEndpoint", method = [RequestMethod.GET])
    fun getEndpoint(@RequestBody endpointRequest: EndpointRequest): EndpointAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = endpointRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this endpoint")

        val endpointAnswer = EndpointManagementUtil.getEndpoint(endpointEntityRepository, endpointParametersRepository, endpointRequest)
        if(endpointAnswer != null) {
            PermissionUtils.censorEndpointFromUserPermission(permissionMap,endpointAnswer.endpointEntity)
            return endpointAnswer
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Endpoint")
    }

    @RequestMapping("/getEndpointFriendlyName", method = [RequestMethod.GET])
    fun getEndpointFriendlyName(@RequestBody endpointRequest: EndpointRequest): EndpointFriendlyName{
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = endpointRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this endpoint")

        val endpointFriendlyName = EndpointManagementUtil.getEndpointFriendlyName(endpointParametersRepository, endpointRequest)
        if(endpointFriendlyName != null)
            return endpointFriendlyName
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get endpoint friendly name")

    }

    @RequestMapping("/getNode", method = [RequestMethod.GET])
    fun getNode(@RequestBody nodeRequest: NodeRequest): Node {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this node")

        val node = EndpointManagementUtil.getNode(endpointEntityRepository, nodeRequest)
        if(node != null) {
            PermissionUtils.censorNodeFromUserPermission(permissionMap,nodeRequest.nodeTopic+"/", node)
            return node
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Node")
    }

    @RequestMapping("/getWotNode", method = [RequestMethod.GET])
    fun getWotNode(@RequestBody nodeRequest: NodeRequest, request: HttpServletRequest): WotNode {
        val host = request.requestURL.toString().replace("/api/v1/getWotNode", "")

        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this node")

        val wotNode = EndpointManagementUtil.getWotNode(endpointEntityRepository, nodeRequest, host)
        if(wotNode == null)
            throw CloudioHttpExceptions.BadRequestException("Couldn't get WoT Node")
        else
            return wotNode
    }

    @RequestMapping("/getObject", method = [RequestMethod.GET])
    fun getObject(@RequestBody objectRequest: ObjectRequest): CloudioObject {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = objectRequest.objectTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this object")

        val cloudioObject = EndpointManagementUtil.getObject(endpointEntityRepository, objectRequest)
        if(cloudioObject != null) {
            PermissionUtils.censorObjectFromUserPermission(permissionMap,objectRequest.objectTopic+"/", cloudioObject)
            return cloudioObject
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Object")
    }

    @RequestMapping("/getAttribute", method = [RequestMethod.GET])
    fun getAttribute(@RequestBody attributeRequest: AttributeRequest): Attribute {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeRequest.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val attribute = EndpointManagementUtil.getAttribute(endpointEntityRepository, attributeRequest)
        if(attribute != null)
            return attribute
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Attribute")
    }

    @RequestMapping("/setAttribute", method = [RequestMethod.GET])
    fun setAttribute(@RequestBody attributeSetRequest: AttributeSetRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeSetRequest.attributeTopic.split("/"))<Permission.WRITE)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to write this attribute")

        val setAction = EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, attributeSetRequest)
        if(setAction.success)
            throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't set attribute: "+setAction.message)
    }

    @RequestMapping("/notifyAttributeChange", method = [RequestMethod.GET])
    fun notifyAttributeChange(@RequestBody attributeRequestLongpoll: AttributeRequestLongpoll): DeferredResult<Attribute> {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeRequestLongpoll.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val result = DeferredResult<Attribute>(attributeRequestLongpoll.timeout,
                CloudioHttpExceptions.TimeoutException("No attribute change after "+attributeRequestLongpoll.timeout+"ms on topic: "+attributeRequestLongpoll.attributeTopic))

        CompletableFuture.runAsync {
            object :  AttributeChangeNotifier(connectionFactory, "@set."+attributeRequestLongpoll.attributeTopic.replace("/", ".")){
                override fun notifyAttributeChange(attribute: Attribute){
                    result.setResult(attribute)
                }
            }
        }
        return result
    }

    @RequestMapping("/getOwnedEndpoints", method = [RequestMethod.GET])
    fun getOwnedEndpoints(): OwnedEndpointsAnswer{
        val userName = SecurityContextHolder.getContext().authentication.name
        return EndpointManagementUtil.getOwnedEndpoints(userRepository, userGroupRepository, endpointParametersRepository, userName)
    }

    @RequestMapping("/getAccessibleAttributes", method = [RequestMethod.GET])
    fun getAccessibleAttributes(): AccessibleAttributesAnswer{
        val userName = SecurityContextHolder.getContext().authentication.name
        return EndpointManagementUtil.getAccessibleAttributes(userRepository, userGroupRepository, endpointEntityRepository, userName)
    }
}