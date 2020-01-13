package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.influxdb.dto.QueryResult
import org.springframework.core.env.Environment
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HistoryController(val env: Environment, val influx: InfluxDB, var userRepository: UserRepository, var userGroupRepository: UserGroupRepository, var endpointEntityRepository: EndpointEntityRepository) {

    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @RequestMapping("/getAttributeHistoryRequest", method = [RequestMethod.POST])
    fun getAttributeHistoryRequest(@RequestBody historyDefaultRequest: HistoryDefaultRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyDefaultRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val splitTopic = historyDefaultRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult = HistoryUtil.getAttributeHistoryRequest(influx, database, historyDefaultRequest)

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryByDateRequest", method = [RequestMethod.POST])
    fun getAttributeHistoryByDateRequest(@RequestBody historyDateRequest: HistoryDateRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyDateRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val splitTopic = historyDateRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult = HistoryUtil.getAttributeHistoryByDateRequest(influx, database, historyDateRequest)

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryWhere", method = [RequestMethod.POST])
    fun getAttributeHistoryWhere(@RequestBody historyWhereRequest: HistoryWhereRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyWhereRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val splitTopic = historyWhereRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult = HistoryUtil.getAttributeHistoryWhere(influx, database, historyWhereRequest)

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryExpert", method = [RequestMethod.POST])
    fun getAttributeHistoryExpert(@RequestBody historyExpertRequest: HistoryExpertRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyExpertRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequestException("You don't have permission to  access this attribute")

        val splitTopic = historyExpertRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(splitTopic[0])!!.blocked)
            throw CloudioHttpExceptions.BadRequestException(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult = HistoryUtil.getAttributeHistoryExpert(influx, database, historyExpertRequest)

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequestException("Query didn't return a result")
        else
            return queryResult
    }
}