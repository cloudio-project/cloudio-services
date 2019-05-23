package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.model.Permission
import ch.hevs.cloudio2.cloud.model.User
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class AuthenticationService {

    companion object {
        private val log = LoggerFactory.getLogger(AuthenticationService::class.java)
    }

    var userRepository : UserRepository

    constructor(userRepository : UserRepository)
    {
        this.userRepository = userRepository
    }

    @PostConstruct
    fun initialize()
    {
        val userCount : Long = userRepository.count()
        if (userCount == 0L)
            userRepository.save(User("root", "123456"))
    }

    @RabbitListener(bindings = [QueueBinding(value=Queue("authentication"),
            exchange = Exchange(value = "authentication", type = ExchangeTypes.FANOUT, ignoreDeclarationExceptions = "true"))])
    fun authenticate (message: Message): String
    {
        val action =  message.messageProperties.headers["action"]?.toString()
        val id =  message.messageProperties.headers["username"].toString()

        val user = userRepository.findById(id)

        try {
            return when (action) {
                "login" -> {
                    log.info("login")
                    log.info(id)
                    log.info(user.isPresent.toString())

                    if (user.isPresent && user.get().passwordHash == message.messageProperties.headers["password"]) {
                        ""
                    } else {
                        "refused"
                    }

                }
                "check_vhost" -> {
                    val vhost = message.messageProperties.headers["vhost"]?.toString()
                    when (vhost) {
                        "/" -> "allow"
                        else -> {
                            log.warn("$action $vhost")
                            "deny"
                        }
                    }
                }
                "check_resource" -> {
                    val resource = message.messageProperties.headers["resource"]?.toString()
                    val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())

                    when (resource) {
                        "queue" -> if (permission.value <= Permission.CONFIGURE.value) "allow" else "deny"

                        "exchange" -> if (permission.value <= Permission.WRITE.value) "allow" else "deny"

                        else -> "deny"
                    }
                }
                "check_topic" -> {
                    val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                    val routingKey = (message.messageProperties.headers["routing_key"] as String).split(".")
                    if (!user.isPresent)
                        "deny"
                    else{
                        val endpointPermission = user.get().permissions.getOrDefault(routingKey[1], Permission.DENY)

                        when {
                            endpointPermission == Permission.DENY -> "deny"
                            routingKey[0][0] != '@' -> "deny"
                            endpointPermission.value >= permission.value -> "allow"
                            else -> "deny"
                        }
                    }
                }
                else -> {
                    log.info("Unexpected header action, connection denied")
                    "deny"
                }
            }
        } catch (exception: Exception) {
            log.error("Exception during authentication message handling", exception)
        }
        return "allow"
    }
}