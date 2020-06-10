package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.model.*
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

    private lateinit var testEndpointUUID: UUID

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        endpointRepository.deleteAll()
        testEndpointUUID = endpointRepository.save(Endpoint(
                friendlyName = "MyEndpoint"
        )).uuid
    }

    private fun <R> transaction(block: () -> R): R = transactionTemplate!!.execute {
        return@execute block()
    }!!

    @Test
    fun addMinimalEndpoint() {
        val uuid = transaction {
            endpointRepository.save(Endpoint()).uuid
        }

        transaction {
            val endpoint = endpointRepository.findById(uuid).orElseThrow()
            assert(endpoint.uuid == uuid)
            assert(endpoint.friendlyName == "Unnamed endpoint")
            assert(!endpoint.banned)
            assert(!endpoint.online)
            assert(endpoint.dataModel.nodes.isEmpty())
            assert(endpoint.metaData.isEmpty())
            assert(endpoint.configuration.properties.isEmpty())
            assert(endpoint.configuration.logLevel == LogLevel.ERROR)
            assert(endpoint.configuration.clientCertificate.isEmpty())
            assert(endpoint.configuration.privateKey.isEmpty())
        }
    }

    @Test
    fun addCompleteEndpoint() {
        val uuid = transaction {
            endpointRepository.save(Endpoint(
                    friendlyName = "My awesome endpoint",
                    banned = true,
                    online = true,
                    configuration = EndpointConfiguration(
                            properties = mutableMapOf(
                                    "ch.hevs.cloudio.endpoint.hostUri" to "staging.cloudio.hevs.ch",
                                    "ch.hevs.cloudio.endpoint.ssl.protocol" to "TLSv1.2"
                            ),
                            clientCertificate = "BlZmZpY2l0dXIgc2FnaXR0aXMgY29uc2VxdWF0LCB=",
                            privateKey = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQgY29uc2VjdGV0dXIgYWRpcGlzY2luZyBlbGl0IHBlciwgbWF0dGlzIHRlbXB1cyBmaW5pYnVzIGNvbnZh",
                            logLevel = LogLevel.ALL
                    ),
                    dataModel = EndpointDataModel(
                            nodes = mutableMapOf(
                                    "testNode" to Node(
                                            implements = setOf("ExampleInterface"),
                                            objects = mutableMapOf(
                                                    "testObject1" to CloudioObject(
                                                            conforms = "PhysicalMeasure",
                                                            attributes = mutableMapOf(
                                                                    "Unit" to Attribute(
                                                                            constraint = AttributeConstraint.Static,
                                                                            type = AttributeType.String,
                                                                            value = "Degree Celcius"
                                                                    ),
                                                                    "Value" to Attribute(
                                                                            constraint = AttributeConstraint.Measure,
                                                                            type = AttributeType.Number,
                                                                            value = 22.7,
                                                                            timestamp = 1587102037.73
                                                                    )
                                                            )
                                                    ),
                                                    "testObject2" to CloudioObject(
                                                            attributes = mutableMapOf(
                                                                    "Version" to Attribute(
                                                                            constraint = AttributeConstraint.Status,
                                                                            type = AttributeType.String,
                                                                            value = "1.364",
                                                                            timestamp = 1587101037.0
                                                                    )
                                                            )
                                                    )
                                            )
                                    )
                            )
                    ),
                    metaData = mutableMapOf(
                            "maintainer" to "John Doe",
                            "deployed" to "1. May 2019",
                            "modules" to listOf("KNX", "GSM", "Bluetooth"),
                            "location" to mapOf("latitude" to 46.227315, "longitude" to 7.363509)
                    )
            )).uuid
        }

        transaction {
            val endpoint = endpointRepository.findById(uuid).orElseThrow()
            assert(endpoint.uuid == uuid)
            assert(endpoint.friendlyName == "My awesome endpoint")
            assert(endpoint.banned)
            assert(endpoint.online)
            assert(endpoint.online)
            assert(endpoint.configuration.properties.count() == 2)
            assert(endpoint.configuration.properties["ch.hevs.cloudio.endpoint.hostUri"] == "staging.cloudio.hevs.ch")
            assert(endpoint.configuration.properties["ch.hevs.cloudio.endpoint.ssl.protocol"] == "TLSv1.2")
            assert(endpoint.configuration.clientCertificate == "BlZmZpY2l0dXIgc2FnaXR0aXMgY29uc2VxdWF0LCB=")
            assert(endpoint.configuration.privateKey == "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQgY29uc2VjdGV0dXIgYWRpcGlzY2luZyBlbGl0IHBlciwgbWF0dGlzIHRlbXB1cyBmaW5pYnVzIGNvbnZh")
            assert(endpoint.configuration.logLevel == LogLevel.ALL)
            assert(endpoint.dataModel.nodes.contains("testNode"))
            assert(endpoint.dataModel.nodes["testNode"]!!.implements == setOf("ExampleInterface"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects.count() == 2)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects.contains("testObject1"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.conforms == "PhysicalMeasure")
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes.count() == 2)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes.contains("Unit"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Unit"]!!.constraint == AttributeConstraint.Static)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Unit"]!!.type == AttributeType.String)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Unit"]!!.value == "Degree Celcius")
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes.contains("Value"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Value"]!!.constraint == AttributeConstraint.Measure)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Value"]!!.type == AttributeType.Number)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Value"]!!.value == 22.7)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject1"]!!.attributes["Value"]!!.timestamp == 1587102037.73)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects.contains("testObject2"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.conforms == null)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes.count() == 1)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes.contains("Version"))
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes["Version"]!!.constraint == AttributeConstraint.Status)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes["Version"]!!.type == AttributeType.String)
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes["Version"]!!.value == "1.364")
            assert(endpoint.dataModel.nodes["testNode"]!!.objects["testObject2"]!!.attributes["Version"]!!.timestamp == 1587101037.0)
            assert(endpoint.metaData["maintainer"] == "John Doe")
            assert(endpoint.metaData["deployed"] == "1. May 2019")
            assert((endpoint.metaData["modules"]!! as? List<*>)?.count() ?: 0 == 3)
            assert((endpoint.metaData["modules"]!! as? List<*>)?.contains("KNX") ?: false)
            assert((endpoint.metaData["modules"]!! as? List<*>)?.contains("GSM") ?: false)
            assert((endpoint.metaData["modules"]!! as? List<*>)?.contains("Bluetooth") ?: false)
            assert(endpoint.metaData["location"] == mapOf("latitude" to 46.227315, "longitude" to 7.363509))
        }
    }

    @Test
    fun modifyEndpoint() {
        val endpoint = transaction {
            endpointRepository.findById(testEndpointUUID).orElseThrow()
        }

        transaction {
            endpoint.friendlyName = "Changed friendly name"
            endpoint.banned = true
            endpoint.dataModel.nodes["toto"] = Node()
            endpoint.metaData["Version"] = "2.12"

            endpointRepository.save(endpoint)
        }

        transaction {
            endpointRepository.findById(testEndpointUUID).orElseThrow().let {
                assert(it.friendlyName == "Changed friendly name")
                assert(it.banned)
                assert(it.dataModel.nodes.containsKey("toto"))
                assert(it.metaData.contains("Version"))
                assert(it.metaData["Version"] == "2.12")
            }
        }
    }

    @Test
    fun deleteEndpoint() {
        transaction {
            endpointRepository.deleteById(testEndpointUUID)
        }

        transaction {
            assert(endpointRepository.count() == 0L)
        }
    }
}
