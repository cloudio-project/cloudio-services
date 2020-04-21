package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogsService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.CloudioLogMessage
import ch.hevs.cloudio.cloud.model.LogParameter
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogsService(
        private val influx: InfluxDB,
        serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties) : AbstractLogsService(serializationFormats) {

    override fun logLevelChange(endpointUuid: String, logParameter: LogParameter) {
        //nothing to do in influx
    }

    override fun newLog(endpointUuid: String, cloudioLogMessage: CloudioLogMessage) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement("$endpointUuid.logs")
                .time((cloudioLogMessage.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .addField("level", cloudioLogMessage.level.toString())
                .addField("message", cloudioLogMessage.message)
                .addField("loggerName", cloudioLogMessage.loggerName)
                .addField("logSource", cloudioLogMessage.logSource)
                .build())
    }
}
