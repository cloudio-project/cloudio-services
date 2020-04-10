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
class UserGroupEndpointPermissionRepositoryTest {
    @Autowired
    private lateinit var userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository

    @Before
    fun setup() {
        userGroupEndpointPermissionRepository.deleteAll()
    }

    @Test
    fun addPermissionOWN() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group1", endpointUUID, EndpointPermission.OWN))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group1", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group1", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun addPermissionGRANT() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group2", endpointUUID, EndpointPermission.GRANT))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group2", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group2", endpointUUID)).orElseThrow().permission == EndpointPermission.GRANT)
    }

    @Test
    fun addPermissionCONFIGURE() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group3", endpointUUID, EndpointPermission.CONFIGURE))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group3", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group3", endpointUUID)).orElseThrow().permission == EndpointPermission.CONFIGURE)
    }

    @Test
    fun addPermissionWRITE() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group4", endpointUUID, EndpointPermission.WRITE))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group4", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group4", endpointUUID)).orElseThrow().permission == EndpointPermission.WRITE)
    }

    @Test
    fun addPermissionREAD() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group5", endpointUUID, EndpointPermission.READ))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group5", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group5", endpointUUID)).orElseThrow().permission == EndpointPermission.READ)
    }

    @Test
    fun addPermissionBROWSE() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group6", endpointUUID, EndpointPermission.BROWSE))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group6", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group6", endpointUUID)).orElseThrow().permission == EndpointPermission.BROWSE)
    }

    @Test
    fun addPermissionACCESS() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group7", endpointUUID, EndpointPermission.ACCESS))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group7", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group7", endpointUUID)).orElseThrow().permission == EndpointPermission.ACCESS)
    }

    @Test
    fun updatePermission() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group8", endpointUUID, EndpointPermission.WRITE))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group8", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.count() == 1L)
        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("Group8", endpointUUID, EndpointPermission.OWN))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("Group8", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.count() == 1L)
        assert(userGroupEndpointPermissionRepository.findById(UserGroupEndpointPermission.Key("Group8", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun deletePermission() {
        val endpointUUID = UUID.randomUUID()

        userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission("hansi.hinterseher", endpointUUID, EndpointPermission.CONFIGURE))

        assert(userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.count() == 1L)
        userGroupEndpointPermissionRepository.deleteById(UserGroupEndpointPermission.Key("hansi.hinterseher", endpointUUID))

        assert(!userGroupEndpointPermissionRepository.existsById(UserGroupEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(userGroupEndpointPermissionRepository.count() == 0L)
    }
}