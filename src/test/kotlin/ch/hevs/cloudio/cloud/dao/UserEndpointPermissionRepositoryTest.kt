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
class UserEndpointPermissionRepositoryTest {
    @Autowired
    private lateinit var userEndpointPermissionRepository: UserEndpointPermissionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    private var userID: Long = 0

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userEndpointPermissionRepository.deleteAll()
        userRepository.deleteAll()
        userID = userRepository.save(User(userName = "TestUser")).id
    }

    private fun transaction(block: () -> Unit) = transactionTemplate!!.executeWithoutResult {
        block()
    }

    // TODO: Add permissions using user repository for all scenarios.
    // TODO: Add permission to non-existent user.

    @Test
    fun addPermissionOWN() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.OWN
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.OWN)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.OWN)
                }
            }
        }
    }

    @Test
    fun addPermissionGRANT() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.GRANT
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.GRANT)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.GRANT)
                }
            }
        }
    }

    @Test
    fun addPermissionCONFIGURE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.CONFIGURE
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.CONFIGURE)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.CONFIGURE)
                }
            }
        }
    }

    @Test
    fun addPermissionWRITE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.WRITE
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.WRITE)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.WRITE)
                }
            }
        }
    }

    @Test
    fun addPermissionREAD() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.READ
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.READ)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.READ)
                }
            }
        }
    }

    @Test
    fun addPermissionBROWSE() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.BROWSE
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.BROWSE)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.BROWSE)
                }
            }
        }
    }

    @Test
    fun addPermissionACCESS() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.ACCESS
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.ACCESS)

            val user = userRepository.findById(userID).orElseThrow()
            user.let {
                assert(user.permissions.count() == 1)
                user.permissions.first().let {
                    assert(it.endpointUUID == endpointUUID)
                    assert(it.permission == EndpointPermission.ACCESS)
                }
            }
        }
    }

    @Test
    fun updatePermission() {
        val endpointUUID = UUID.randomUUID()
        var id: Long = 0

        transaction {
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.WRITE
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.count() == 1L)

            val user = userRepository.findById(userID).orElseThrow()
            assert(user.permissions.count() == 1)
            user.permissions.first().let {
                assert(it.endpointUUID == endpointUUID)
                assert(it.permission == EndpointPermission.WRITE)
            }
        }

        transaction {
            userEndpointPermissionRepository.save(UserEndpointPermission(id, userID, endpointUUID, EndpointPermission.OWN))
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.count() == 1L)
            assert(userEndpointPermissionRepository.findByUserIDAndEndpointUUID(userID, endpointUUID).orElseThrow().permission == EndpointPermission.OWN)

            val user = userRepository.findById(userID).orElseThrow()
            assert(user.permissions.count() == 1)
            user.permissions.first().let {
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
            id = userEndpointPermissionRepository.save(UserEndpointPermission(
                    0,
                    userID,
                    endpointUUID,
                    EndpointPermission.CONFIGURE
            )).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.count() == 1L)

            val user = userRepository.findById(userID).orElseThrow()
            assert(user.permissions.count() == 1)
        }

        transaction {
            userEndpointPermissionRepository.deleteById(id)
        }


        transaction {
            assert(!userEndpointPermissionRepository.existsById(id))
            assert(userEndpointPermissionRepository.count() == 0L)

            val user = userRepository.findById(userID).orElseThrow()
            assert(user.permissions.count() == 0)
        }
    }

    @Test
    fun deleteUserDeletesPermissions() {
        val endpointUUID1 = UUID.randomUUID()
        val endpointUUID2 = UUID.randomUUID()
        var id1: Long = 0
        var id2: Long = 0

        transaction {
            id1 = userEndpointPermissionRepository.save(UserEndpointPermission(0, userID, endpointUUID1, EndpointPermission.CONFIGURE)).id
            id2 = userEndpointPermissionRepository.save(UserEndpointPermission(0, userID, endpointUUID2, EndpointPermission.READ)).id
        }

        transaction {
            assert(userEndpointPermissionRepository.existsById(id1))
            assert(userEndpointPermissionRepository.existsById(id2))
            assert(userEndpointPermissionRepository.count() == 2L)

            val userGroup = userRepository.findById(userID).orElseThrow()
            assert(userGroup.permissions.count() == 2)
        }

        transaction {
            userRepository.deleteById(userID)
        }

        transaction {
            assert(!userEndpointPermissionRepository.existsById(id1))
            assert(!userEndpointPermissionRepository.existsById(id2))
            assert(userEndpointPermissionRepository.count() == 0L)
        }
    }
}
