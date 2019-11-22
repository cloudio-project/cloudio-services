package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
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
import org.springframework.data.repository.findByIdOrNull
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
class EndpointManagementController(var connectionFactory: ConnectionFactory, var environment: Environment, var userGroupRepository: UserGroupRepository, var userRepository: UserRepository, var endpointEntityRepository: EndpointEntityRepository) {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/createEndpoint", method = [RequestMethod.POST])
    fun createEndpoint(@RequestBody endpointCreateRequest: EndpointCreateRequest): EndpointParameters {
        val toReturn = EndpointManagementUtil.createEndpoint(endpointEntityRepository, endpointCreateRequest)

        val userName = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findById(userName).get()
        val permissions = user.permissions.toMutableMap()
        permissions.put(toReturn.endpointUuid+"/#", PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST))
        user.permissions = permissions.toMap()
        userRepository.save(user)

        return toReturn

    }

    @RequestMapping("/getEndpoint", method = [RequestMethod.GET])
    fun getEndpoint(@RequestBody endpointRequest: EndpointRequest): EndpointEntity{
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = endpointRequest.endpointUuid + "/#"
        val splitTopic = genericTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this endpoint")

        val endpointEntity = EndpointManagementUtil.getEndpoint(endpointEntityRepository, endpointRequest)
        if(endpointEntity != null) {
            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else{
                PermissionUtils.censorEndpointFromUserPermission(permissionMap, endpointEntity)
                return endpointEntity
            }
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
        val splitTopic = genericTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this endpoint")

        val endpointFriendlyName = EndpointManagementUtil.getEndpointFriendlyName(endpointEntityRepository, endpointRequest)
        if(endpointFriendlyName != null) {
            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else
                return endpointFriendlyName
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get endpoint friendly name")

    }

    @RequestMapping("/getNode", method = [RequestMethod.GET])
    fun getNode(@RequestBody nodeRequest: NodeRequest): Node {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        val splitTopic = genericTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this node")

        val node: Node?
        try{
            node = EndpointManagementUtil.getNode(endpointEntityRepository, nodeRequest)
        }
        catch(e: CloudioApiException){
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Node: "+e.message)
        }

        if(node != null) {

            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else {
                PermissionUtils.censorNodeFromUserPermission(permissionMap, nodeRequest.nodeTopic + "/", node)
                return node
            }
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
        val splitTopic = genericTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this node")

        val wotNode: WotNode?
        try{
            wotNode = EndpointManagementUtil.getWotNode(endpointEntityRepository, nodeRequest, host)
        }
        catch(e: CloudioApiException){
            throw CloudioHttpExceptions.BadRequestException("Couldn't get WoT Node: "+e.message)
        }

        if(wotNode == null) {
            throw CloudioHttpExceptions.BadRequestException("Couldn't get WoT Node")
        }
        else{
            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else
                return wotNode
        }
    }

    @RequestMapping("/getObject", method = [RequestMethod.GET])
    fun getObject(@RequestBody objectRequest: ObjectRequest): CloudioObject {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = objectRequest.objectTopic + "/#"
        val splitTopic = genericTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this object")

        val cloudioObject: CloudioObject?
        try{
            cloudioObject = EndpointManagementUtil.getObject(endpointEntityRepository, objectRequest)
        }
        catch(e: CloudioApiException){
            throw CloudioHttpExceptions.BadRequestException("Couldn't get WoT Node: "+e.message)
        }

        if(cloudioObject != null) {
            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else{
                PermissionUtils.censorObjectFromUserPermission(permissionMap,objectRequest.objectTopic+"/", cloudioObject)
                return cloudioObject
            }
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Object")
    }

    @RequestMapping("/getAttribute", method = [RequestMethod.GET])
    fun getAttribute(@RequestBody attributeRequest: AttributeRequest): Attribute {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val splitTopic = attributeRequest.attributeTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val attribute: Attribute?
        try{
            attribute = EndpointManagementUtil.getAttribute(endpointEntityRepository, attributeRequest)
        }
        catch(e: CloudioApiException){
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Attribute: "+e.message)
        }
        if(attribute != null){
            if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
            else
                return attribute
        }
        else
            throw CloudioHttpExceptions.BadRequestException("Couldn't get Attribute")
    }

    @RequestMapping("/setAttribute", method = [RequestMethod.GET])
    fun setAttribute(@RequestBody attributeSetRequest: AttributeSetRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val splitTopic = attributeSetRequest.attributeTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)<Permission.WRITE)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to write this attribute")

        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
        else{
            try{
                EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, attributeSetRequest)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            }
            catch(e: CloudioApiException){
                throw CloudioHttpExceptions.BadRequestException("Couldn't set attribute: "+e.message)
            }
        }

    }

    @RequestMapping("/notifyAttributeChange", method = [RequestMethod.GET])
    fun notifyAttributeChange(@RequestBody attributeRequestLongpoll: AttributeRequestLongpoll): DeferredResult<Attribute> {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)

        val splitTopic = attributeRequestLongpoll.attributeTopic.split("/")
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, splitTopic)==Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
        else{
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
    }

    @RequestMapping("/blockEndpoint", method = [RequestMethod.POST])
    fun blockEndpoint(@RequestBody endpointRequest: EndpointRequest){
        val userName = SecurityContextHolder.getContext().authentication.name

        //if http admin, can block any endpoint
        if (userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN)){
            val success = EndpointManagementUtil.blockEndpoint(endpointEntityRepository, endpointRequest)
            if(success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't block endpoint")
        }
        else{
            //else need to check user permission on endpoint --> user need to own
            val permissionMap = PermissionUtils
                    .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
            val genericTopic = endpointRequest.endpointUuid + "/#"

            val endpointGeneralPermission = permissionMap.get(genericTopic)
            if(endpointGeneralPermission?.permission == Permission.OWN){
                val success = EndpointManagementUtil.blockEndpoint(endpointEntityRepository, endpointRequest)
                if(success)
                    throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
                else
                    throw CloudioHttpExceptions.BadRequestException("Couldn't block endpoint")
            }
            else{
                if(endpointGeneralPermission==null)
                    throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
                else
                    throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
            }
        }
    }

    @RequestMapping("/unblockEndpoint", method = [RequestMethod.POST])
    fun unblockEndpoint(@RequestBody endpointRequest: EndpointRequest){
        val userName = SecurityContextHolder.getContext().authentication.name

        //if http admin, can unblock any endpoint
        if (userRepository.findById(userName).get().authorities.contains(Authority.HTTP_ADMIN)){
            val success = EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, endpointRequest)
            if(success)
                throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't block endpoint")
        }
        else {
            //else need to check user permission on endpoint --> user need to own
            val permissionMap = PermissionUtils
                    .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
            val genericTopic = endpointRequest.endpointUuid + "/#"

            val endpointGeneralPermission = permissionMap.get(genericTopic)
            if (endpointGeneralPermission?.permission == Permission.OWN) {
                val success = EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, endpointRequest)
                if (success)
                    throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
                else
                    throw CloudioHttpExceptions.BadRequestException("Couldn't unblock endpoint")
            } else {
                if (endpointGeneralPermission == null)
                    throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
                else
                    throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
            }
        }
    }

    @RequestMapping("/getOwnedEndpoints", method = [RequestMethod.GET])
    fun getOwnedEndpoints(): OwnedEndpointsAnswer{
        val userName = SecurityContextHolder.getContext().authentication.name
        return EndpointManagementUtil.getOwnedEndpoints(userRepository, userGroupRepository, endpointEntityRepository, userName)
    }

    @RequestMapping("/getAccessibleAttributes", method = [RequestMethod.GET])
    fun getAccessibleAttributes(): AccessibleAttributesAnswer{
        val userName = SecurityContextHolder.getContext().authentication.name
        return EndpointManagementUtil.getAccessibleAttributes(userRepository, userGroupRepository, endpointEntityRepository, userName)
    }
}