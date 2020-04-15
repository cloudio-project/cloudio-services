package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointPermission
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
class UserGroupEndpointPermissionRepositoryTest {
    @Autowired
    private lateinit var userGroupEndpointPermissionRepository: UserGroupEndpointPermissionRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    private var userGroupID: Long = 0

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userGroupEndpointPermissionRepository.deleteAll()
        userGroupRepository.deleteAll()
        userGroupID = userGroupRepository.save(UserGroup(groupName = "TestGroup")).id
    }

    private fun transaction(block: () -> Unit) = transactionTemplate!!.executeWithoutResult {
        block()
    }

    // TODO: Add permissions using user group repository for all scenarios.
    // TODO: Add permission to non-existent user group.

    @Test
    fun addPermissionOWN() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.OWN)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.OWN)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.OWN)
            }
        }
    }

    @Test
    fun addPermissionGRANT() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.GRANT)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.GRANT)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.GRANT)
            }
        }
    }

    @Test
    fun addPermissionCONFIGURE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.CONFIGURE)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.CONFIGURE)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.CONFIGURE)
            }
        }

    }

    @Test
    fun addPermissionWRITE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.WRITE)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.WRITE)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.WRITE)
            }
        }
    }

    @Test
    fun addPermissionREAD() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.READ)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.READ)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.READ)
            }
        }
    }

    @Test
    fun addPermissionBROWSE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.BROWSE)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.BROWSE)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.BROWSE)
            }
        }
    }

    @Test
    fun addPermissionACCESS() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.ACCESS)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.ACCESS)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.ACCESS)
            }
        }
    }

    @Test
    fun updatePermission() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.WRITE)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.count() == 1L)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.WRITE)
            }
        }

        transaction {
            userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(id, userGroupID, endpointUUID, EndpointPermission.OWN))
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.count() == 1L)
            assert(userGroupEndpointPermissionRepository.findByUserGroupIDAndEndpointUUID(userGroupID, endpointUUID).orElseThrow().permission == EndpointPermission.OWN)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
            userGroup.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.OWN)
            }
        }
    }

    @Test
    fun deletePermission() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID, EndpointPermission.CONFIGURE)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.count() == 1L)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 1)
        }

        transaction {
            userGroupEndpointPermissionRepository.deleteById(id)
        }

        transaction {
            assert(!userGroupEndpointPermissionRepository.existsById(id))
            assert(userGroupEndpointPermissionRepository.count() == 0L)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 0)
        }
    }

    @Test
    fun deleteUserGroupDeletesPermissions() {
        val endpointUUID1 = UUID.randomUUID()
        val endpointUUID2 = UUID.randomUUID()
        var id1: Long = 0
        var id2: Long = 0

        transaction {
            id1 = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID1, EndpointPermission.CONFIGURE)).id
            id2 = userGroupEndpointPermissionRepository.save(UserGroupEndpointPermission(0, userGroupID, endpointUUID2, EndpointPermission.READ)).id
        }

        transaction {
            assert(userGroupEndpointPermissionRepository.existsById(id1))
            assert(userGroupEndpointPermissionRepository.existsById(id2))
            assert(userGroupEndpointPermissionRepository.count() == 2L)

            val userGroup = userGroupRepository.findById(userGroupID).orElseThrow()
            assert(userGroup.permissions.count() == 2)
        }

        transaction {
            userGroupRepository.deleteById(userGroupID)
        }

        transaction {
            assert(!userGroupEndpointPermissionRepository.existsById(id1))
            assert(!userGroupEndpointPermissionRepository.existsById(id2))
            assert(userGroupEndpointPermissionRepository.count() == 0L)
        }
    }
}
