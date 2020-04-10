package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointPermission
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class UserEndpointPermissionRepositoryTest {
    @Autowired
    private lateinit var userEndpointPermissionRepository: UserEndpointPermissionRepository

    @Before
    fun setup() {
        userEndpointPermissionRepository.deleteAll()
    }

    @Test
    fun addPermissionOWN() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("sepp.blatter", endpointUUID, EndpointPermission.OWN))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("sepp.blatter", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("sepp.blatter", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun addPermissionGRANT() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("hugh.grant", endpointUUID, EndpointPermission.GRANT))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("hugh.grant", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("hugh.grant", endpointUUID)).orElseThrow().permission == EndpointPermission.GRANT)
    }

    @Test
    fun addPermissionCONFIGURE() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("max.pain", endpointUUID, EndpointPermission.CONFIGURE))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("max.pain", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("max.pain", endpointUUID)).orElseThrow().permission == EndpointPermission.CONFIGURE)
    }

    @Test
    fun addPermissionWRITE() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("peter.alexander", endpointUUID, EndpointPermission.WRITE))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("peter.alexander", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("peter.alexander", endpointUUID)).orElseThrow().permission == EndpointPermission.WRITE)
    }

    @Test
    fun addPermissionREAD() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("asterix", endpointUUID, EndpointPermission.READ))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("asterix", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("asterix", endpointUUID)).orElseThrow().permission == EndpointPermission.READ)
    }

    @Test
    fun addPermissionBROWSE() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("kurt.felix", endpointUUID, EndpointPermission.BROWSE))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("kurt.felix", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("kurt.felix", endpointUUID)).orElseThrow().permission == EndpointPermission.BROWSE)
    }

    @Test
    fun addPermissionACCESS() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("bob", endpointUUID, EndpointPermission.ACCESS))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("bob", endpointUUID)))
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("bob", endpointUUID)).orElseThrow().permission == EndpointPermission.ACCESS)
    }

    @Test
    fun updatePermission() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("franz.beckenbauer", endpointUUID, EndpointPermission.WRITE))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("franz.beckenbauer", endpointUUID)))
        assert(userEndpointPermissionRepository.count() == 1L)
        userEndpointPermissionRepository.save(UserEndpointPermission("franz.beckenbauer", endpointUUID, EndpointPermission.OWN))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("franz.beckenbauer", endpointUUID)))
        assert(userEndpointPermissionRepository.count() == 1L)
        assert(userEndpointPermissionRepository.findById(UserEndpointPermission.Key("franz.beckenbauer", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun deletePermission() {
        val endpointUUID = UUID.randomUUID()

        userEndpointPermissionRepository.save(UserEndpointPermission("hansi.hinterseher", endpointUUID, EndpointPermission.CONFIGURE))

        assert(userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(userEndpointPermissionRepository.count() == 1L)
        userEndpointPermissionRepository.deleteById(UserEndpointPermission.Key("hansi.hinterseher", endpointUUID))

        assert(!userEndpointPermissionRepository.existsById(UserEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(userEndpointPermissionRepository.count() == 0L)
    }
}
