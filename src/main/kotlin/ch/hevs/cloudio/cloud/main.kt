package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.restapi.MongoCustomUserDetailsService
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.DefaultSaslConfig
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.amqp.support.converter.SmartMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableGlobalMethodSecurity(prePostEnabled = true)
class CloudioApplication {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.modulesToInstall(KotlinModule())
    }

    @Bean
    fun messageConverter() = object : SmartMessageConverter {
        private val simple = SimpleMessageConverter()
        private val json = Jackson2JsonMessageConverter().apply {
            jacksonObjectMapper().registerModule(KotlinModule())
        }

        override fun toMessage(obj: Any, messageProperties: MessageProperties) = when (obj) {
            is Message -> simple.toMessage(obj, messageProperties)
            is ByteArray -> simple.toMessage(obj, messageProperties)
            else -> json.toMessage(obj, messageProperties)
        }

        override fun fromMessage(message: Message, conversionHint: Any) = if (message.messageProperties.contentType == MessageProperties.CONTENT_TYPE_JSON) {
            json.fromMessage(message, conversionHint)
        } else {
            simple.fromMessage(message)
        }

        override fun fromMessage(message: Message) = if (message.messageProperties.contentType == MessageProperties.CONTENT_TYPE_JSON) {
            json.fromMessage(message)
        } else {
            simple.fromMessage(message)
        }
    }

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
            http.csrf().disable()
                    .authorizeRequests().anyRequest().hasAuthority(Authority.HTTP_ACCESS.name)
                    .and().httpBasic()
                    .and().sessionManagement().disable()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CloudioApplication>(*args)
}
