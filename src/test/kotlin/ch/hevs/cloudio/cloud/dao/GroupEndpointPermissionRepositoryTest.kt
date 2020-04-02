package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.dao.groupendpointpermission.GroupEndpointPermission
import ch.hevs.cloudio.cloud.dao.groupendpointpermission.GroupEndpointPermissionRepository
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
class GroupEndpointPermissionRepositoryTest {
    @Autowired
    private lateinit var groupEndpointPermissionRepository: GroupEndpointPermissionRepository

    @Before
    fun setup() {
        groupEndpointPermissionRepository.deleteAll()
    }

    @Test
    fun addPermissionOWN() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group1", endpointUUID, EndpointPermission.OWN))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group1", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group1", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun addPermissionGRANT() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group2", endpointUUID, EndpointPermission.GRANT))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group2", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group2", endpointUUID)).orElseThrow().permission == EndpointPermission.GRANT)
    }

    @Test
    fun addPermissionCONFIGURE() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group3", endpointUUID, EndpointPermission.CONFIGURE))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group3", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group3", endpointUUID)).orElseThrow().permission == EndpointPermission.CONFIGURE)
    }

    @Test
    fun addPermissionWRITE() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group4", endpointUUID, EndpointPermission.WRITE))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group4", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group4", endpointUUID)).orElseThrow().permission == EndpointPermission.WRITE)
    }

    @Test
    fun addPermissionREAD() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group5", endpointUUID, EndpointPermission.READ))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group5", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group5", endpointUUID)).orElseThrow().permission == EndpointPermission.READ)
    }

    @Test
    fun addPermissionBROWSE() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group6", endpointUUID, EndpointPermission.BROWSE))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group6", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group6", endpointUUID)).orElseThrow().permission == EndpointPermission.BROWSE)
    }

    @Test
    fun addPermissionACCESS() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group7", endpointUUID, EndpointPermission.ACCESS))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group7", endpointUUID)))
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group7", endpointUUID)).orElseThrow().permission == EndpointPermission.ACCESS)
    }

    @Test
    fun updatePermission() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group8", endpointUUID, EndpointPermission.WRITE))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group8", endpointUUID)))
        assert(groupEndpointPermissionRepository.count() == 1L)
        groupEndpointPermissionRepository.save(GroupEndpointPermission("Group8", endpointUUID, EndpointPermission.OWN))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("Group8", endpointUUID)))
        assert(groupEndpointPermissionRepository.count() == 1L)
        assert(groupEndpointPermissionRepository.findById(GroupEndpointPermission.Key("Group8", endpointUUID)).orElseThrow().permission == EndpointPermission.OWN)
    }

    @Test
    fun deletePermission() {
        val endpointUUID = UUID.randomUUID()

        groupEndpointPermissionRepository.save(GroupEndpointPermission("hansi.hinterseher", endpointUUID, EndpointPermission.CONFIGURE))

        assert(groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(groupEndpointPermissionRepository.count() == 1L)
        groupEndpointPermissionRepository.deleteById(GroupEndpointPermission.Key("hansi.hinterseher", endpointUUID))

        assert(!groupEndpointPermissionRepository.existsById(GroupEndpointPermission.Key("hansi.hinterseher", endpointUUID)))
        assert(groupEndpointPermissionRepository.count() == 0L)
    }
}