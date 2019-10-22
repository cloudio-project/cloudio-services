package ch.hevs.cloudio.cloud.apiutils

import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult

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


}