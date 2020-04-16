package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.model.LogLevel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class EndpointRepositoryTests {
    @Autowired
    private lateinit var endpointRepository: EndpointRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        endpointRepository.deleteAll()
        endpointRepository.save(Endpoint(
                friendlyName = "MyEndpoint"
        ))
    }

    private fun transaction(block: () -> Unit) = transactionTemplate!!.executeWithoutResult {
        block()
    }

    @Test
    fun addMinimalEndpoint() {
        var uuid = UUID(0, 0)
        transaction {
            uuid = endpointRepository.save(Endpoint()).uuid
        }

        transaction {
            val endpoint = endpointRepository.findById(uuid).orElseThrow()
            assert(endpoint.uuid == uuid)
            assert(endpoint.friendlyName == "Unnamed endpoint")
            assert(!endpoint.blocked)
            assert(!endpoint.online)
            assert(endpoint.dataModel.nodes.isEmpty())
            assert(endpoint.metaData.isEmpty())
            assert(endpoint.configuration.properties.isEmpty())
            assert(endpoint.configuration.logLevel == LogLevel.ERROR)
            assert(endpoint.configuration.clientCertificate.isEmpty())
            assert(endpoint.configuration.privateKey.isEmpty())
        }
    }

    // TODO: More tests...
}
