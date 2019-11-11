package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.ExecOutputNotifier
import ch.hevs.cloudio.cloud.apiutils.JobExecuteRequest
import ch.hevs.cloudio.cloud.apiutils.JobsUtil
import ch.hevs.cloudio.cloud.model.JobsLineOutput
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.restapi.CloudioOkException
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import com.rabbitmq.client.ConnectionFactory
import org.influxdb.InfluxDB
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/v1")
class JobsController(var connectionFactory: ConnectionFactory, val env: Environment, val influx: InfluxDB, var userRepository: UserRepository, var userGroupRepository: UserGroupRepository){

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    private val executor = Executors.newCachedThreadPool()

    @RequestMapping("/executeJob", method = [RequestMethod.POST])
    fun executeJob(@RequestBody jobExecuteRequest: JobExecuteRequest): SseEmitter {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = jobExecuteRequest.endpointUuid + "/#"
        if (PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/")) == Permission.DENY)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")



        if (!jobExecuteRequest.getOutput){
            JobsUtil.executeJob(rabbitTemplate, jobExecuteRequest)
            throw CloudioOkException("Success")
        }
        else {

            val emitter = SseEmitter()

            executor.execute {
                try {
                    val execOutputNotifier = object : ExecOutputNotifier(connectionFactory, "@execOutput." + jobExecuteRequest.endpointUuid) {
                        override fun notifyExecOutput(jobsLineOutput: JobsLineOutput) {
                            if(jobsLineOutput.correlationID == jobExecuteRequest.correlationID)
                            emitter.send(SseEmitter.event().id(jobsLineOutput.correlationID).data(jobsLineOutput.data))
                        }
                    }
                    JobsUtil.executeJob(rabbitTemplate, jobExecuteRequest)
                    Thread.sleep(jobExecuteRequest.timeout)
                    emitter.complete()
                    execOutputNotifier.deleteQueue()

                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }
            return emitter
        }


    }
}