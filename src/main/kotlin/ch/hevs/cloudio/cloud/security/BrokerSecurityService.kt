package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
@Profile("authentication", "default")
class BrokerSecurityService(private val userRepository: MONGOUserRepository,
                            private val userGroupRepository: MONGOUserGroupRepository,
                            private val endpointRepository: EndpointRepository,
                            private val passwordEncoder: PasswordEncoder,
                            private val rabbitProperties: RabbitProperties) {

    private val log = LogFactory.getLog(BrokerSecurityService::class.java)
    private val uuidPattern = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()

    @PostConstruct
    fun createAdminUserIfUserRepoIsEmpty() {
        if (userRepository.count() == 0L) {
            val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val password = List(16) { alphabet.random() }.joinToString("")
            log.warn("*** User repository is empty - creating default admin user 'admin' with password '$password' ***")
            userRepository.save(User(
                    userName = "admin",
                    passwordHash = passwordEncoder.encode(password),
                    authorities = setOf(
                            Authority.BROKER_ACCESS, Authority.BROKER_MANAGEMENT_ADMINISTRATOR,
                            Authority.HTTP_ACCESS, Authority.HTTP_ADMIN)
            ))
        }
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue("authentication"),
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
                    val user = userRepository.findById(id)
                    if (user.isPresent &&
                            user.get().authorities.contains(Authority.BROKER_ACCESS) &&
                            passwordEncoder.matches(password, user.get().passwordHash)) {
                        val authorities = user.get().authorities
                                .map(Authority::name)
                                .filter { it.startsWith("BROKER_MANAGEMENT_") }
                                .joinToString(separator = ",") { it.substring(18).toLowerCase() }
                        log.debug("Access granted for user \"$id\" using password (authorities=$authorities).")
                        authorities
                    } else {
                        log.warn("Access refused for user \"$id\" (password).")
                        "refused"
                    }
                } else {   // Authentication with certificates --> Endpoint.
                    val endpoint = endpointRepository.findById(UUID.fromString(id))
                    if (endpoint.isPresent && !endpoint.get().banned) {
                        log.debug("Access granted for endpoint \"$id\" using client certificate.")
                        "allow"
                    } else {
                        log.warn("Access refused for endpoint \"$id\" (certificate).")
                        "refused"
                    }
                }
            }
            "check_vhost" -> {
                when (val vhost = message.messageProperties.headers["vhost"]?.toString()) {
                    rabbitProperties.virtualHost ?: "/" -> {
                        log.debug("Access to virtual host \"$vhost\" granted for user/endpoint \"$id\".")
                        "allow"
                    }
                    else -> {
                        log.warn("Access to virtual host \"$vhost\" refused for user/endpoint \"$id\".")
                        "deny"
                    }
                }
            }
            "check_resource" -> {
                val resource = message.messageProperties.headers["resource"]?.toString()
                val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                val name = message.messageProperties.headers["name"]?.toString()
                when (resource) {
                    "queue" -> if (permission.value <= Permission.CONFIGURE.value) {
                        log.debug("Permission $permission to queue \"$name\" granted for user/endpoint \"$id\"")
                        "allow"
                    } else {
                        log.warn("Permission $permission to queue \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                    "exchange" -> if (permission.value <= Permission.WRITE.value) {
                        log.debug("Permission $permission to exchange \"$name\" granted for user/endpoint \"$id\"")
                        "allow"
                    } else {
                        log.warn("Permission $permission to exchange \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                    else -> {
                        log.warn("Permission $permission to $resource \"$name\" refused for user/endpoint \"$id\"")
                        "deny"
                    }
                }
            }
            "check_topic" -> {
                val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                val routingKey = (message.messageProperties.headers["routing_key"] as String).split(".")
                if (routingKey.size < 2) {
                    log.warn("Permission to topic refused - topic is too short.")
                    "deny"
                } else {
                    when (uuidPattern.matches(id)) {
                        true -> {
                            if (id == routingKey[1] && endpointRepository.existsById(UUID.fromString(id))) {
                                if (!endpointRepository.findByIdOrNull(UUID.fromString(id))!!.banned) {
                                    log.debug("Access to topic $routingKey granted for endpoint $id")
                                    "allow"
                                } else {
                                    log.warn("Access to topic $routingKey refused for endpoint $id")
                                    "deny"
                                }
                            } else {
                                log.warn("Access to topic $routingKey refused for endpoint $id")
                                "deny"
                            }
                        }
                        false -> {
                            val permissionMap = PermissionUtils
                                    .permissionFromUserAndGroup(id, userRepository, userGroupRepository)

                            val topicFilter = routingKey.drop(1) // Drop the verb @...

                            // Check if there is permission linked to topic.
                            val endpointPermission = PermissionUtils.getHigherPriorityPermission(permissionMap, topicFilter)

                            when {
                                endpointPermission == Permission.DENY -> {
                                    log.warn("Access to topic $routingKey refused for user $id")
                                    "deny"
                                }
                                routingKey[0][0] != '@' -> {
                                    log.warn("Access to topic $routingKey refused for user $id - invalid topic")
                                    "deny"
                                }
                                !endpointRepository.existsById(UUID.fromString(routingKey[1])) -> {
                                    log.warn("Access to topic $routingKey refused for user $id - endpoint does not exist")
                                    "deny"
                                }
                                endpointRepository.findByIdOrNull(UUID.fromString(routingKey[1]))!!.banned -> {
                                    log.warn("Access to topic $routingKey refused for user $id - endpoint is blocked")
                                    "deny"
                                }
                                endpointPermission.value >= permission.value -> {
                                    log.debug("Access to topic $routingKey granted for user $id")
                                    "allow"
                                }
                                else -> {
                                    log.warn("Access to topic $routingKey refused for user $id - unknown error")
                                    "deny"
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                log.error("Unexpected header action, connection denied")
                throw RuntimeException("Invalid authentication action")
            }
        }

        return Message(body.toByteArray(), MessageProperties())
    }
}
