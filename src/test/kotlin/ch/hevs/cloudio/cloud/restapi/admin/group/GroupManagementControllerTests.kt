package ch.hevs.cloudio.cloud.restapi.admin.group

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
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

@RunWith(SpringRunner::class)
@SpringBootTest
class GroupManagementControllerTests {
    @Autowired
    private lateinit var groupManagementController: GroupManagementController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: UserGroupRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Before
    fun setup() {
        userRepository.deleteAll()
        groupRepository.deleteAll()
        groupRepository.save(UserGroup(
                userGroupName = "TestGroup"
        ))
        userRepository.save(User(
                userName = "TestUser",
                passwordHash = passwordEncoder.encode("TestUserPassword"),
                authorities = setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                userGroups = setOf("TestGroup"),
                permissions = mutableMapOf(),
                banned = false
        ))
    }

    // Create group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createGroup() {
        groupManagementController.postGroupByGroupName("Group1")

        groupRepository.findById("Group1").orElseThrow().apply {
            assert(userGroupName == "Group1")
            assert(permissions.isEmpty())
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun recreateExistingGroup() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            groupManagementController.postGroupByGroupName("TestGroup")
        }
    }

    @Test
    @WithMockUser("philip.fry", authorities = ["HTTP_ACCESS"])
    fun createGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            groupManagementController.postGroupByGroupName("Group1")
        }
    }

    // Get group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getGroup() {
        val group = groupManagementController.getGroupByGroupName("TestGroup")
        assert(group.name == "TestGroup")
        assert(group.permissions.isEmpty())
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            groupManagementController.getGroupByGroupName("TestGroupThatDoesNotExist")
        }
    }

    @Test
    @WithMockUser("Hanspeter", authorities = ["HTTP_ACCESS"])
    fun getGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            groupManagementController.getGroupByGroupName("TestGroupThatDoesNotExist")
        }
    }

    // Modify group

    @Test
    @WithMockUser("Admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addPermissionToGroup() {
        groupManagementController.getGroupByGroupName("TestGroup").let {
            it.permissions = it.permissions.toMutableMap().apply {
                set("1234567890/toto", PrioritizedPermission(Permission.OWN, PermissionPriority.LOW))
            }
            groupManagementController.putGroupByGroupName("TestGroup", it)
        }

        groupRepository.findById("TestGroup").orElseThrow().apply {
            assert(userGroupName == "TestGroup")
            assert(permissions.count() == 1)
            assert(permissions["1234567890/toto"]?.permission == Permission.OWN)
            assert(permissions["1234567890/toto"]?.priority == PermissionPriority.LOW)
        }
    }

    @Test
    @WithMockUser("Admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addPermissionToNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            groupManagementController.putGroupByGroupName("TestGroup2", GroupBody("TestGroup2"))
        }
    }

    @Test
    @WithMockUser("Admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addPermissionWhereNamesDoNotMatch() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            groupManagementController.putGroupByGroupName("TestGroup", GroupBody("TestGroup2"))
        }
    }

    @Test
    @WithMockUser("sepp.blatter", authorities = ["HTTP_ACCESS"])
    fun addPermissionByNonAdmin() {
        assertThrows<AccessDeniedException> {
            groupManagementController.putGroupByGroupName("TestGroup", GroupBody("TestGroup"))
        }
    }

    // Delete group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteGroup() {
        groupManagementController.deleteGroupByGroupName("TestGroup")

        assert(groupRepository.findAll().count() == 0)
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            groupManagementController.deleteGroupByGroupName("TestGroup222")
        }
    }

    @Test
    @WithMockUser("mac.gyver", authorities = ["HTTP_ACCESS"])
    fun deleteGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            groupManagementController.deleteGroupByGroupName("TestGroup222")
        }
    }

    // Group/User relations

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deletedGroupIsRemovedFromUsers() {
        userRepository.save(User(
                userName = "TestUser2",
                passwordHash = passwordEncoder.encode("TestUserPassword"),
                authorities = setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                userGroups = setOf("TestGroup", "TestGroup2"),
                permissions = mutableMapOf(),
                banned = false
        ))

        groupManagementController.deleteGroupByGroupName("TestGroup")

        userRepository.findAll().forEach {
            assert(!it.userGroups.contains("TestGroup"))
        }
    }

    // Get all groups

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getAllGroups() {
        val groups = groupManagementController.getAllGroups()

        assert(groups.count() == 1)
        assert(groups.first() == "TestGroup")
    }
}
