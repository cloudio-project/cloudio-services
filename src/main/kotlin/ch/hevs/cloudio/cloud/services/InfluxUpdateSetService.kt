package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractUpdateSetService
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeType
import org.apache.commons.logging.LogFactory
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct


@Service
@Profile("update-influx", "default")
class InfluxUpdateSetService(val env: Environment, val influx: InfluxDB) : AbstractUpdateSetService() {

    companion object {
        private val log = LogFactory.getLog(InfluxUpdateSetService::class.java)
    }
    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    val influxBatchTimeMs : Int by lazy { env.getProperty("cloudio.influxBatchIntervallMs", "10000").toInt() }
    val influxBatchSize : Int by lazy { env.getProperty("cloudio.influxBatchSize", "2000").toInt() }


    @PostConstruct
    fun initialize()
    {
        // Create database if needed
        if(influx.query(Query("SHOW DATABASES","")).toString().indexOf(database)==-1)
            influx.query(Query("CREATE DATABASE $database",""))

        influx.enableBatch(BatchOptions.DEFAULTS.actions(influxBatchSize).flushDuration(influxBatchTimeMs))
    }

    override fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String) {
        // create the influxDB point
        val point = Point
                .measurement(attributeId)
                .time((attribute.timestamp *(1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .tag("constraint", attribute.constraint.toString())
                .tag("type", attribute.type.toString())

        try {
            // complete the point depending on attribute type
            when (attribute.type) {
                AttributeType.Boolean -> point.addField("value", attribute.value as Boolean)
                AttributeType.Integer -> point.addField("value", attribute.value as Int)
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
            val myPoint =  point.build()

            //if batch enabled, save point in set, either send it
            influx.write(database, "autogen",myPoint)

        } catch (e: ClassCastException){
            log.error("The attribute $attributeId has been updated with wrong data type")
        }
    }
}
