package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogParameter
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull

object LogsUtil {

    fun getEndpointLogsRequest(influx: InfluxDB, database: String, logsDefaultRequest: LogsDefaultRequest): QueryResult? {
        val logEntry = logsDefaultRequest.endpointUuid+".logs"
        val number = logsDefaultRequest.dataPointNumber
        return influx.query(Query("SELECT * FROM \"$logEntry\" WHERE time < now() order by time desc limit $number",database))
    }

    fun getEndpointLogsByDateRequest(influx: InfluxDB, database: String, logsDateRequest: LogsDateRequest): QueryResult? {
        val logEntry = logsDateRequest.endpointUuid+".logs"
        val dateStart = logsDateRequest.dateStart
        val dateStop = logsDateRequest.dateStop
        return influx.query(Query("SELECT * FROM \"$logEntry\" WHERE time >= '$dateStart' and time <= '$dateStop'",database))

    }

    fun getEndpointLogsWhereRequest(influx: InfluxDB, database: String, logsWhereRequest: LogsWhereRequest): QueryResult? {
        val logEntry = logsWhereRequest.endpointUuid+".logs"
        val where = logsWhereRequest.where
        return influx.query(Query("SELECT * FROM \"$logEntry\" WHERE $where",database))

    }

    fun setLogsLevel(rabbitTemplate: RabbitTemplate, logsSetRequest: LogsSetRequest){
        val logParameter = LogParameter(logsSetRequest.level.toString())
        rabbitTemplate.convertAndSend("amq.topic",
              "@logsLevelUnRetained." + logsSetRequest.endpointUuid, JsonSerializationFormat.serializeLogParameter(logParameter))

    }

    fun getLogsLevel(endpointEntityRepository: EndpointEntityRepository, logsGetRequest: LogsGetRequest): LogsGetAnswer? {

        val endpointEntity = endpointEntityRepository.findByIdOrNull(logsGetRequest.endpointUuid)
        if(endpointEntity != null){
            return LogsGetAnswer(endpointEntity.logLevel)
        }
        else{
            return null
        }

    }



}