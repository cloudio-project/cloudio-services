package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.MqttNotifier
import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.JobParameter
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JobsUtilTest {

    @Autowired
    val rabbitTemplate = RabbitTemplate()
    @Autowired
    private lateinit var endpointEntityRepository: EndpointEntityRepository

    @Autowired
    private lateinit var connectionFactory: ConnectionFactory
    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    private lateinit var endpointParameters: EndpointParameters
    private lateinit var createdEndpoint: EndpointEntity

    private lateinit var jobExecuteRequest: JobExecuteRequest

    private var messageReceived = false

    @BeforeAll
    fun setup() {
        val friendlyName = "KarolTheEndpoint"
        endpointParameters = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
        //simulate an @online that populate the endpoint data model
        createdEndpoint = TestUtil.createEndpointEntity(endpointParameters.endpointUuid, endpointParameters.friendlyName)
        endpointEntityRepository.save(createdEndpoint)
        jobExecuteRequest = JobExecuteRequest(endpointParameters.endpointUuid, "cmd://listJobs", false, "123soleil", "fake data", 1000)
    }

    @AfterAll
    fun cleanUp() {
        endpointEntityRepository.deleteById(endpointParameters.endpointUuid)
    }

    @Test
    fun executeJob() {
        val startMilli = System.currentTimeMillis()

        object : MqttNotifier(connectionFactory, "@exec.${endpointParameters.endpointUuid}") {
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