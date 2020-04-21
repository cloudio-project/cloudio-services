package ch.hevs.cloudio.cloud.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class CBORSerializationFormat: AbstractJacksonSerializationFormat(Jackson2ObjectMapperBuilder.cbor().build<ObjectMapper>()) {
    override fun detect(data: ByteArray) = data.isNotEmpty() && data[0].toInt() and 0x11100000 == 0x10100000
    override fun identifier() = "CBOR"
}
