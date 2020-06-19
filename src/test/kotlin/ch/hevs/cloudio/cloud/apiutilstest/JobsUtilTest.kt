package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.MqttNotifier
import ch.hevs.cloudio.cloud.apiutils.JobExecuteRequest
import ch.hevs.cloudio.cloud.apiutils.JobsUtil
import ch.hevs.cloudio.cloud.dao.Endpoint
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.JobParameter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class JobsUtilTest {

    @Autowired
    val rabbitTemplate = RabbitTemplate()
    @Autowired
    private lateinit var endpointRepository: EndpointRepository

    @Autowired
    private lateinit var connectionFactory: ConnectionFactory
    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    private lateinit var createdEndpointUUID: UUID

    private lateinit var jobExecuteRequest: JobExecuteRequest

    private var messageReceived = false

    @Before
    fun setup() {
        val friendlyName = "KarolTheEndpoint"
        createdEndpointUUID = endpointRepository.save(Endpoint(
                friendlyName = friendlyName
        )).uuid
        jobExecuteRequest = JobExecuteRequest(createdEndpointUUID.toString(), "cmd://listJobs", false, "123soleil", "fake data", 1000)
    }

    @After
    fun cleanUp() {
        endpointRepository.deleteById(createdEndpointUUID)
    }

    @Test
    fun executeJob() {
        val startMilli = System.currentTimeMillis()

        object : MqttNotifier(connectionFactory, "@exec.${createdEndpointUUID}") {
            override fun notifyMqttMessage(message: String) {
                val output: JobParameter = mapper.readValue(message.toByteArray(), JobParameter::class.java)

                assert(output.sendOutput == jobExecuteRequest.getOutput)
                assert(output.correlationID == jobExecuteRequest.correlationID)
                assert(output.data == jobExecuteRequest.data)
                assert(output.jobURI == jobExecuteRequest.jobURI)
                messageReceived = true
            }
        }

        JobsUtil.executeJob(rabbitTemplate, jobExecuteRequest)
        while (!messageReceived) {
            Thread.sleep(100)
            if (System.currentTimeMillis() - startMilli > 5000)
                assert(false) // mqtt message not received
        }


    }
}