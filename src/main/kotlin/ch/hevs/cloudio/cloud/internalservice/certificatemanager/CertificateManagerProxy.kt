package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import ch.hevs.cloudio.cloud.apiutils.LibraryLanguage
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*

@Service
@Lazy
class CertificateManagerProxy(private val rabbitTemplate: RabbitTemplate) {
    init {
        // TODO: It is not clear (to me) it amqpTemplate is a singleton or not. If it is a singleton we should avoid
        //       to change the reply timeout globally.
        rabbitTemplate.setReplyTimeout(15000)
    }

    fun getCACertificate(): String {
        return rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::getCACertificate", "") as String
    }

    fun generateEndpointKeyAndCertificate(endpointUUID: UUID, password: String? = null): Pair<String, ByteArray> {
        val response = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::generateEndpointKeyAndCertificate",
                GenerateEndpointKeyAndCertificateRequest(endpointUUID, password))
                as GenerateEndpointKeyAndCertificateResponse
        if (endpointUUID != response.endpointUUID || (password != null && password != response.password)) {
            throw RuntimeException("UUID or password do not match")
        }
        return Pair(response.password, response.pkcs12Data)
    }

    fun generateEndpointCertificateFromPublicKey(endpointUUID: UUID, publicKeyPEM: String): String {
        val response = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::generateEndpointCertificateFromPublicKey",
                GenerateEndpointCertificateFromPublicKeyRequest(endpointUUID, publicKeyPEM))
                as GenerateEndpointCertificateFromPublicKeyResponse
        if (endpointUUID != response.endpointUUID) {
            throw RuntimeException("UUID does not match")
        }
        return response.certificatePEM
    }

    fun generateEndpointConfigurationArchive(endpointUUID: UUID, language: LibraryLanguage,
                                             properties: Map<String, String> = emptyMap()): ByteArray {
        val response = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::generateEndpointConfigurationArchive",
                GenerateEndpointConfigurationArchiveRequest(endpointUUID, language, properties))
                as GenerateEndpointConfigurationArchiveResponse
        if (endpointUUID != response.endpointUUID || language != response.language) {
            throw RuntimeException("UUID or library type does not match")
        }
        return response.pkcs12Data
    }
}
