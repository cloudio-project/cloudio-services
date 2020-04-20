package ch.hevs.cloudio.cloud.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class CBORSerializationFormat: AbstractJacksonSerializationFormat(Jackson2ObjectMapperBuilder.cbor().build<ObjectMapper>()) {
    override fun detect(data: ByteArray) = false // TODO
    override fun identifier() = "CBOR"
}
