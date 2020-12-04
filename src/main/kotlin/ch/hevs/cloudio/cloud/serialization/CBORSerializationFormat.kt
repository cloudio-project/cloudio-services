package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.serialization.jackson.AbstractJacksonSerializationFormat
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class CBORSerializationFormat: AbstractJacksonSerializationFormat(Jackson2ObjectMapperBuilder.cbor().build()) {
    override fun detect(data: ByteArray) = data.isNotEmpty() && data[0].toInt() and 0b11100000 == 0b10100000
    override fun identifier() = "CBOR"
}
