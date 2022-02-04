package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractAttributeService
import ch.hevs.cloudio.cloud.dao.InfluxWriteAPI
import ch.hevs.cloudio.cloud.model.*
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("update-influx", "default")
class InfluxAttributeService(
        private val influx: InfluxWriteAPI
) : AbstractAttributeService() {
    private val log = LogFactory.getLog(InfluxAttributeService::class.java)

    override fun attributeUpdated(id: ModelIdentifier, attribute: Attribute) {
        if (attribute.timestamp != null) {
            Point.measurement(id.toInfluxSeriesName())
                .time((attribute.timestamp!! * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                .addTag("event", "update")
                .addTag("constraint", attribute.constraint.toString())
                .addTag("type", attribute.type.toString())
                .writeValue(id, attribute.value, attribute.type)
        }
    }

    override fun attributeSet(id: ModelIdentifier, attribute: AttributeSetCommand) {
        Point.measurement(id.toInfluxSeriesName())
            .time((attribute.timestamp * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
            .addTag("event", "set")
            .writeValue(id, attribute.value)
    }

    override fun attributeDidSet(id: ModelIdentifier, attribute: AttributeSetStatus) {
        Point.measurement(id.toInfluxSeriesName())
            .time((attribute.timestamp * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
            .addTag("event", "didset")
            .writeValue(id, attribute.value)
    }

    private fun Point.writeValue(id: ModelIdentifier, value: Any?, type: AttributeType? = null) {
        try {
            val effectiveType = type ?: when (value?.javaClass?.kotlin) {
                    Boolean::class -> AttributeType.Boolean
                    Long::class -> AttributeType.Integer
                    Number::class -> AttributeType.Number
                    String::class -> AttributeType.String
                    else -> AttributeType.Invalid
            }
            when (effectiveType) {
                AttributeType.Boolean -> addField("value", value as Boolean)
                AttributeType.Integer -> addField("value", value as Long)
                AttributeType.Number -> {
                    if (value is Int) {
                        addField("value", value.toFloat())
                    } else {
                        addField("value", value as Number)
                    }
                }
                AttributeType.String -> addField("value", value as String)
                else -> {
                    log.error("Invalid datatype.")
                }
            }
            influx.writePoint("${id.endpoint}", this)
        } catch (e: ClassCastException) {
            log.error("The attribute $id has been updated with wrong data type")
        }
    }
}
