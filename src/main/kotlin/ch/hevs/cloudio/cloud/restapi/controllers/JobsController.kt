package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.JobExecuteRequest
import ch.hevs.cloudio.cloud.apiutils.JobsUtil
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.restapi.CloudioOkException
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.influxdb.InfluxDB
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
class JobsController(val env: Environment, val influx: InfluxDB, var userRepository: UserRepository, var userGroupRepository: UserGroupRepository){

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/executeJob", method = [RequestMethod.POST])
    fun executeJob(@RequestBody jobExecuteRequest: JobExecuteRequest){
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = jobExecuteRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))== Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")

        JobsUtil.executeJob(rabbitTemplate, jobExecuteRequest)
        throw CloudioOkException("Success")


    }
}