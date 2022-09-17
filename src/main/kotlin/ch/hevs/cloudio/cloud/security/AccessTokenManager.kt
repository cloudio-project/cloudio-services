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
    class ValidUserToken(val userId: Long): ValidationResult
    class ValidEndpointPermissionToken(val endpointUUID: UUID, val permission: EndpointPermission, val id: String): ValidationResult
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

    fun generateUserAccessToken(user: CloudioUserDetails): String = Jwts.builder()
        .setSubject("user")
        .setId(UUID.randomUUID().toString())
        .claim("uid", user.id.toString())
        .setIssuer("cloud.iO")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + tokenExpirationDuration))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact()

    fun generateEndpointPermissionAccessToken(endpointUUID: UUID, permission: EndpointPermission): String = Jwts.builder()
        .setSubject("endpoint")
        .setId(UUID.randomUUID().toString())
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
                    "user" -> ValidUserToken((claims.body["uid"] as String).toLong())
                    "endpoint" -> {
                        EndpointPermission.valueOf(claims.body["perm"] as String).let { permission ->
                            when(permission) {
                                EndpointPermission.READ, EndpointPermission.WRITE, EndpointPermission.CONFIGURE ->
                                    ValidEndpointPermissionToken(UUID.fromString(claims.body["uuid"] as String), permission, claims.body.id)
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
            log.warn("JWT expired", exception)
        } catch (exception: IllegalArgumentException) {
            log.warn("Token is null, empty or only whitespace or endpoint token contains invalid UUID", exception)
        } catch (exception: MalformedJwtException) {
            log.warn("JWT is invalid", exception)
        } catch (exception: UnsupportedJwtException) {
            log.warn("JWT is not supported", exception)
        } catch (exception: SignatureException) {
            log.warn("Signature validation failed", exception)
        } catch (exception: NumberFormatException) {
            log.warn("Invalid user ID in token.", exception)
        }

        return InvalidToken
    }
}