package ch.hevs.cloudio.cloud.apiutils

import com.rabbitmq.client.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment


@Configuration
class ConnectionFactoryConfiguration {
    //create a ConnectionFactory with spring configuration of the broker
    @Bean
    fun getConnectionFactory(environment: Environment): ConnectionFactory {
        val connectionFactory = ConnectionFactory()
        connectionFactory.username = environment.getRequiredProperty("spring.rabbitmq.username")
        connectionFactory.password = environment.getRequiredProperty("spring.rabbitmq.password")
        return connectionFactory
    }
}