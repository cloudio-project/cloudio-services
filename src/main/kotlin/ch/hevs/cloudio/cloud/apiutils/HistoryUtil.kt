package ch.hevs.cloudio.cloud.apiutils

import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult

object HistoryUtil{
    fun getAttributeHistoryRequest(influx: InfluxDB, database: String, historyDefaultRequest: HistoryDefaultRequest): QueryResult? {
        val attributeTopic = historyDefaultRequest.attributeTopic
        val number = historyDefaultRequest.dataPointNumber
        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/",".")}\" WHERE time < now() order by time desc limit $number",database))
    }

    fun getAttributeHistoryByDateRequest(influx: InfluxDB, database: String, historyDateRequest: HistoryDateRequest): QueryResult? {
        val attributeTopic = historyDateRequest.attributeTopic
        val dateStart = historyDateRequest.dateStart
        val dateStop = historyDateRequest.dateStop
        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/",".")}\" WHERE time >= '$dateStart' and time <= '$dateStop'",database))

    }

    fun getAttributeHistoryWhere(influx: InfluxDB, database: String, historyWhereRequest: HistoryWhereRequest): QueryResult? {
        val attributeTopic = historyWhereRequest.attributeTopic
        val where = historyWhereRequest.where
        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/",".")}\" WHERE $where",database))

    }
}