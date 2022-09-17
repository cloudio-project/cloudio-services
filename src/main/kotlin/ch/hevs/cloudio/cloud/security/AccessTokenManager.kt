package ch.hevs.cloudio.cloud.security

import io.jsonwebtoken.*
import org.apache.juli.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class AccessTokenManager {
    sealed interface ValidationResult
    object InvalidToken: ValidationResult
    class ValidProvisioningToken(val endpointUUID: UUID): ValidationResult
    class ValidUserToken(val userId: Long): ValidationResult
    data class ValidEndpointPermissionToken(val endpointUUID: UUID, val permission: EndpointPermission): ValidationResult
    private val log = LogFactory.getLog(AccessTokenManager::class.java)

    @Value("\${cloudio.jwt.secret:#{null}}")
    private var secretKey: String? = null

    @Value("\${cloudio.jwt.expirationDuration:#{24 * 60 * 60 * 1000}}")
    private var tokenExpirationDuration: Long = 24 * 60 * 60  * 1000

    @PostConstruct
    private fun generateSecretKeyIfMissing() {
        if (secretKey == null) {
            log.error("JWT token secret key (cloudio.jwt.secret) is missing, a key will be generated. Tokens will only be valid for this node and not across restarts!")
            secretKey = ByteArray(64).let {
                Random(System.currentTimeMillis()).nextBytes(it)
                Base64.getEncoder().encodeToString(it)
            }
        }
    }

    fun generateProvisionToken(endpointUUID: UUID, expirationDuration: Long = tokenExpirationDuration): String = Jwts.builder()
        .setSubject("provision")
        .claim("endpoint", endpointUUID.toString())
        .setIssuer("cloud.iO")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + expirationDuration))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact()

    fun generateUserAccessToken(user: CloudioUserDetails): String = Jwts.builder()
        .setSubject("user")
        .claim("uid", user.id.toString())
        .setIssuer("cloud.iO")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + tokenExpirationDuration))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact()

    fun generateEndpointPermissionAccessToken(endpointUUID: UUID, permission: EndpointPermission): String = Jwts.builder()
        .setSubject("endpoint")
        .claim("uuid", endpointUUID.toString())
        .claim("perm", permission)
        .setIssuer("cloud.iO")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + tokenExpirationDuration))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact()

    fun validate(token: String): ValidationResult {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).let { claims ->
                when (claims.body.subject) {
                    "provision" -> ValidProvisioningToken(UUID.fromString(claims.body["endpoint"] as String))
                    "user" -> ValidUserToken((claims.body["uid"] as String).toLong())
                    "endpoint" -> {
                        EndpointPermission.valueOf(claims.body["perm"] as String).let { permission ->
                            when(permission) {
                                EndpointPermission.READ, EndpointPermission.WRITE, EndpointPermission.CONFIGURE ->
                                    ValidEndpointPermissionToken(UUID.fromString(claims.body["uuid"] as String), permission)
                                else -> {
                                    log.warn("Token for endpoint access contains invalid permission")
                                    InvalidToken
                                }
                            }
                        }
                    }
                    else -> {
                        log.warn("Token contains invalid subject: ${claims.body.subject}")
                        InvalidToken
                    }
                }
            }
        } catch (exception: ExpiredJwtException) {
            log.warn("JWT expired")
        } catch (exception: IllegalArgumentException) {
            log.warn("Token is null, empty or only whitespace or endpoint token contains invalid UUID")
        } catch (exception: MalformedJwtException) {
            log.warn("JWT is invalid")
        } catch (exception: UnsupportedJwtException) {
            log.warn("JWT is not supported")
        } catch (exception: SignatureException) {
            log.warn("Signature validation failed")
        } catch (exception: NumberFormatException) {
            log.warn("Invalid user ID in token")
        }

        return InvalidToken
    }
}