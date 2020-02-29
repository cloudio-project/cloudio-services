package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.TestUtil.createEndpointEntity
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.extension.fillAttributesFromInfluxDB
import ch.hevs.cloudio.cloud.extension.fillFromInfluxDB
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.admin.user.PostUserBody
import ch.hevs.cloudio.cloud.restapi.admin.user.UserManagementController
import org.influxdb.InfluxDB
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class EndpointManagementUtilTest {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @Autowired
    private lateinit var endpointEntityRepository: EndpointEntityRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var influx: InfluxDB

    @Autowired
    private lateinit var userManagement: UserManagementController

    val database = "cloudio"

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var endpointParameters: EndpointParameters
    private lateinit var createdEndpoint: EndpointEntity

    @Before
    fun setup() {
        val friendlyName = "PaquitoTheEndpoint"
        endpointParameters = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
        //simulate an @online that populate the endpoint data model
        createdEndpoint = createEndpointEntity(endpointParameters.endpointUuid, endpointParameters.friendlyName)
        endpointEntityRepository.save(createdEndpoint)
    }

    @After
    fun cleanUp() {
        endpointEntityRepository.deleteById(endpointParameters.endpointUuid)
    }

    @Test
    fun createEndpoint() {
        val friendlyName = "TotoTheEndpoint"
        val endpointParameters2 = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
        UUID.fromString(endpointParameters2.endpointUuid)
        assert(friendlyName == endpointParameters2.friendlyName)

        endpointEntityRepository.deleteById(endpointParameters2.endpointUuid)
    }

    @Test
    fun getEndpoint() {
        val endpointEntity = EndpointManagementUtil.getEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))
        //test if retrieved endpoint is the same
        assert(endpointEntity == createdEndpoint)
    }

    @Test
    fun getNode() {
        val node = EndpointManagementUtil.getNode(endpointEntityRepository, NodeRequest("${endpointParameters.endpointUuid}/demoNode"))
        //test if retrieved node is the same
        assert(node == createdEndpoint.endpoint.nodes["demoNode"])
    }

    @Test
    fun getObject() {
        val cloudioObject = EndpointManagementUtil.getObject(endpointEntityRepository, ObjectRequest("${endpointParameters.endpointUuid}/demoNode/demoObject"))
        //test if retrieved object is the same
        assert(cloudioObject == createdEndpoint.endpoint.nodes["demoNode"]!!.objects["demoObject"])
    }

    @Test
    fun getAttribute() {
        val cloudioAttributeSetPoint = EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoSetPoint"))
        //test if retrieved attribute is the same
        assert(cloudioAttributeSetPoint == createdEndpoint.endpoint.nodes["demoNode"]!!.objects["demoObject"]!!.attributes["demoSetPoint"])

        val cloudioAttributeMeasure = EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure"))
        //test if retrieved attribute is the same
        assert(cloudioAttributeMeasure == createdEndpoint.endpoint.nodes["demoNode"]!!.objects["demoObject"]!!.attributes["demoMeasure"])
    }

    @Test
    fun setAttributeSetPoint() {
        val setAttribute = Attribute(AttributeConstraint.SetPoint, AttributeType.Number, 1578992269.000, 11.0)
        EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, AttributeSetRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoSetPoint", setAttribute))
        Thread.sleep(3000) //to be sure @set message is transferred to influxDB (3000ms is default batch time)
        // TODO: This does not work, InfluxDB driver has no time to save data when blocking the thread.

        val endpointEntity = EndpointManagementUtil.getEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))
        endpointEntity!!.fillAttributesFromInfluxDB(influx, database)
        //test if retrieved endpoint is the same
        //assert(endpointEntity.endpoint.nodes["demoNode"]!!.objects["demoObject"]!!.attributes["demoSetPoint"]!!.value == setAttribute.value)

        val node = EndpointManagementUtil.getNode(endpointEntityRepository, NodeRequest("${endpointParameters.endpointUuid}/demoNode"))
        node!!.fillAttributesFromInfluxDB(influx, database, "${endpointParameters.endpointUuid}/demoNode")
        //test if retrieved node is the same
        //assert(node.objects["demoObject"]!!.attributes["demoSetPoint"]!!.value == setAttribute.value)

        val cloudioObject = EndpointManagementUtil.getObject(endpointEntityRepository, ObjectRequest("${endpointParameters.endpointUuid}/demoNode/demoObject"))
        cloudioObject!!.fillAttributesFromInfluxDB(influx, database, "${endpointParameters.endpointUuid}/demoNode/demoObject")
        //test if retrieved object is the same
        //assert(cloudioObject.attributes["demoSetPoint"]!!.value == setAttribute.value)

        val cloudioAttributeSetPoint = EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoSetPoint"))
        cloudioAttributeSetPoint!!.fillFromInfluxDB(influx, database, "${endpointParameters.endpointUuid}/demoNode/demoObject/demoSetPoint")
        //test if retrieved attribute is the same
        //assert(cloudioAttributeSetPoint.value == setAttribute.value)
    }

    @Test
    fun setAttributeNotSetPoint() {
        val setAttribute = Attribute(AttributeConstraint.SetPoint, AttributeType.Number, 1578992269.000, 11.0)
        assertFails { EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, AttributeSetRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure", setAttribute)) }

    }

    @Test
    fun blockEndpoint() {
        EndpointManagementUtil.blockEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))
        assert(EndpointManagementUtil.getEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))!!.blocked)
    }

    @Test
    fun unblockEndpoint() {
        EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))
        assert(!EndpointManagementUtil.getEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))!!.blocked)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getAccessibleAttributes() {
        val userName = TestUtil.generateRandomString(15)
        userManagement.postUserByUserName(userName, PostUserBody(password = "test"))
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic("${endpointParameters.endpointUuid}/#", Permission.OWN, PermissionPriority.HIGHEST))))

        EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, EndpointRequest(endpointParameters.endpointUuid))
        val accessibleAttribute = EndpointManagementUtil.getAccessibleAttributes(userRepository, userGroupRepository, endpointEntityRepository, userName)
        //be sure endpoint is unblocked
        assert(accessibleAttribute.accessibleAttributes["${endpointParameters.endpointUuid}/demoNode/demoObject/demoSetPoint"] == Permission.OWN)
        assert(accessibleAttribute.accessibleAttributes["${endpointParameters.endpointUuid}/demoNode/demoObject/demoMeasure"] == Permission.OWN)
        userManagement.deleteUser(userName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getOwnedEndpoints() {
        val userName = TestUtil.generateRandomString(15)
        userManagement.postUserByUserName(userName, PostUserBody(password = "test"))
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic("${endpointParameters.endpointUuid}/#", Permission.OWN, PermissionPriority.HIGHEST))))

        val ownedEndpoint = EndpointManagementUtil.getOwnedEndpoints(userRepository, userGroupRepository, endpointEntityRepository, userName)
        val endpointParametersAndBlock = EndpointParametersAndBlock(endpointParameters.endpointUuid, endpointParameters.friendlyName, false)
        assert(ownedEndpoint.ownedEndpoints.contains(endpointParametersAndBlock))
        userManagement.deleteUser(userName)
    }

    @Test
    fun randomCharacterEndpointTest() {
        val setAttribute = Attribute(AttributeConstraint.SetPoint, AttributeType.Number, 1578992269.000, 11.0)

        assert(EndpointManagementUtil.getEndpoint(endpointEntityRepository, EndpointRequest(randomCharacters)) == null)

        assert(EndpointManagementUtil.getNode(endpointEntityRepository, NodeRequest("${endpointParameters.endpointUuid}/$randomCharacters")) == null)
        assertFails { EndpointManagementUtil.getNode(endpointEntityRepository, NodeRequest(randomCharacters)) }

        assert(EndpointManagementUtil.getObject(endpointEntityRepository, ObjectRequest("${endpointParameters.endpointUuid}/demoNode/$randomCharacters")) == null)
        assertFails { EndpointManagementUtil.getObject(endpointEntityRepository, ObjectRequest(randomCharacters)) }

        assert(EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/$randomCharacters")) == null)
        assertFails { EndpointManagementUtil.getAttribute(endpointEntityRepository, AttributeRequest(randomCharacters)) }

        assertFails { EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, AttributeSetRequest("${endpointParameters.endpointUuid}/demoNode/demoObject/$randomCharacters", setAttribute)) }
        assertFails { EndpointManagementUtil.setAttribute(rabbitTemplate, endpointEntityRepository, AttributeSetRequest(randomCharacters, setAttribute)) }

        assert(!EndpointManagementUtil.blockEndpoint(endpointEntityRepository, EndpointRequest(randomCharacters)))

        assert(!EndpointManagementUtil.unblockEndpoint(endpointEntityRepository, EndpointRequest(randomCharacters)))

        assertFails { EndpointManagementUtil.getAccessibleAttributes(userRepository, userGroupRepository, endpointEntityRepository, randomCharacters) }

        assertFails { EndpointManagementUtil.getOwnedEndpoints(userRepository, userGroupRepository, endpointEntityRepository, randomCharacters) }

    }
}