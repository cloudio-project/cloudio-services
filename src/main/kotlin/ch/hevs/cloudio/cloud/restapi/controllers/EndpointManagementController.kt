package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.restapi.CloudioOkException
import ch.hevs.cloudio.cloud.restapi.CloudioTimeoutException
import ch.hevs.cloudio.cloud.serialization.JsonWotSerializationFormat
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
        permissions.put(toReturn.UUID+"/#", PrioritizedPermission(Permission.OWN, Priority.HIGHEST))
        user.permissions = permissions.toMap()
        userRepository.save(user)

        return toReturn

    }

    @RequestMapping("/getEndpoint", method = [RequestMethod.GET])
    fun getEndpoint(@RequestBody endpointRequest: EndpointRequest): EndpointEntity {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = endpointRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        val endpointEntity = EndpointManagementUtil.getEndpoint(endpointEntityRepository, endpointRequest)
        if(endpointEntity != null) {

            PermissionUtils.censoreEndpointFromUserPermission(permissionMap,endpointEntity)

            return endpointEntity
        }
        else
            throw CloudioBadRequestException("Couldn't get Endpoint")
    }

    @RequestMapping("/getNode", method = [RequestMethod.GET])
    fun getNode(@RequestBody nodeRequest: NodeRequest): Node {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this node")

        val node = EndpointManagementUtil.getNode(endpointEntityRepository, nodeRequest)
        if(node != null) {
            PermissionUtils.censoreNodeFromUserPermission(permissionMap,nodeRequest.nodeTopic+"/", node)
            return node
        }
        else
            throw CloudioBadRequestException("Couldn't get Node")
    }

    @RequestMapping("/getWotNode", method = [RequestMethod.GET])
    fun getWotNode(@RequestBody nodeRequest: NodeRequest): WotNode? {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = nodeRequest.nodeTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this node")

        val splitedTopic = nodeRequest.nodeTopic.split("/")
        val endpointEntity = endpointEntityRepository.findByIdOrNull(splitedTopic[0])!!

        return JsonWotSerializationFormat.wotNodeFromCloudioNode(endpointEntity.endpoint, endpointEntity.id, splitedTopic[1])

    }

    @RequestMapping("/getObject", method = [RequestMethod.GET])
    fun getObject(@RequestBody objectRequest: ObjectRequest): CloudioObject {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = objectRequest.objectTopic + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this object")

        val cloudioObject = EndpointManagementUtil.getObject(endpointEntityRepository, objectRequest)
        if(cloudioObject != null) {
            val permissionMap = PermissionUtils
                    .permissionFromGroup(userRepository.findById(userName).get().permissions,
                            userRepository.findById(userName).get().userGroups,
                            userGroupRepository)
            PermissionUtils.censoreObjectFromUserPermission(permissionMap,objectRequest.objectTopic+"/", cloudioObject)
            return cloudioObject
        }
        else
            throw CloudioBadRequestException("Couldn't get Object")
    }

    @RequestMapping("/getAttribute", method = [RequestMethod.GET])
    fun getAttribute(@RequestBody attributeRequest: AttributeRequest): Attribute {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeRequest.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")

        val attribute = EndpointManagementUtil.getAttribute(endpointEntityRepository, attributeRequest)
        if(attribute != null)
            return attribute
        else
            throw CloudioBadRequestException("Couldn't get Attribute")
    }

    @RequestMapping("/setAttribute", method = [RequestMethod.GET])
    fun setAttribute(@RequestBody attributeSetRequest: AttributeSetRequest){
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeSetRequest.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")

        val setAction = EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, attributeSetRequest)
        if(setAction.success)
            throw CloudioOkException("Success")
        else
            throw CloudioBadRequestException("Couldn't set attribute: "+setAction.message)
    }

    @RequestMapping("/notifyAttributeChange", method = [RequestMethod.GET])
    fun notifyAttributeChange(@RequestBody attributeRequestLongpoll: AttributeRequestLongpoll): DeferredResult<Attribute> {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, attributeRequestLongpoll.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")

        val result = DeferredResult<Attribute>(attributeRequestLongpoll.timeout,
                CloudioTimeoutException("No attribute change after "+attributeRequestLongpoll.timeout+"ms on topic: "+attributeRequestLongpoll.attributeTopic))

        CompletableFuture.runAsync {
            object :  EndpointManagementUtil.TopicChangeNotifier(connectionFactory, "@set."+attributeRequestLongpoll.attributeTopic.replace("/", ".")){
                override fun notifyAttributeChange(attribute: Attribute){
                    result.setResult(attribute)
                }
            }
        }
        return result
    }
}