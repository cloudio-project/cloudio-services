package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractUpdateSetService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeType
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.apache.commons.logging.LogFactory
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
@Profile("update-influx", "default")
class InfluxUpdateSetService(
        private val influx: InfluxDB,
        serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties
) : AbstractUpdateSetService(serializationFormats) {
    private val log = LogFactory.getLog(InfluxUpdateSetService::class.java)

    @PostConstruct
    fun initialize() {
        // Create database if needed
        if (influx.query(Query("SHOW DATABASES", "")).results.firstOrNull()?.series?.firstOrNull()?.values?.none { it.firstOrNull() == influxProperties.database} != false)
            influx.query(Query("CREATE DATABASE ${influxProperties.database}", ""))

        influx.enableBatch(BatchOptions.DEFAULTS.actions(influxProperties.batchSize).flushDuration(influxProperties.batchIntervalMs))
    }

    override fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String) {
        if (attribute.timestamp != null) {
            // create the influxDB point
            val point = Point
                    .measurement(attributeId)
                    .time((attribute.timestamp!! * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                    .tag("constraint", attribute.constraint.toString())
                    .tag("type", attribute.type.toString())

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
                //write the actual point in influx
                val myPoint = point.build()

                //if batch enabled, save point in set, either send it
                influx.write(influxProperties.database, "autogen", myPoint)

            } catch (e: ClassCastException) {
                log.error("The attribute $attributeId has been updated with wrong data type")
            }
        }
    }
}
