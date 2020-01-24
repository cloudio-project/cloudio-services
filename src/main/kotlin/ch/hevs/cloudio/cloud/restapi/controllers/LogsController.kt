package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_SUCCESS_MESSAGE
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.influxdb.dto.QueryResult
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class LogsController(val env: Environment, val influx: InfluxDB, var userRepository: UserRepository, var endpointEntityRepository: EndpointEntityRepository, var userGroupRepository: UserGroupRepository) {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @RequestMapping("/getEndpointLogsRequest", method = [RequestMethod.POST])
    fun getEndpointLogsRequest(@RequestBody logsDefaultRequest: LogsDefaultRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = logsDefaultRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {

            if (endpointEntityRepository.findByIdOrNull(logsDefaultRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

            val queryResult = LogsUtil.getEndpointLogsRequest(influx, database, logsDefaultRequest)

            if (queryResult == null)
                throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
            else
                return queryResult
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/getEndpointLogsByDateRequest", method = [RequestMethod.POST])
    fun getEndpointLogsByDateRequest(@RequestBody logsDateRequest: LogsDateRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = logsDateRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {

            if (endpointEntityRepository.findByIdOrNull(logsDateRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

            val queryResult : QueryResult?
            try {
                queryResult = LogsUtil.getEndpointLogsByDateRequest(influx, database, logsDateRequest)
            }
            catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couln't access logs: "+e.message)
            }

            if (queryResult == null)
                throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
            else
                return queryResult
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/getEndpointLogsWhereRequest", method = [RequestMethod.POST])
    fun getEndpointLogsWhereRequest(@RequestBody logsWhereRequest: LogsWhereRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = logsWhereRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {

            if (endpointEntityRepository.findByIdOrNull(logsWhereRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

            val queryResult : QueryResult?
            try {
                queryResult = LogsUtil.getEndpointLogsWhereRequest(influx, database, logsWhereRequest)
            }
            catch (e: CloudioApiException) {
                throw CloudioHttpExceptions.BadRequestException("Couln't access logs: "+e.message)
            }

            if (queryResult == null)
                throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
            else
                return queryResult
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/setLogsLevel", method = [RequestMethod.POST])
    fun setLogsLevel(@RequestBody logsSetRequest: LogsSetRequest) {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = logsSetRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {

            if (endpointEntityRepository.findByIdOrNull(logsSetRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

            LogsUtil.setLogsLevel(rabbitTemplate, logsSetRequest)
            throw CloudioHttpExceptions.OkException(CLOUDIO_SUCCESS_MESSAGE)
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/getLogsLevel", method = [RequestMethod.POST])
    fun getLogsLevel(@RequestBody logsGetRequest: LogsGetRequest): LogsGetAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getLogsLevel(userName, logsGetRequest)
    }

    @RequestMapping("/getLogsLevel/{endpointUuid}", method = [RequestMethod.GET])
    fun getLogsLevel(@PathVariable endpointUuid: String): LogsGetAnswer {
        val userName = SecurityContextHolder.getContext().authentication.name
        return getLogsLevel(userName, LogsGetRequest(endpointUuid))
    }

    fun getLogsLevel(userName: String, logsGetRequest: LogsGetRequest): LogsGetAnswer {
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = logsGetRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {

            if (endpointEntityRepository.findByIdOrNull(logsGetRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

            val logLevel = LogsUtil.getLogsLevel(endpointEntityRepository, logsGetRequest)
            if (logLevel != null)
                return logLevel
            else
                throw CloudioHttpExceptions.BadRequestException("Couldn't retrieve log level")
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }
}