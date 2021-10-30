package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogMessage
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogService(
    private val influx: InfluxDB,
    private val influxProperties: CloudioInfluxProperties
) : AbstractLogService(ignoresLogLevel = true) {
    override fun logMessage(endpointUUID: UUID, message: LogMessage) {
        influx.write(
            influxProperties.database, "autogen", Point
                .measurement("$endpointUUID.logs")
                .time((message.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .addField("level", message.level.ordinal)
                .addField("message", message.message)
                .addField("loggerName", message.loggerName)
                .addField("logSource", message.logSource)
                .build()
        )
    }
}
