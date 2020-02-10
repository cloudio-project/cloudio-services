package ch.hevs.cloudio.cloud

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
@ConfigurationPropertiesScan
class CloudioApplication {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.modulesToInstall(KotlinModule())
    }

    @Bean
    fun messageConverter() = Jackson2JsonMessageConverter()
}

fun main(args: Array<String>) {
    runApplication<CloudioApplication>(*args)
}
