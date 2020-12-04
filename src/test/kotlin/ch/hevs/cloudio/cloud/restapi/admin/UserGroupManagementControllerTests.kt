package ch.hevs.cloudio.cloud.restapi.admin

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.dao.UserGroup
import ch.hevs.cloudio.cloud.dao.UserGroupRepository
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.admin.usergroup.PostUserGroupEntity
import ch.hevs.cloudio.cloud.restapi.admin.usergroup.UserGroupEntity
import ch.hevs.cloudio.cloud.restapi.admin.usergroup.UserGroupManagementController
import ch.hevs.cloudio.cloud.security.Authority
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
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@RunWith(SpringRunner::class)
@SpringBootTest
class UserGroupManagementControllerTests {
    @Autowired
    private lateinit var userGroupManagementController: UserGroupManagementController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: UserGroupRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userRepository.deleteAll()
        groupRepository.deleteAll()
        val userGroup = groupRepository.save(UserGroup(
                groupName = "TestGroup"
        ))
        userRepository.save(User(
                userName = "TestUser",
                password = passwordEncoder.encode("TestUserPassword"),
                authorities = mutableSetOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                groupMemberships = mutableSetOf(userGroup),
                permissions = mutableSetOf(),
                banned = false
        ))
    }

    private fun <R> transaction(block: () -> R): R = transactionTemplate!!.execute {
        return@execute block()
    }!!

    // Create group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createGroup() {
        userGroupManagementController.createGroup(PostUserGroupEntity("Group1"))

        transaction {
            groupRepository.findByGroupName("Group1").orElseThrow().apply {
                assert(groupName == "Group1")
                assert(permissions.isEmpty())
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun recreateExistingGroup() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            userGroupManagementController.createGroup(PostUserGroupEntity("TestGroup"))
        }
    }

    @Test
    @WithMockUser("philip.fry", authorities = ["HTTP_ACCESS"])
    fun createGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userGroupManagementController.createGroup(PostUserGroupEntity("Group1"))
        }
    }

    // Get group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getGroup() {
        val group = userGroupManagementController.getGroupByGroupName("TestGroup")
        assert(group.name == "TestGroup")
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.getGroupByGroupName("TestGroupThatDoesNotExist")
        }
    }

    @Test
    @WithMockUser("Hanspeter", authorities = ["HTTP_ACCESS"])
    fun getGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userGroupManagementController.getGroupByGroupName("TestGroupThatDoesNotExist")
        }
    }

    // Modify group

    @Test
    @WithMockUser("Admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.updateGroupByGroupName("TestGroup2", UserGroupEntity("TestGroup2", mapOf("test" to true)))
        }
    }

    @Test
    @WithMockUser("Admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyWhereNamesDoNotMatch() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            userGroupManagementController.updateGroupByGroupName("TestGroup", UserGroupEntity("TestGroup2", emptyMap()))
        }
    }

    @Test
    @WithMockUser("sepp.blatter", authorities = ["HTTP_ACCESS"])
    fun modifyByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userGroupManagementController.updateGroupByGroupName("TestGroup", UserGroupEntity("TestGroup", mapOf("test" to true)))
        }
    }

    // Delete group

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteGroup() {
        userGroupManagementController.deleteGroupByGroupName("TestGroup")

        assert(groupRepository.findAll().count() == 0)
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteNonExistentGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.deleteGroupByGroupName("TestGroup222")
        }
    }

    @Test
    @WithMockUser("mac.gyver", authorities = ["HTTP_ACCESS"])
    fun deleteGroupByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userGroupManagementController.deleteGroupByGroupName("TestGroup222")
        }
    }

    // Group/User relations

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deletedGroupIsRemovedFromUsers() {
        transaction {
            userRepository.save(User(
                    userName = "TestUser2",
                    password = passwordEncoder.encode("TestUserPassword"),
                    authorities = Authority.DEFAULT_AUTHORITIES.toMutableSet(),
                    groupMemberships = mutableSetOf(groupRepository.findByGroupName("TestGroup").get()),
                    permissions = mutableSetOf(),
                    banned = false
            ))
        }

        userGroupManagementController.deleteGroupByGroupName("TestGroup")

        transaction {
            userRepository.findAll().forEach {
                assert(it.groupMemberships.isEmpty())
            }
        }
    }

    // Get all groups

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getAllGroups() {
        val groups = userGroupManagementController.getAllGroups()

        assert(groups.count() == 1)
        assert(groups.first() == "TestGroup")
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAllGroupsByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userGroupManagementController.getAllGroups()
        }
    }

    // Random group name tests

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserGroupTest() {
        val randomCharacters = TestUtil.generateRandomString(15)

        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.getGroupByGroupName(randomCharacters)
        }

        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.updateGroupByGroupName(randomCharacters, UserGroupEntity(randomCharacters, emptyMap()))
        }

        assertThrows<CloudioHttpExceptions.NotFound> {
            userGroupManagementController.deleteGroupByGroupName(randomCharacters)
        }
    }
}
