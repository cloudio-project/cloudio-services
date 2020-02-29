package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.TimeUnit
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class LogsUtilTest {

    @Autowired
    private val rabbitTemplate = RabbitTemplate()
    @Autowired
    private lateinit var influx: InfluxDB
    @Autowired
    private lateinit var endpointEntityRepository: EndpointEntityRepository

    val database = "cloudio"

    private lateinit var endpointParameters: EndpointParameters
    private lateinit var createdEndpoint: EndpointEntity

    private val attribute = Attribute(AttributeConstraint.Measure, AttributeType.Number, 10.0, 10.0)

    @Before
    fun setup() {
        val friendlyName = "LuluTheEndpoint"
        endpointParameters = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
        //simulate an @online that populate the endpoint data model
        createdEndpoint = TestUtil.createEndpointEntity(endpointParameters.endpointUuid, endpointParameters.friendlyName)
        endpointEntityRepository.save(createdEndpoint)

        var cloudioLogMessage = CloudioLogMessage(LogLevel.DEBUG)
        // the date from those 10 points goes from 2020-01-14T08:57:49.001Z to 2020-01-14T08:57:49.009Z
        for (i in 0..4) {
            influx.write(database, "autogen", Point
                    .measurement("${endpointParameters.endpointUuid}.logs")
                    .time(1578992269000000 + i * 1000, TimeUnit.MICROSECONDS)
                    .addField("level", cloudioLogMessage.level.toString())
                    .addField("message", cloudioLogMessage.message)
                    .addField("loggerName", cloudioLogMessage.loggerName)
                    .addField("logSource", cloudioLogMessage.logSource)
                    .build())
        }
        cloudioLogMessage = CloudioLogMessage(LogLevel.ERROR)
        for (i in 5..9) {
            influx.write(database, "autogen", Point
                    .measurement("${endpointParameters.endpointUuid}.logs")
                    .time(1578992269000000 + i * 1000, TimeUnit.MICROSECONDS)
                    .addField("level", cloudioLogMessage.level.toString())
                    .addField("message", cloudioLogMessage.message)
                    .addField("loggerName", cloudioLogMessage.loggerName)
                    .addField("logSource", cloudioLogMessage.logSource)
                    .build())
        }
        Thread.sleep(3000) //to be sure the logs will transferred to influxDB (3000ms is default batch time)
    }

    @After
    fun cleanUp() {
        endpointEntityRepository.deleteById(endpointParameters.endpointUuid)
    }

    @Test
    fun getEndpointLogsRequest() {
        val logs = LogsUtil.getEndpointLogsRequest(influx, database, LogsDefaultRequest(endpointParameters.endpointUuid, 10))

        assert(logs!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.logs")
        assert(logs.results[0].series[0].values.size == 10)
    }

    @Test
    fun getEndpointLogsByDateRequest() {
        val logs = LogsUtil.getEndpointLogsByDateRequest(influx, database, LogsDateRequest(endpointParameters.endpointUuid,
                "2020-01-14T08:57:49Z",
                "2020-01-14T08:57:49.01Z"))

        assert(logs!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.logs")
        assert(logs.results[0].series[0].values.size == 10)

    }

    @Test
    fun getEndpointLogsWhereRequest() {
        //filter Error logs -> only 5 logs
        val logs = LogsUtil.getEndpointLogsWhereRequest(influx, database, LogsWhereRequest(endpointParameters.endpointUuid,
                "time >= '2020-01-14T08:57:49Z' and time <= '2020-01-14T08:57:49.01Z' and \"level\" = 'ERROR'"))

        assert(logs!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.logs")
        assert(logs.results[0].series[0].values.size == 5)

    }

    @Test
    fun getLogsLevel() {
        LogsUtil.getLogsLevel(endpointEntityRepository, LogsGetRequest(endpointParameters.endpointUuid))
    }

    @Test
    fun setLogsLevel() {
        LogsUtil.setLogsLevel(rabbitTemplate, LogsSetRequest(endpointParameters.endpointUuid, LogLevel.FATAL))
        Thread.sleep(100) //wait for the mqtt message to be send
        val level = LogsUtil.getLogsLevel(endpointEntityRepository, LogsGetRequest(endpointParameters.endpointUuid))
        assert(level!!.level == LogLevel.FATAL)
    }

    @Test
    fun basicSqlInjection() {
        assertFails {
            LogsUtil.getEndpointLogsByDateRequest(influx, database, LogsDateRequest(endpointParameters.endpointUuid,
                    "2020-01-14T08:57:49Z; Show databases",
                    "2020-01-14T08:57:49.01Z"))
        }
        assertFails {
            LogsUtil.getEndpointLogsWhereRequest(influx, database, LogsWhereRequest(endpointParameters.endpointUuid,
                    "time >= '2020-01-14T08:57:49Z' and time <= '2020-01-14T08:57:49.01Z' and \"level\" = 'ERROR'; Show databases"))
        }
    }
}
