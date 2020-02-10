package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import ch.hevs.cloudio.cloud.apiutils.LibraryLanguage
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer
import java.util.*

class GenerateEndpointConfigurationArchiveResponse(
        @JsonSerialize(using = UUIDSerializer::class)
        @JsonDeserialize(using = UUIDDeserializer::class)
        val endpointUUID: UUID? = null,
        val language: LibraryLanguage = LibraryLanguage.INVALID,
        val pkcs12Data: ByteArray = ByteArray(0)
)
