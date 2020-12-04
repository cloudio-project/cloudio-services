package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer
import java.util.*

data class GenerateEndpointKeyAndCertificateRequest(
        @JsonSerialize(using = UUIDSerializer::class)
        @JsonDeserialize(using = UUIDDeserializer::class)
        val endpointUUID: UUID? = null
)
