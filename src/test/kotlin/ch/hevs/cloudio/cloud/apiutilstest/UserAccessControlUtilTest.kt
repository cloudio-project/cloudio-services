package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.admin.user.PostUserBody
import ch.hevs.cloudio.cloud.restapi.admin.user.UserManagementController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class UserAccessControlUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userManagement: UserManagementController

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String
    private lateinit var userName2: String


    @Before
    fun setup() {
        //create Users
        userName = TestUtil.generateRandomString(15)
        userName2 = TestUtil.generateRandomString(15)

        userManagement.postUserByUserName(userName, PostUserBody(password = "test"))
        userManagement.postUserByUserName(userName2, PostUserBody(password = "test"))
    }

    @After
    fun cleanUp() {
        try {
            //remove users
            userManagement.deleteUser(userName)
            userManagement.deleteUser(userName2)
        } catch (e: Exception) {
        }
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addUserAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        val accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
        //test if added access control exist
        assert(accessRight?.get(topic) != null)
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.CONFIGURE)
        //test if random access control doesn't exist
        assert(accessRight.get(randomCharacters) == null)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        UserAccessControlUtil.modifyUserAccessRight(userRepository,
                UserRightRequest(userName, UserRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        val accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
        //test if added access control exist
        assert(accessRight?.get(topic) != null)
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.LOW)
        assert(accessRight.get(topic)!!.permission == Permission.DENY)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun removeUserAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"

        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        UserAccessControlUtil.removeUserAccessRight(userRepository, UserTopicRequest(userName, topic))
        val accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
        //test if added access control exist
        assert(accessRight?.get(topic) == null)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun giveUserAccessRightOwn() {
        val topic = "${TestUtil.generateRandomString(15)}/#"

        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.OWN, PermissionPriority.HIGH))))

        // give user right from userName to userName2
        UserAccessControlUtil.giveUserAccessRight(userRepository,
                UserRightRequestList(userName2, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        val accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName2))
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.READ)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun giveUserAccessRightNotOwn() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))))
        // try to give user right from userName to userName2, doesn't have own access modified above
        assertFails {
            UserAccessControlUtil.giveUserAccessRight(userRepository,
                    UserRightRequestList(userName2, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)

        }
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserAccessTest() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        //shouldn't be able to act on random user
        assert(UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(randomCharacters)) == null)
        assertFails {
            UserAccessControlUtil.addUserAccessRight(userRepository,
                    UserRightRequestList(randomCharacters, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))
        }
        assertFails {
            UserAccessControlUtil.modifyUserAccessRight(userRepository,
                    UserRightRequest(randomCharacters, UserRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        }
        assertFails { UserAccessControlUtil.removeUserAccessRight(userRepository, UserTopicRequest(randomCharacters, topic)) }
        assertFails { UserAccessControlUtil.removeUserAccessRight(userRepository, UserTopicRequest(userName, randomCharacters)) }
        assertFails {
            UserAccessControlUtil.giveUserAccessRight(userRepository,
                    UserRightRequestList(userName2, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), randomCharacters)
        }
    }
}