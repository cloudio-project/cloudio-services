package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.restapi.CloudioOkException
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.influxdb.dto.QueryResult
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class LogsController(val env: Environment, val influx: InfluxDB, var userRepository: UserRepository, var endpointEntityRepository: EndpointEntityRepository, var userGroupRepository: UserGroupRepository){

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @RequestMapping("/getEndpointLogsRequest", method = [RequestMethod.GET])
    fun getEndpointLogsRequest(@RequestBody logsDefaultRequest: LogsDefaultRequest): QueryResult{
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = logsDefaultRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        val queryResult = LogsUtil.getEndpointLogsRequest(influx, database, logsDefaultRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getEndpointLogsByDateRequest", method = [RequestMethod.GET])
    fun getEndpointLogsByDateRequest(@RequestBody logsDateRequest: LogsDateRequest): QueryResult{
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = logsDateRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        val queryResult = LogsUtil.getEndpointLogsByDateRequest(influx, database, logsDateRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult

    }

    @RequestMapping("/getEndpointLogsWhereRequest", method = [RequestMethod.GET])
    fun getEndpointLogsWhereRequest(@RequestBody logsWhereRequest: LogsWhereRequest): QueryResult{
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = logsWhereRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        val queryResult = LogsUtil.getEndpointLogsWhereRequest(influx, database, logsWhereRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/setLogsLevel", method = [RequestMethod.POST])
    fun setLogsLevel(@RequestBody logsSetRequest: LogsSetRequest){
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = logsSetRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        LogsUtil.setLogsLevel(rabbitTemplate, logsSetRequest)
        throw CloudioOkException("Success")
    }

    @RequestMapping("/getLogsLevel", method = [RequestMethod.GET])
    fun getLogsLevel(@RequestBody logsGetRequest: LogsGetRequest): LogsGetAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = logsGetRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        val logLevel = LogsUtil.getLogsLevel(endpointEntityRepository, logsGetRequest)
        if(logLevel != null)
            return logLevel
        else
            throw CloudioBadRequestException("Couldn't retrieve log level")
    }
}