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
        val password: String = "",
        val pkcs12Data: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenerateEndpointKeyAndCertificateResponse

        if (endpointUUID != other.endpointUUID) return false
        if (password != other.password) return false
        if (!pkcs12Data.contentEquals(other.pkcs12Data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpointUUID.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + pkcs12Data.contentHashCode()
        return result
    }
}
