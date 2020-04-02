package ch.hevs.cloudio.cloud.restapi.endpoint

import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import java.security.Principal
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class EndpointManagementControllerTests {
    @Autowired
    private lateinit var endpointManagementController: EndpointManagementController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: UserGroupRepository

    @Autowired
    private lateinit var endpointEntityRepository: EndpointEntityRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val endpointUUIDs = List(10) { UUID.randomUUID() }

    @Before
    fun setup() {
        userRepository.deleteAll()
        groupRepository.deleteAll()
        endpointEntityRepository.deleteAll()

        endpointUUIDs.subList(0, 8).forEachIndexed { i, it ->
            endpointEntityRepository.save(EndpointEntity(it, "Endpoint $i", blocked = false, online = false))
        }

        groupRepository.save(UserGroup(
                userGroupName = "TestGroup",
                permissions = mutableMapOf(
                        "${endpointUUIDs[0]}/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGH),
                        "${endpointUUIDs[1]}/#" to PrioritizedPermission(Permission.WRITE, PermissionPriority.HIGH),
                        "${endpointUUIDs[2]}/#" to PrioritizedPermission(Permission.READ, PermissionPriority.HIGH)
                )
        ))

        userRepository.save(User(
                userName = "TestUser",
                passwordHash = passwordEncoder.encode("TestUserPassword"),
                authorities = setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                userGroups = setOf("TestGroup"),
                permissions = mutableMapOf(
                        "${endpointUUIDs[3]}/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGH),
                        "${endpointUUIDs[4]}/#" to PrioritizedPermission(Permission.WRITE, PermissionPriority.HIGH),
                        "${endpointUUIDs[5]}/#" to PrioritizedPermission(Permission.READ, PermissionPriority.HIGH)
                ),
                banned = false
        ))
        userRepository.save(User(
                userName = "TestUser2",
                passwordHash = passwordEncoder.encode("TestUser2Password"),
                permissions = mutableMapOf(
                        "${endpointUUIDs[9]}/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGH)
                )
        ))
        userRepository.save(User(
                userName = "admin",
                passwordHash = passwordEncoder.encode("admin"),
                authorities = Authority.DEFAULT_AUTHORITIES + Authority.HTTP_ADMIN
        ))
    }

    // Create endpoint.

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS", "HTTP_ENDPOINT_CREATION"])
    fun createEndpoint() {
        val uuid = endpointManagementController.createEndpointByFriendlyName("MyEndpoint", Principal { "TestUser" })

        assert(endpointEntityRepository.existsById(uuid))
        assert(userRepository.findById("TestUser").orElseThrow().permissions.contains("$uuid/#"))

        endpointEntityRepository.deleteById(uuid)
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun createEndpointByUserWithoutAuthority() {
        assertThrows<AccessDeniedException> {
            endpointManagementController.createEndpointByFriendlyName("MyEndpoint", Principal { "TestUser" })
        }
    }

    @Test
    @WithMockUser(username = "TestUser3", authorities = ["HTTP_ACCESS", "HTTP_ENDPOINT_CREATION"])
    fun createEndpointByNonExistingUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            endpointManagementController.createEndpointByFriendlyName("MyEndpoint", Principal { "TestUser3" })
        }
    }

    @Test
    @WithMockUser(username = "admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createEndpointByAdmin() {
        val uuid = endpointManagementController.createEndpointByFriendlyName("MyEndpoint2", Principal { "admin" })

        assert(endpointEntityRepository.existsById(uuid))
        assert(userRepository.findById("admin").orElseThrow().permissions.contains("$uuid/#"))

        endpointEntityRepository.deleteById(uuid)
    }

    // Get endpoint.

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getEndpointByUUID() {
        (0..5).forEach {
            val endpoint = endpointManagementController.getEndpointByUUID(endpointUUIDs[it], Principal { "TestUser" })
            assert(endpoint.friendlyName == "Endpoint $it")
        }
        (6..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointByUUID(endpointUUIDs[it], Principal { "TestUser" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser2", authorities = ["HTTP_ACCESS"])
    fun getEndpointByUUIDWithoutAccess() {
        (0..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointByUUID(endpointUUIDs[it], Principal { "TestUser2" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getNonExistingEndpointByUUID() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            endpointManagementController.getEndpointByUUID(endpointUUIDs[9], Principal { "TestUser2" })
        }
    }

    // Get friendly name.

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getEndpointFriendlyNameByUUID() {
        (0..5).forEach {
            val friendlyName = endpointManagementController.getEndpointFriendlyNameByUUID(endpointUUIDs[it], Principal { "TestUser" })
            assert(friendlyName == "Endpoint $it")
        }
        (6..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointFriendlyNameByUUID(endpointUUIDs[it], Principal { "TestUser" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser2", authorities = ["HTTP_ACCESS"])
    fun getEndpointFriendlyNameByUUIDWithoutAccess() {
        (0..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointFriendlyNameByUUID(endpointUUIDs[it], Principal { "TestUser2" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getNonExistingEndpointFriendlyNameByUUID() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            endpointManagementController.getEndpointFriendlyNameByUUID(endpointUUIDs[9], Principal { "TestUser2" })
        }
    }

    // Get blocked.

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getEndpointBlockedNameByUUID() {
        (0..5).forEach {
            val blocked = endpointManagementController.getEndpointBlockedByUUID(endpointUUIDs[it], Principal { "TestUser" })
            assert(!blocked)
        }
        (6..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointBlockedByUUID(endpointUUIDs[it], Principal { "TestUser" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser2", authorities = ["HTTP_ACCESS"])
    fun getEndpointBlockedByUUIDWithoutAccess() {
        (0..8).forEach {
            assertThrows<CloudioHttpExceptions.Forbidden> {
                endpointManagementController.getEndpointBlockedByUUID(endpointUUIDs[it], Principal { "TestUser2" })
            }
        }
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun getNonExistingEndpointBlockedByUUID() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            endpointManagementController.getEndpointBlockedByUUID(endpointUUIDs[9], Principal { "TestUser2" })
        }
    }

    // Update endpoint.

    // Delete endpoint.

    // List endpoints.

    @Test
    @WithMockUser(username = "TestUser", authorities = ["HTTP_ACCESS"])
    fun listEndpointsForTestUser() {
        val endpoints = endpointManagementController.getAllOwnedEndpoints(Principal { "TestUser" })

        assert(endpoints.count() == 2)
        listOf(0, 3).forEach { i ->
            assert(endpoints.filter { it.uuid == endpointUUIDs[i] }.count() == 1)
        }
    }

    @Test
    @WithMockUser(username = "TestUser2", authorities = ["HTTP_ACCESS"])
    fun listEndpointsForTestUser2() {
        val endpoints = endpointManagementController.getAllOwnedEndpoints(Principal { "TestUser2" })

        assert(endpoints.count() == 1)
        assert(endpoints.first().uuid == endpointUUIDs[9])
    }
}
