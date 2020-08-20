package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(
        private val influx: InfluxDB,
        serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties) : AbstractLifecycleService(serializationFormats) {

    override fun endpointIsOffline(uuid: String) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(uuid)
                .addField("event", "offline")
                .build())
    }

    override fun endpointIsOnline(uuid: String, dataModel: EndpointDataModel) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(uuid)
                .addField("event", "online")
                .build())
    }

    override fun nodeAdded(uuid: String, nodeName: String, node: Node) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(uuid)
                .tag("node", nodeName)
                .addField("event", "added")
                .build())
    }

    override fun nodeRemoved(uuid: String, nodeName: String) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(uuid)
                .tag("node", nodeName)
                .addField("event", "removed")
                .build())
    }
}
