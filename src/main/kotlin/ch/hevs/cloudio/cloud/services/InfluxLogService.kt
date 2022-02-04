package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogService
import ch.hevs.cloudio.cloud.dao.InfluxWriteAPI
import ch.hevs.cloudio.cloud.model.LogMessage
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("logs-influx", "default")
class InfluxLogService(
    private val influx: InfluxWriteAPI
) : AbstractLogService(ignoresLogLevel = true) {
    override fun logMessage(endpointUUID: UUID, message: LogMessage) {
        influx.writePoint("$endpointUUID",
            Point.measurement("logs")
                .time((message.timestamp * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                .addTag("level", "${message.level.ordinal}")
                .addTag("logger", message.loggerName)
                .addTag("source", message.logSource)
                .addField("message", message.message)
        )
    }
}
