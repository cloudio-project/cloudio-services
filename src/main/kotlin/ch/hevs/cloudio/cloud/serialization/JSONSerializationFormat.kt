package ch.hevs.cloudio.cloud.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class JSONSerializationFormat : AbstractJacksonSerializationFormat(Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()) {
    override fun detect(data: ByteArray): Boolean = data.isNotEmpty() && data[0] == '{'.toByte()
    override fun identifier() = "JSON"
}
