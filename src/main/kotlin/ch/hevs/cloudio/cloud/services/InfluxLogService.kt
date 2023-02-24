package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogMessage
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.WriteApiBlocking
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogService(
    private val influx: InfluxDBClient,
    private val influxProperties: CloudioInfluxProperties
) : AbstractLogService(ignoresLogLevel = true) {
    override fun logMessage(endpointUUID: UUID, message: LogMessage) {
        val writeApi: WriteApiBlocking = influx.writeApiBlocking
        writeApi.writePoint(
            influxProperties.database, influxProperties.organization, Point
                .measurement("$endpointUUID.logs")
                .time((message.timestamp * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                .addField("level", message.level.ordinal)
                .addField("message", message.message)
                .addField("loggerName", message.loggerName)
                .addField("logSource", message.logSource)
        )
    }
}
