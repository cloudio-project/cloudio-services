package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer
import java.util.*

data class GenerateEndpointKeyAndCertificateResponse(
        @JsonSerialize(using = UUIDSerializer::class)
        @JsonDeserialize(using = UUIDDeserializer::class)
        val endpointUUID: UUID? = null,
        val certificate: String = "",
        val privateKey: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenerateEndpointKeyAndCertificateResponse

        if (endpointUUID != other.endpointUUID) return false
        if (certificate != other.certificate) return false
        if (privateKey != other.privateKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpointUUID.hashCode()
        result = 31 * result + certificate.hashCode()
        result = 31 * result + privateKey.hashCode()
        return result
    }
}
