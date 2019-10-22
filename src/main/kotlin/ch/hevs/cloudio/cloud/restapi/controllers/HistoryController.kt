package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.HistoryDateRequest
import ch.hevs.cloudio.cloud.apiutils.HistoryDefaultRequest
import ch.hevs.cloudio.cloud.apiutils.HistoryUtil
import ch.hevs.cloudio.cloud.apiutils.HistoryWhereRequest
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HistoryController(val env: Environment,val influx: InfluxDB, var userRepository: UserRepository, var userGroupRepository: UserGroupRepository){

    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @RequestMapping("/getAttributeHistoryRequest")
    fun getAttributeHistoryRequest(@RequestBody historyDefaultRequest: HistoryDefaultRequest): QueryResult{
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, historyDefaultRequest.attributeTopic.split("/"))== Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")

        val queryResult = HistoryUtil.getAttributeHistoryRequest(influx, database, historyDefaultRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult
    }

    @RequestMapping("/getAttributeHistoryByDateRequest")
    fun getAttributeHistoryByDateRequest(@RequestBody historyDateRequest: HistoryDateRequest): QueryResult{val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, historyDateRequest.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")

        val queryResult = HistoryUtil.getAttributeHistoryByDateRequest(influx, database, historyDateRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult

    }

    @RequestMapping("/getAttributeHistoryWhere")
    fun getAttributeHistoryWhere(@RequestBody historyWhereRequest: HistoryWhereRequest): QueryResult{
        val userName = SecurityContextHolder.getContext().authentication.name
        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, historyWhereRequest.attributeTopic.split("/"))==Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this attribute")
        val queryResult = HistoryUtil.getAttributeHistoryWhere(influx, database, historyWhereRequest)

        if (queryResult == null)
            throw CloudioBadRequestException("Query didn't return a result")
        else
            return queryResult
    }
}