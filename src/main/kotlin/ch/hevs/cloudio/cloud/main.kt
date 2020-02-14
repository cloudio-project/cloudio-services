package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.restapi.MongoCustomUserDetailsService
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rabbitmq.client.DefaultSaslConfig
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
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

    @Bean
    fun connectionFactory(rabbitProperties: RabbitProperties): ConnectionFactory = CachingConnectionFactory(RabbitConnectionFactoryBean().apply {
        rabbitProperties.host?.let { setHost(it) }
        setPort(rabbitProperties.port)
        rabbitProperties.username?.let { setUsername(it) }
        rabbitProperties.password?.let { setPassword(it) }
        rabbitProperties.virtualHost?.let { setVirtualHost(it) }
        rabbitProperties.requestedHeartbeat?.let { setRequestedHeartbeat(it.seconds.toInt()) }
        rabbitProperties.ssl.let { ssl ->
            if (ssl.determineEnabled()) {
                setUseSSL(true)
                ssl.algorithm?.let { setSslAlgorithm(it) }
                if (ssl.keyStore != null) {
                    setSaslConfig(DefaultSaslConfig.EXTERNAL)
                }
                setKeyStoreType(ssl.keyStoreType)
                setKeyStore(ssl.keyStore)
                setKeyStorePassphrase(ssl.keyStorePassword ?: "")
                setTrustStoreType(ssl.trustStoreType)
                setTrustStore(ssl.trustStore)
                setTrustStorePassphrase(ssl.trustStorePassword)
                isSkipServerCertificateValidation = !ssl.isValidateServerCertificate
                setEnableHostnameVerification(ssl.verifyHostname)
            }
        }
        rabbitProperties.connectionTimeout?.let { setConnectionTimeout(it.seconds.toInt()) }
        afterPropertiesSet()
    }.`object`).apply {
        setAddresses(rabbitProperties.determineAddresses())
        isPublisherReturns = rabbitProperties.isPublisherReturns
        rabbitProperties.publisherConfirmType?.let { setPublisherConfirmType(it) }
        rabbitProperties.cache.channel.let { channel ->
            channel.size?.let { channelCacheSize = it }
            channel.checkoutTimeout?.let { setChannelCheckoutTimeout(it.toMillis()) }
        }
        rabbitProperties.cache.connection.let { connection ->
            connection.mode?.let { cacheMode = it }
            connection.size?.let { connectionCacheSize = it }
        }
    }

    @Bean
    fun webSecurityConfigurerAdapter(customUserDetailsService: MongoCustomUserDetailsService) = object : WebSecurityConfigurerAdapter() {
        override fun configure(auth: AuthenticationManagerBuilder) {
            auth.userDetailsService(customUserDetailsService)
        }

        override fun configure(http: HttpSecurity) {
            /*http.authorizeRequests()
                    .anyRequest().hasAuthority("http_access").and()
                    .httpBasic().and()
                    .sessionManagement().disable()*/
            http.csrf().disable()
                    .authorizeRequests().anyRequest().permitAll()
                    .and().httpBasic()
                    .and().sessionManagement().disable()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CloudioApplication>(*args)
}
