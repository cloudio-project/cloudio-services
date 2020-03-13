package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class UserManagementControllerTests {
    @Autowired
    private lateinit var userManagementController: UserManagementController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: UserGroupRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Before
    fun setup() {
        userRepository.deleteAll()
        groupRepository.save(UserGroup(
                userGroupName = "TestGroup"
        ))
        userRepository.save(User(
                userName = "TestUser",
                passwordHash = passwordEncoder.encode("TestUserPassword"),
                authorities = setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                userGroups = setOf("TestGroup"),
                permissions = mapOf(),
                banned = false
        ))
    }

    // Create user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createAndDeleteMinimalUser() {
        userManagementController.postUserByUserName("User1", PostUserBody(
                password = "88888888"
        ))

        val user = userRepository.findByIdOrNull("User1")
        assert(user != null)
        user?.apply {
            assert(userName == "User1")
            assert(passwordEncoder.matches("88888888", passwordHash))
            assert(permissions.isEmpty())
            assert(userGroups.isEmpty())
            assert(authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS))
            assert(!banned)
        }

        userManagementController.deleteUser("User1")
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.deleteUser("Uesr1")
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun recreateExistingUser() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            userManagementController.postUserByUserName("TestUser", PostUserBody(
                    password = "88888888"
            ))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserInNonExistingGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.postUserByUserName("User1", PostUserBody(
                    password = "88888888",
                    groupMemberships = setOf("NonexistingGroup")
            ))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserInExistingGroup() {
        userManagementController.postUserByUserName("User1", PostUserBody(
                password = "88888888",
                groupMemberships = setOf("TestGroup")
        ))

        val user = userRepository.findByIdOrNull("User1")
        assert(user != null)
        user?.apply {
            assert(userName == "User1")
            assert(passwordEncoder.matches("88888888", passwordHash))
            assert(permissions.isEmpty())
            assert(userGroups == setOf("TestGroup"))
            assert(authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS))
            assert(!banned)
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createAdminUser() {
        userManagementController.postUserByUserName("Admin1", PostUserBody(
                password = "1234567812345678",
                authorities = setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN, Authority.BROKER_ACCESS)
        ))

        val user = userRepository.findByIdOrNull("Admin1")
        assert(user != null)
        user?.apply {
            assert(userName == "Admin1")
            assert(passwordEncoder.matches("1234567812345678", passwordHash))
            assert(permissions.isEmpty())
            assert(userGroups.isEmpty())
            assert(authorities == setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN, Authority.BROKER_ACCESS))
            assert(!banned)
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun createUserNotByAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.postUserByUserName("User1", PostUserBody(
                    password = "88888888"
            ))
        }
    }

    // Get user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUser() {
        val user = userManagementController.getUserByUserName("TestUser")
        assert(user.name == "TestUser")
        assert(user.authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS))
        assert(user.permissions.isEmpty())
        assert(user.groupMemberships == setOf("TestGroup"))
        assert(!user.banned)
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getNonExistingUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.getUserByUserName("TestUserNotExist")
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getUserByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.getUserByUserName("TestUser")
        }
    }

    // Delete user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteUser() {
        userManagementController.deleteUser("TestUser")
        assert(userRepository.findAll().count() == 0)
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun deleteUserNonByAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.deleteUser("User1")
        }
    }

    // Modify user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUser() {
        userManagementController.getUserByUserName("TestUser").let {
            it.authorities = setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN)
            it.groupMemberships = emptySet()
            it.permissions = mapOf("349898052345-345-435-345345-435/toto/titi" to PrioritizedPermission(Permission.READ, PermissionPriority.HIGH))
            it.banned = true
            userManagementController.putUserByUserName("TestUser", it)
        }
        userRepository.findById("TestUser").orElseThrow().apply {
            assert(userName == "TestUser")
            assert(authorities == setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN))
            assert(userGroups.isEmpty())
            assert(permissions.count() == 1)
            assert(permissions["349898052345-345-435-345345-435/toto/titi"] == PrioritizedPermission(Permission.READ, PermissionPriority.HIGH))
            assert(banned)
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserAddNonExistingGroup() {
        userManagementController.getUserByUserName("TestUser").let {
            it.authorities = setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN)
            it.groupMemberships = setOf("NonExistingGroup")
            it.permissions = mapOf("349898052345-345-435-345345-435/toto/titi" to PrioritizedPermission(Permission.READ, PermissionPriority.HIGH))
            it.banned = true
            assertThrows<CloudioHttpExceptions.NotFound> {
                userManagementController.putUserByUserName("TestUser", it)
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserAddExistingGroup() {
        userRepository.findById("TestUser").orElseThrow().let {
            it.userGroups = emptySet()
            userRepository.save(it)
        }
        userManagementController.getUserByUserName("TestUser").let {
            it.groupMemberships = setOf("TestGroup")
            userManagementController.putUserByUserName("TestUser", it)
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyNonExistentUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.putUserByUserName("ThisDoesNotExist", UserBody("ThisDoesNotExist", emptyMap(), emptySet(), emptySet(), false))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserWhereUserNamesDoNotMatch() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            userManagementController.putUserByUserName("TestUser", UserBody("ThestUser", emptyMap(), emptySet(), emptySet(), false))
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun modifyUserByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.putUserByUserName("ThisDoesNotExist", UserBody("ThisDoesNotExist", emptyMap(), emptySet(), emptySet(), false))
        }
    }

    // Change password

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun changeUsersPassword() {
        userManagementController.putUserPassword("TestUser", "toto")
        assert(passwordEncoder.matches("toto", userRepository.findByIdOrNull("TestUser")?.passwordHash))
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun changeNonExistentUsersPassword() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.putUserPassword("TestUserThatDoesNotExist", "toto")
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changeUsersPasswordByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.putUserPassword("TestUser", "toto")
        }
        assert(passwordEncoder.matches("TestUserPassword", userRepository.findByIdOrNull("TestUser")?.passwordHash))
    }

    // Get all users

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getAllUsers() {
        val users = userManagementController.getAllUsers()
        assert(users.count() == 1)
        assert(users.first() == "TestUser")
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAllUsersByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.getAllUsers()
        }
    }
}
