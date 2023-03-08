package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.*
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.WriteApiBlocking
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(
    private val influx: InfluxDBClient,
    private val influxProperties: CloudioInfluxProperties
) : AbstractLifecycleService() {
    private val log = LogFactory.getLog(InfluxLifecycleService::class.java)

    override fun endpointIsOnline(endpointUUID: UUID, dataModel: EndpointDataModel) {
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(
            influxProperties.database, influxProperties.organization, Point
                .measurement(endpointUUID.toString())
                .addField("event", "online")
        )
        dataModel.nodes.writeAttributeValues(endpointUUID.toString())
    }

    override fun endpointIsOffline(endpointUUID: UUID) {
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(
            influxProperties.database, influxProperties.organization, Point
                .measurement(endpointUUID.toString())
                .addField("event", "offline")
        )
    }

    override fun nodeAdded(endpointUUID: UUID, nodeName: String, node: Node) {
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(
            influxProperties.database, influxProperties.organization, Point
                .measurement(endpointUUID.toString())
                .addTag("node", nodeName)
                .addField("event", "added")
        )
        node.writeAttributeValues("$endpointUUID.$nodeName")
    }

    override fun nodeRemoved(endpointUUID: UUID, nodeName: String) {
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(
            influxProperties.database, influxProperties.organization, Point
                .measurement(endpointUUID.toString())
                .addTag("node", nodeName)
                .addField("event", "removed")
        )
    }

    private fun Map<String, Node>.writeAttributeValues(endpointId: String) {
        forEach { (name, node) -> node.writeAttributeValues("$endpointId.$name") }
    }

    private fun Node.writeAttributeValues(nodeId: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues("$nodeId.$name") }
    }

    private fun CloudioObject.writeAttributeValues(objectId: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues("$objectId.$name") }
        attributes.forEach { (name, attribute) -> attribute.writeAttributeValue("$objectId.$name") }
    }

    private fun Attribute.writeAttributeValue(attributeId: String) {
        if (timestamp != null && constraint != AttributeConstraint.Static) {

            // create the influxDB point
            val point = Point
                .measurement(attributeId)
                .time((timestamp!! * 1000.0).toLong(), WritePrecision.MS)
                .addTag("constraint", constraint.toString())
                .addTag("type", type.toString())

            try {
                // complete the point depending on attribute type
                when (type) {
                    AttributeType.Boolean -> point.addField("value", value as Boolean)
                    AttributeType.Integer -> point.addField("value", value as Long)
                    AttributeType.Number -> {
                        if (value is Int) {
                            value = (value as Int).toFloat()
                        }
                        point.addField("value", value as Number)
                    }
                    AttributeType.String -> point.addField("value", value as String)
                    else -> {
                    }
                }

                //if batch enabled, save point in set, either send it
                val writeApi: WriteApiBlocking = influx.writeApiBlocking
                writeApi.writePoint(influxProperties.database, influxProperties.organization, point)

            } catch (e: ClassCastException) {
                log.error("The attribute $attributeId has been updated with wrong data type")
            }
        }
    }
}
