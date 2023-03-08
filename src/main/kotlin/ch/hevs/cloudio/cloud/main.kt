package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.cors.CorsRepository
import ch.hevs.cloudio.cloud.internalservice.certificatemanager.CertificateManagerService
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioCorsConfigurationSource
import ch.hevs.cloudio.cloud.security.CloudioUserDetails
import ch.hevs.cloudio.cloud.security.CloudioUserDetailsService
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.rabbitmq.client.DefaultSaslConfig
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityScheme
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
import org.springframework.data.domain.AuditorAware
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfigurationSource
import java.net.InetAddress
import java.util.*
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
@OpenAPIDefinition(
    info = Info(
        title = "cloud.iO API",
        version = "v1",
        description = "API Documentation for cloud.iO",
        license = License(name = "MIT", url = "https://opensource.org/licenses/MIT")
    )
)
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)
class CloudioApplication {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() = Jackson2ObjectMapperBuilderCustomizer {
        it.modulesToInstall(KotlinModule.Builder().build())
    }

    @Bean
    fun messageConverter() = object : SmartMessageConverter {
        private val simple = SimpleMessageConverter()
        private val json = Jackson2JsonMessageConverter().apply {
            jacksonObjectMapper().registerModule(KotlinModule.Builder().build())
        }

        override fun toMessage(obj: Any, messageProperties: MessageProperties) = when (obj) {
            is Message -> simple.toMessage(obj, messageProperties)
            is ByteArray -> simple.toMessage(obj, messageProperties)
            else -> json.toMessage(obj, messageProperties)
        }

        override fun fromMessage(message: Message, conversionHint: Any) =
            if (message.messageProperties.contentType == MessageProperties.CONTENT_TYPE_JSON) {
                json.fromMessage(message, conversionHint)
            } else {
                simple.fromMessage(message)
            }

        override fun fromMessage(message: Message) =
            if (message.messageProperties.contentType == MessageProperties.CONTENT_TYPE_JSON) {
                json.fromMessage(message)
            } else {
                simple.fromMessage(message)
            }
    }

    @Bean
    fun connectionFactory(
        rabbitProperties: RabbitProperties,
        certificateManagerService: CertificateManagerService
    ): ConnectionFactory =
        CachingConnectionFactory(object : RabbitConnectionFactoryBean() {
            override fun configureKeyManagers() = if (keyStore != "@generate") {
                super.configureKeyManagers()
            } else {
                KeyManagerFactory.getInstance("SunX509").apply {
                    init(certificateManagerService.clientKeyStore("cloudio_services", keyStoreType), "".toCharArray())
                }.keyManagers
            }

            override fun configureTrustManagers() = if (trustStore != "@generate") {
                super.configureTrustManagers()
            } else {
                TrustManagerFactory.getInstance("SunX509").apply {
                    init(certificateManagerService.trustKeyStore(trustStoreType))
                }.trustManagers
            }
        }.apply {
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
                    setKeyStore(ssl.keyStore ?: "")
                    setKeyStorePassphrase(ssl.keyStorePassword ?: "")
                    setTrustStoreType(ssl.trustStoreType)
                    setTrustStore(ssl.trustStore ?: "")
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
            setConnectionNameStrategy { InetAddress.getLocalHost().hostName }
        }

    @Bean
    fun corsConfigurationSource(repo: CorsRepository): CorsConfigurationSource = CloudioCorsConfigurationSource(repo)

    @Bean
    fun webSecurityConfigurerAdapter(cloudioUserDetailsService: CloudioUserDetailsService) =
        object : WebSecurityConfigurerAdapter() {
            override fun configure(auth: AuthenticationManagerBuilder) {
                auth.userDetailsService(cloudioUserDetailsService)
            }

            override fun configure(http: HttpSecurity) {
                http.cors().and()
                    .csrf().disable()
                    .authorizeRequests().antMatchers(
                        "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
                        "/api/v1/provision/*", "/messageformat/**"
                    ).permitAll()
                    .anyRequest().hasAuthority(Authority.HTTP_ACCESS.name)
                    .and().httpBasic()
                    .and().sessionManagement().disable()
            }
        }

    @Bean
    fun auditorAware() = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated) {
            val userDetails = authentication.principal as? CloudioUserDetails
            if (userDetails != null) {
                Optional.of(userDetails.id)
            } else {
                Optional.empty()
            }
        } else {
            Optional.empty()
        }
    }

    @Bean
    fun influxDBClient(cloudioInfluxProperties: CloudioInfluxProperties): InfluxDBClient? {
        return InfluxDBClientFactory.create(
            cloudioInfluxProperties.url,
            cloudioInfluxProperties.token.toCharArray(),
            cloudioInfluxProperties.organization,
            cloudioInfluxProperties.database
        );
    }
}

fun main(args: Array<String>) {
    runApplication<CloudioApplication>(*args)
}
