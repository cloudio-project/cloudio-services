package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogsService
import ch.hevs.cloudio.cloud.model.CloudioLogMessage
import ch.hevs.cloudio.cloud.model.LogParameter
import org.apache.commons.logging.LogFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogsService(val env: Environment, val influx: InfluxDB) : AbstractLogsService() {


    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    companion object {
        private val log = LogFactory.getLog(InfluxLogsService::class.java)
    }


    override fun logLevelChange(endpointUuid: String, logParameter: LogParameter) {
        //nothing to do in influx
    }

    override fun newLog(endpointUuid: String, cloudioLogMessage: CloudioLogMessage) {
        influx.write(database, "autogen", Point
                .measurement(endpointUuid + ".logs")
                .time((cloudioLogMessage.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .addField("level", cloudioLogMessage.level.toString())
                .addField("message", cloudioLogMessage.message)
                .addField("loggerName", cloudioLogMessage.loggerName)
                .addField("logSource", cloudioLogMessage.logSource)
                .build())

    }
}
