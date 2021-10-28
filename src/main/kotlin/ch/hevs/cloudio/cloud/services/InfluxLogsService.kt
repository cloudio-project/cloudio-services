package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogsService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogMessage
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogsService(
        private val influx: InfluxDB,
        private val influxProperties: CloudioInfluxProperties) : AbstractLogsService() {

    override fun logLevelChanged(endpointUuid: String, logLevel: LogLevel) {
        //nothing to do in influx
    }

    override fun newLog(endpointUuid: String, logMessage: LogMessage) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement("$endpointUuid.logs")
                .time((logMessage.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .addField("level", logMessage.level.ordinal)
                .addField("message", logMessage.message)
                .addField("loggerName", logMessage.loggerName)
                .addField("logSource", logMessage.logSource)
                .build())
    }
}
