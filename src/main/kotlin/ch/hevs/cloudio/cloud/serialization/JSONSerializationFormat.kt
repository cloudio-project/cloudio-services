package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.serialization.jackson.AbstractJacksonSerializationFormat
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class JSONSerializationFormat : AbstractJacksonSerializationFormat(Jackson2ObjectMapperBuilder.json().build()) {
    override fun detect(data: ByteArray): Boolean = data.isNotEmpty() && data[0] == '{'.toByte()
    override fun identifier() = "JSON"
}
