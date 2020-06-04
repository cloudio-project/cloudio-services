package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.repo.MONOGOEndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.influxdb.dto.QueryResult
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*

//@RestController
//@RequestMapping("/api/v1")
class HistoryController(val influx: InfluxDB, var userRepository: MONGOUserRepository, var userGroupRepository: MONGOUserGroupRepository, var endpointEntityRepository: MONOGOEndpointEntityRepository, val influxProperties: CloudioInfluxProperties) {

    @RequestMapping("/getAttributeHistoryRequest", method = [RequestMethod.POST])
    fun getAttributeHistoryRequest(@RequestBody historyDefaultRequest: HistoryDefaultRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyDefaultRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this attribute")

        val splitTopic = historyDefaultRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
            throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult = HistoryUtil.getAttributeHistoryRequest(influx, influxProperties.database, historyDefaultRequest)

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequest("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryByDateRequest", method = [RequestMethod.POST])
    fun getAttributeHistoryByDateRequest(@RequestBody historyDateRequest: HistoryDateRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyDateRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this attribute")

        val splitTopic = historyDateRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
            throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult : QueryResult?
        try {
            queryResult = HistoryUtil.getAttributeHistoryByDateRequest(influx, influxProperties.database, historyDateRequest)
        }
        catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couln't access history: "+e.message)
        }

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequest("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryWhere", method = [RequestMethod.POST])
    fun getAttributeHistoryWhere(@RequestBody historyWhereRequest: HistoryWhereRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyWhereRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this attribute")

        val splitTopic = historyWhereRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
            throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)

        val queryResult : QueryResult?
        try {
            queryResult = HistoryUtil.getAttributeHistoryWhere(influx, influxProperties.database, historyWhereRequest)
        }
        catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couln't access history: "+e.message)
        }

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequest("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryExpert", method = [RequestMethod.POST])
    fun getAttributeHistoryExpert(@RequestBody historyExpertRequest: HistoryExpertRequest): QueryResult {
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, historyExpertRequest.attributeTopic.split("/")) == Permission.DENY)
            throw CloudioHttpExceptions.BadRequest("You don't have permission to  access this attribute")

        val splitTopic = historyExpertRequest.attributeTopic.split("/")
        if (endpointEntityRepository.findByIdOrNull(UUID.fromString(splitTopic[0]))!!.blocked)
            throw CloudioHttpExceptions.BadRequest(CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT)
        val queryResult : QueryResult?
        try {
            queryResult = HistoryUtil.getAttributeHistoryExpert(influx, influxProperties.database, historyExpertRequest)
        }
        catch (e: CloudioApiException) {
            throw CloudioHttpExceptions.BadRequest("Couln't access history: "+e.message)
        }

        if (queryResult == null)
            throw CloudioHttpExceptions.BadRequest("Query didn't return a result")
        else
            return queryResult
    }
}