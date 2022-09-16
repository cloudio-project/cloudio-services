package ch.hevs.cloudio.cloud.security

import io.jsonwebtoken.*
import org.apache.juli.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class AccessTokenManager {
    private val log = LogFactory.getLog(AccessTokenManager::class.java)

    @Value("\${cloudio.jwt.secret:#{null}}")
    private var secretKey: String? = null

    @Value("\${cloudio.jwt.expirationDuration:#{24 * 60 * 60 * 1000}}")
    private var tokenExpirationDuration: Long = 24 * 60 * 60  * 1000

    @PostConstruct
    private fun generateSecretKeyIfMissing() {
        if (secretKey == null) {
            log.error("JWT token secret key (cloudio.jwt.secret) is missing, a key will be generated. Tokens will only be valid for this node and not across restarts.")
            secretKey = ByteArray(64).let {
                Random(System.currentTimeMillis()).nextBytes(it)
                Base64.getEncoder().encodeToString(it)
            }
        }
    }

    fun generate(user: CloudioUserDetails): String = Jwts.builder()
        .setSubject(user.id.toString())
        .setIssuer("cloud.iO")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + tokenExpirationDuration))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact()

    fun validate(token: String): Long? {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject.toLong()
        } catch (exception: ExpiredJwtException) {
            log.warn("JWT expired", exception)
        } catch (exception: IllegalArgumentException) {
            log.error("Token is null, empty or only whitespace", exception)
        } catch (exception: MalformedJwtException) {
            log.error("JWT is invalid", exception)
        } catch (exception: UnsupportedJwtException) {
            log.error("JWT is not supported", exception)
        } catch (exception: SignatureException) {
            log.error("Signature validation failed", exception)
        }

        return null
    }
}