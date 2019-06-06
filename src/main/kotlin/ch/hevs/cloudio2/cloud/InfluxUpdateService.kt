package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.abstractservices.AbstractUpdateService
import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.model.AttributeType
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Service
class InfluxUpdateService(val env: Environment, val influx: InfluxDB) : AbstractUpdateService() {

    companion object {
        private val log = LoggerFactory.getLogger(InfluxUpdateService::class.java)
    }
    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    @PostConstruct
    fun initialize()
    {
        // Create database if needed
        if(influx.query(Query("SHOW DATABASES","")).toString().indexOf(database)==-1)
            influx.query(Query("CREATE DATABASE $database",""))
    }

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
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
            influx.write(database, "autogen",myPoint)

        } catch (e: ClassCastException){
            log.error("The attribute $attributeId has been updated with wrong data type")
        }
    }

}