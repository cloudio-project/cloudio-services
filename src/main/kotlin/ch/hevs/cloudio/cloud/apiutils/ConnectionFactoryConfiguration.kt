package ch.hevs.cloudio.cloud.apiutils

import com.rabbitmq.client.DefaultSaslConfig
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ConnectionFactoryConfiguration {
    @Bean
    fun connectionFactory(rabbitProperties: RabbitProperties): org.springframework.amqp.rabbit.connection.ConnectionFactory = CachingConnectionFactory(RabbitConnectionFactoryBean().apply {
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
                setKeyStoreType(ssl.keyStoreType)
                setKeyStore(ssl.keyStore)
                setKeyStorePassphrase(ssl.keyStorePassword)
                if (ssl.keyStore != null) {
                    setSaslConfig(DefaultSaslConfig.EXTERNAL)
                }
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
}

