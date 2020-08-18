package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("authentication", "default")
class BrokerSecurityService(private val userDetailsService: CloudioUserDetailsService,
                            private val permissionManager: CloudioPermissionManager,
                            private val endpointRepository: EndpointRepository,
                            private val passwordEncoder: PasswordEncoder,
                            private val rabbitProperties: RabbitProperties) {
    private val log = LogFactory.getLog(BrokerSecurityService::class.java)

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(
                        value = "authentication",
                        autoDelete = "true",
                        arguments = [
                            Argument(name = "x-message-ttl", value = "2000", type = "java.lang.Integer")
                        ]),
                exchange = Exchange(
                        value = "authentication",
                        type = ExchangeTypes.FANOUT,
                        durable = "false",
                        ignoreDeclarationExceptions = "true"
                )
        )
    ])
    fun authenticate(message: Message): Message {
        val action = message.messageProperties.headers["action"]?.toString()
        val id = message.messageProperties.headers["username"].toString()

        val body = when (action) {
            "login" -> {
                val password = message.messageProperties.headers["password"]?.toString()
                if (password != null) {   // Authentication with password --> User.
                    authenticateUser(id, password)
                } else {   // Authentication with certificates --> Endpoint.
                    authenticateEndpoint(id)
                }
            }
            "check_vhost" -> {
                when (val vhost = message.messageProperties.headers["vhost"]?.toString()) {
                    rabbitProperties.virtualHost ?: "/" -> {
                        "allow"
                    }
                    else -> {
                        log.info("Access to virtual host \"$vhost\" refused for user/endpoint \"$id\".")
                        "deny"
                    }
                }
            }
            "check_resource" -> {
                val resource = message.messageProperties.headers["resource"]?.toString()
                val permission = BrokerPermission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                val name = message.messageProperties.headers["name"]?.toString()
                when (resource) {
                    "queue" -> if (permission.value <= BrokerPermission.CONFIGURE.value) {
                        "allow"
                    } else {
                        log.info("Permission $permission to queue \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                    "exchange" -> if (permission.value <= BrokerPermission.WRITE.value && name == "amq.topic") {
                        "allow"
                    } else {
                        log.info("Permission $permission to exchange \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                    else -> {
                        log.info("Permission $permission to $resource \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                }
            }
            "check_topic" -> {
                val permission = BrokerPermission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                val modelIdentifier = ModelIdentifier(message.messageProperties.headers["routing_key"] as String)
                when {
                    !modelIdentifier.valid -> {
                        log.info("Access to topic \"${message.messageProperties.headers["routing_key"] as String}\" refused for user/endpoint - Invalid topic.")
                        "deny"
                    }
                    modelIdentifier.action == ActionIdentifier.NONE -> {
                        log.info("Access to topic \"$modelIdentifier\" refused for user/endpoint - Missing action.")
                        "deny"
                    }
                    modelIdentifier.action == ActionIdentifier.INVALID -> {
                        log.info("Access to topic \"$modelIdentifier\" refused for user/endpoint - Invalid action.")
                        "deny"
                    }
                    modelIdentifier.endpoint.toString() == id -> {
                        if (endpointRepository.findByIdOrNull(modelIdentifier.endpoint)?.banned == false) {
                            "allow"
                        } else {
                            log.info("Access to topic \"$modelIdentifier\" refused for endpoint - Endpoint does not exist or is banned.")
                            "deny"
                        }
                    }
                    else -> try {
                        (userDetailsService.loadUserByUsername(id) as CloudioUserDetails).let { userDetails ->
                            if (modelIdentifier.count() == 0) {
                                if (permissionManager.hasEndpointPermission(userDetails, modelIdentifier.endpoint, permission.toEndpointPermission())) {
                                    "allow"
                                } else {
                                    log.info("Access to topic \"$modelIdentifier\" refused for user - Insufficient permission.")
                                    "deny"
                                }
                            } else {
                                if (permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, permission.toEndpointModelElementPermission())) {
                                    "allow"
                                } else {
                                    log.info("Access to topic \"$modelIdentifier\" refused for user - Insufficient permission.")
                                    "deny"
                                }
                            }
                        }
                    } catch (exception: UsernameNotFoundException) {
                        log.info("Access to topic \"$modelIdentifier\" refused for user - User not found.")
                        "deny"
                    }
                }
            }
            else -> {
                throw RuntimeException("Invalid authentication action \"$action\"")
            }
        }

        return Message(body.toByteArray(), MessageProperties())
    }

    private fun authenticateUser(userName: String, password: String) = try {
        userDetailsService.loadUserByUsername(userName).let { userDetails ->
            when {
                !userDetails.isAccountNonExpired -> {
                    log.info("Broker access refused for user \"$userName\" using password authentication - User is banned.")
                    "refused"
                }
                !userDetails.authorities.contains(SimpleGrantedAuthority(Authority.BROKER_ACCESS.toString())) -> {
                    log.info("Broker access refused for user \"$userName\" using password authentication - User is missing BROKER_ACCESS role.")
                    "refused"
                }
                !passwordEncoder.matches(password, userDetails.password) -> {
                    log.info("Broker access refused for user \"$userName\" using password authentication - Password is wrong.")
                    "refused"
                }
                else -> userDetails.authorities
                        .map(GrantedAuthority::getAuthority)
                        .filter { it.startsWith("BROKER_MANAGEMENT_") }
                        .joinToString(separator = ",") { it.substring(18).toLowerCase() }
            }
        }
    } catch (exception: UsernameNotFoundException) {
        log.info("Broker access refused for user \"$userName\" using password authentication - User does not exist.")
        "refused"
    }

    private fun authenticateEndpoint(uuid: String): String = try {
        endpointRepository.findById(UUID.fromString(uuid)).get().let { endpoint ->
            when {
                endpoint.banned -> {
                    log.info("Broker access refused for endpoint \"$uuid\" using certificate authentication - Endpoint is banned.")
                    "refused"
                }
                else -> "allow"
            }
        }
    } catch (exception: NoSuchElementException) {
        log.info("Broker access refused for endpoint \"$uuid\" using certificate authentication - Endpoint does not exist.")
        "refused"
    } catch (excecption: IllegalArgumentException) {
        log.info("Broker access refused for endpoint \"$uuid\" using certificate authentication - Invalid UUID.")
        "refused"
    }
}
