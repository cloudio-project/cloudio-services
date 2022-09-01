package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import ch.hevs.cloudio.cloud.dao.Endpoint
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.dao.ProvisionToken
import ch.hevs.cloudio.cloud.dao.ProvisionTokenRepository
import ch.hevs.cloudio.cloud.extension.*
import ch.hevs.cloudio.cloud.internalservice.certificatemanager.CertificateManagerService
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RestController
@Profile("rest-api")
@Tag(name = "Endpoint Provisioning", description = "Allows users or developers to provision new endpoints into the system.")
@RequestMapping("/api/v1")
class EndpointProvisioningController(
        private val endpointRepository: EndpointRepository,
        private val provisionTokenRepository: ProvisionTokenRepository,
        private val certificateManager: CertificateManagerService
) {
    private val caCertificate by lazy {
        certificateManager.getCACertificate()
    }

    @GetMapping("/ca-certificate", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns the certificate used by the certificate authority of the cloud.iO installation.")
    fun getCaCertificate() = ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/x-x509-user-cert"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"authority.crt\"")
            .body(caCertificate)

    @PostMapping("/endpoints/{uuid}/provisionToken", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Prepare endpoint for provision and returns the token that can be used for provisioning.")
    fun prepareProvisionByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestBody @Parameter(description = "Additional provisioning options.", required = false) endpointProvisioningOptions: EndpointProvisioningOptionsEntity?
    ): String {
        // Get endpoint.
        val endpoint = endpointRepository.findById(uuid).orElseThrow {
            CloudioHttpExceptions.NotFound("Endpoint not found.")
        }

        // Add custom properties.
        endpointProvisioningOptions?.customProperties?.run {
            endpoint.configuration.properties.putAll(this)
        }

        // Remove pending tokens for this endpoint.
        provisionTokenRepository.deleteByEndpointUUID(uuid)

        // Add a token to the provision token database.
        val expires = Date()
        expires.time = System.currentTimeMillis() + 24 * 3600 * 1000
        return provisionTokenRepository.save(ProvisionToken(
                endpointUUID = uuid,
                expires = expires
        )).token
    }

    @GetMapping("/endpoints/{uuid}/provision")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(#uuid,T(ch.hevs.cloudio.cloud.security.EndpointPermission).CONFIGURE)")
    @Operation(summary = "Get endpoint configuration information for a given endpoint.")
    fun provisionByUUID(
        @PathVariable @Parameter(description = "UUID of the endpoint.", required = true) uuid: UUID,
        @RequestParam @Parameter(description = "Provisioning data format.", required = false) endpointProvisionDataFormat: EndpointProvisioningDataFormat?,
        @RequestParam @Parameter(description = "Public key to use for certificate generation in PEM format.") publicKey: String?
    ): ResponseEntity<Any> = endpointRepository.findById(uuid).orElseThrow {
        CloudioHttpExceptions.NotFound("Not found")
    }.getProvisionEntity(endpointProvisionDataFormat, publicKey)


    @GetMapping("/provision/{token}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get endpoint configuration information for a pending provision token.")
    fun provisionByToken(
            @PathVariable @Parameter(description = "Provision token.", required = true) token: String,
            @RequestParam @Parameter(description = "Provisioning data format.", required = false) endpointProvisionDataFormat: EndpointProvisioningDataFormat?,
            @RequestParam @Parameter(description = "Public key to use for certificate generation in PEM format.") publicKey: String?
    ): ResponseEntity<Any> = provisionTokenRepository.findByToken(token).orElseThrow {
        CloudioHttpExceptions.Forbidden("Forbidden")
    }.let {
        endpointRepository.findById(it.endpointUUID).orElseThrow {
            CloudioHttpExceptions.NotFound("Not found")
        }.getProvisionEntity(endpointProvisionDataFormat, publicKey, it)
    }

    private fun Endpoint.getProvisionEntity(
        endpointProvisionDataFormat: EndpointProvisioningDataFormat?,
        publicKey: String?,
        token: ProvisionToken? = null
    ): ResponseEntity<Any> = this.run {
        if (configuration.clientCertificate.isNotEmpty() && publicKey != null) {
            configuration.clientCertificate = ""
            configuration.privateKey = ""
        }

        if (configuration.clientCertificate.isEmpty()) {
            if (publicKey != null) {
                val certificate = certificateManager.generateEndpointCertificateFromPublicKey(uuid, publicKey)
                configuration.clientCertificate = certificate
                configuration.privateKey = ""
            } else {
                val certificateAndKey = certificateManager.generateEndpointKeyAndCertificate(uuid)
                configuration.clientCertificate = certificateAndKey.first
                configuration.privateKey = certificateAndKey.second
            }
        }
        if (configuration.clientCertificate.isEmpty()) {
            throw CloudioHttpExceptions.InternalServerError("Unable to create client authentication certificate.")
        }

        endpointRepository.save(this)
        if (token != null) {
            provisionTokenRepository.delete(token)
        }

        when (endpointProvisionDataFormat ?:  EndpointProvisioningDataFormat.JSON) {
            EndpointProvisioningDataFormat.JSON -> ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/json"))
                .header("Endpoint", uuid.toString())
                .body(EndpointProvisioningConfigurationEntity(
                    endpoint = uuid,
                    properties = configuration.properties,
                    caCertificate = caCertificate,
                    clientCertificate = configuration.clientCertificate,
                    clientPrivateKey = if (configuration.privateKey.isNotEmpty()) configuration.privateKey else null
                ))
            EndpointProvisioningDataFormat.JAR_ARCHIVE -> {
                if (configuration.privateKey.isEmpty()) {
                    CloudioHttpExceptions.BadRequest("Endpoint has no private key.")
                }

                val password = String.generateRandomPassword()
                val output = ByteArrayOutputStream()
                val zip = ZipOutputStream(output)

                // Write properties file.
                zip.putNextEntry(ZipEntry("cloud.io/$uuid.properties"))
                Properties().apply {
                    configuration.properties.forEach { prop ->
                        setProperty(prop.key, prop.value)
                    }
                    setProperty("ch.hevs.cloudio.endpoint.ssl.clientPassword", password)
                    setProperty("ch.hevs.cloudio.endpoint.ssl.authorityPassword", password)
                }.store(zip, "")
                zip.closeEntry()

                // Add certificate authority keystore.
                val caCertificate = caCertificate.toX509Certificate()

                zip.putNextEntry(ZipEntry("cloud.io/authority.jks"))
                zip.writeJKSTruststore(password, caCertificate)
                zip.closeEntry()

                // Add client P12 file.
                val certificate = configuration.clientCertificate.toX509Certificate()
                val privateKey = configuration.privateKey.toPrivateKey()

                zip.putNextEntry(ZipEntry("cloud.io/$uuid.p12"))
                zip.writePKCS12Keystore(password, certificate, privateKey)
                zip.closeEntry()

                zip.flush()
                zip.close()

                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/java-archive"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${this.uuid}.jar\"")
                    .header("Endpoint", this.uuid.toString())
                    .body(output.toByteArray())
            }
        }
    }
}
