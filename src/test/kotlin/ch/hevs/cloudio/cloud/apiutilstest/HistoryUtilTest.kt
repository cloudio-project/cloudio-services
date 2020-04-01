package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.AttributeType
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.TimeUnit
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class HistoryUtilTest {

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
        val friendlyName = "PaquitoTheEndpoint"
        endpointParameters = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
        //simulate an @online that populate the endpoint data model
        createdEndpoint = TestUtil.createEndpointEntity(endpointParameters.endpointUuid.toString(), endpointParameters.friendlyName)
        endpointEntityRepository.save(createdEndpoint)

        // the date from those 10 points goes from 2020-01-14T08:57:49Z to 2020-01-14T08:57:49.009Z
        for (i in 0..9) {
            val point = Point
                    .measurement("${endpointParameters.endpointUuid}.demoNode.demoObject.demoMeasure")
                    .time(1578992269000000 + i * 1000, TimeUnit.MICROSECONDS)
                    .tag("constraint", attribute.constraint.toString())
                    .tag("type", attribute.type.toString())
            point.addField("value", attribute.value as Number)
            val myPoint = point.build()

            //if batch enabled, save point in set, either send it
            influx.write(database, "autogen", myPoint)
        }
        Thread.sleep(3000) //to be sure the update will be transferred to influxDB (3000ms is default batch time)
    }

    @After
    fun cleanUp() {
        endpointEntityRepository.deleteById(endpointParameters.endpointUuid)
    }

    @Test
    fun getAttributeHistoryRequest() {
        val history = HistoryUtil.getAttributeHistoryRequest(influx, database,
                HistoryDefaultRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure", 10))

        assert(history!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.demoNode.demoObject.demoMeasure")
        assert(history.results[0].series[0].values.size == 10)
    }

    @Test
    fun getAttributeHistoryByDateRequest() {
        val history = HistoryUtil.getAttributeHistoryByDateRequest(influx, database,
                HistoryDateRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                        "2020-01-14T08:57:49Z",
                        "2020-01-14T08:57:49.009Z"))
        assert(history!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.demoNode.demoObject.demoMeasure")
        assert(history.results[0].series[0].values.size == 10)
    }

    @Test
    fun getAttributeHistoryWhereRequest() {
        val history = HistoryUtil.getAttributeHistoryWhere(influx, database,
                HistoryWhereRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                        "time >= '2020-01-14T08:57:49Z' and time <= '2020-01-14T08:57:49.01Z'"))
        assert(history!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.demoNode.demoObject.demoMeasure")
        assert(history.results[0].series[0].values.size == 10)
    }

    @Test
    fun getAttributeHistoryExpert() {
        //with the group of data to 2000us, we expect only 5 data (half since there is data every 1000us)
        val history = HistoryUtil.getAttributeHistoryExpert(influx, database,
                HistoryExpertRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                        AggregationInflux.MEAN,
                        "2020-01-14T08:57:49Z",
                        "2020-01-14T08:57:49.01Z",
                        "2000u",
                        FillInflux.NONE,
                        1000))

        assert(history!!.results[0].series[0].name == "${endpointParameters.endpointUuid}.demoNode.demoObject.demoMeasure")
        assert(history.results[0].series[0].values.size == 5)
    }

    @Test
    fun basicSqlInjection() {
        assertFails {
            HistoryUtil.getAttributeHistoryByDateRequest(influx, database,
                    HistoryDateRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                            "2020-01-14T08:57:49.001Z",
                            "2020-01-14T08:57:49.01Z; Show databases"))
        }

        assertFails {
            HistoryUtil.getAttributeHistoryWhere(influx, database,
                    HistoryWhereRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                            "WHERE time >= '2020-01-14T08:57:49.001Z' and time <= '2020-01-14T08:57:49.01Z'; Show databases"))
        }

        assertFails {
            HistoryUtil.getAttributeHistoryExpert(influx, database,
                    HistoryExpertRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure",
                            AggregationInflux.MEAN,
                            "2020-01-14T08:57:49.001Z",
                            "2020-01-14T08:57:49.01Z; Show databases",
                            "2000u",
                            FillInflux.NONE,
                            1000))
        }

    }
}
