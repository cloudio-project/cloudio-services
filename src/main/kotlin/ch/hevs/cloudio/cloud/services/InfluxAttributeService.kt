package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractAttributeService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeType
import com.influxdb.client.*
import org.apache.commons.logging.LogFactory
import com.influxdb.client.domain.InfluxQLQuery
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
@Profile("update-influx", "default")
class InfluxAttributeService(
        private val influx: InfluxDBClient,
        private val influxProperties: CloudioInfluxProperties
) : AbstractAttributeService() {
    private val log = LogFactory.getLog(InfluxAttributeService::class.java)
    private lateinit var writeApi: WriteApi
    @PostConstruct
    fun initialize() {
        //TODO update to influx 2.x
        var queryApi: InfluxQLQueryApi = influx.influxQLQueryApi
        // Create database if needed
        if (queryApi.query(InfluxQLQuery("SHOW DATABASES", "")).results.firstOrNull()?.series?.firstOrNull()?.values?.none { it.values.firstOrNull() == influxProperties.database} != false)
            queryApi.query(InfluxQLQuery("CREATE DATABASE ${influxProperties.database}", ""))

        writeApi = influx.makeWriteApi(WriteOptions.builder().batchSize(influxProperties.batchSize).flushInterval(influxProperties.batchIntervalMs).build())

    }

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
        if (attribute.timestamp != null) {
            // create the influxDB point
            val point = Point
                    .measurement(attributeId)
                    .time((attribute.timestamp!! * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                    .addTag("constraint", attribute.constraint.toString())
                    .addTag("type", attribute.type.toString())

            try {
                // complete the point depending on attribute type
                when (attribute.type) {
                    AttributeType.Boolean -> point.addField("value", attribute.value as Boolean)
                    AttributeType.Integer -> point.addField("value", attribute.value as Long)
                    AttributeType.Number -> {
                        if (attribute.value is Int) {
                            attribute.value = (attribute.value as Int).toFloat()
                        }
                        point.addField("value", attribute.value as Number)
                    }
                    AttributeType.String -> point.addField("value", attribute.value as String)
                    else -> {
                    }
                }
                //if batch enabled, save point in set, either send it
                writeApi.writePoint(influxProperties.database, "autogen", point)

            } catch (e: ClassCastException) {
                log.error("The attribute $attributeId has been updated with wrong data type")
            }
        }
    }

    override fun attributeSet(attributeId: String, attribute: Attribute) {
        attributeUpdated(attributeId, attribute)
    }

    override fun attributeDidSet(attributeId: String, attribute: Attribute) {

    }
}
