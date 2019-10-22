package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.abstractservices.AbstractUpdateService
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioLog
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
@Profile("logs-influx", "default")
class InfluxLogsService(val env: Environment, val influx: InfluxDB){

    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    companion object {
        private val log = LogFactory.getLog(InfluxLogsService::class.java)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@logs.*"])])
    fun handleExecMessage(message: Message) {
        log.info("@logs.*")

        try {
            val data = message.body
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {
                val cloudioLog = CloudioLog()
                JsonSerializationFormat.deserializeCloudioLog(cloudioLog, data)
                if (cloudioLog.timestamp != -1.0) {
                    influx.write(database, "autogen", Point
                            .measurement(message.messageProperties.receivedRoutingKey.split(".")[1] + ".logs")
                            .time((cloudioLog.timestamp * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                            .addField("level", cloudioLog.level.toString())
                            .addField("message", cloudioLog.message)
                            .addField("loggerName", cloudioLog.loggerName)
                            .addField("logSource", cloudioLog.logSource)
                            .build())
                }
            } else {
                log.error("Unrecognized message format in @logs message from " + message.messageProperties.receivedRoutingKey.split(".")[1])
            }


        } catch (exception: Exception) {
            log.error("Exception during @logs message handling:", exception)
        }
    }

}
