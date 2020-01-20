package ch.hevs.cloudio.cloud.apiutils

import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult

object HistoryUtil {


    @Throws(CloudioApiException::class)
    fun getAttributeHistoryRequest(influx: InfluxDB, database: String, historyDefaultRequest: HistoryDefaultRequest): QueryResult? {
        val attributeTopic = historyDefaultRequest.attributeTopic
        val number = historyDefaultRequest.maxDataPoints
        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/", ".")}\" WHERE time < now() order by time desc limit $number", database))
    }

    @Throws(CloudioApiException::class)
    fun getAttributeHistoryByDateRequest(influx: InfluxDB, database: String, historyDateRequest: HistoryDateRequest): QueryResult? {
        val attributeTopic = historyDateRequest.attributeTopic
        val dateStart = historyDateRequest.dateStart
        val dateStop = historyDateRequest.dateStop

        if(dateStart.contains(";")||dateStop.contains(";"))
            throw CloudioApiException("Unauthorized character in one field of the request")

        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/", ".")}\" WHERE time >= '$dateStart' and time <= '$dateStop'", database))
    }

    @Throws(CloudioApiException::class)
    fun getAttributeHistoryWhere(influx: InfluxDB, database: String, historyWhereRequest: HistoryWhereRequest): QueryResult? {
        val attributeTopic = historyWhereRequest.attributeTopic
        val where = historyWhereRequest.where
        if(where.contains(";"))
            throw CloudioApiException("Unauthorized character in one field of the request")
        return influx.query(Query("SELECT value FROM \"${attributeTopic.replace("/", ".")}\" WHERE $where", database))
    }

    @Throws(CloudioApiException::class)
    fun getAttributeHistoryExpert(influx: InfluxDB, database: String, historyExpertRequest: HistoryExpertRequest): QueryResult? {
        val attributeTopic = historyExpertRequest.attributeTopic
        val dateStart = historyExpertRequest.dateStart
        val dateStop = historyExpertRequest.dateStop
        val aggregation = historyExpertRequest.aggregation.toString()
        val interval = historyExpertRequest.interval
        val fill = historyExpertRequest.fill.value
        val maxDataPoints = historyExpertRequest.maxDataPoints


        if(dateStart.contains(";")||dateStop.contains(";")||interval.contains(";"))
            throw CloudioApiException("Unauthorized character in one field of the request")

        return influx.query(Query("SELECT $aggregation(value) FROM \"${attributeTopic.replace("/", ".")}\" " +
                "WHERE time >= '$dateStart' and time <= '$dateStop' " +
                "GROUP BY TIME($interval) fill($fill) " +
                "limit $maxDataPoints", database))
    }
}