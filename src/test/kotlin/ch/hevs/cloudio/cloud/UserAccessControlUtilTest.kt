package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFails

@SpringBootTest
class UserAccessControlUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    //randomChar to retreive non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    @Test
    fun userAccessControlUtilTest() {
        val userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)
        val userName2 = TestUtil.generateRandomString(15)
        val userTest2 = TestUtil.createUser(userName2)

        val topic = "topic/#"

        try {
            try {
                //be sure that the user is not in mongodb before we start this test
                UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            } catch (innerE: Exception) {
            }
            //create an User
            UserManagementUtil.createUser(userRepository, userTest)

            UserAccessControlUtil.addUserAccessRight(userRepository,
                    UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

            var accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
            //test if added access control exist
            assert(accessRight?.get(topic) != null)
            //test if parameters are correct
            assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
            assert(accessRight.get(topic)!!.permission == Permission.CONFIGURE)
            //test if random access control doesn't exist
            assert(accessRight.get(randomCharacters) == null)

            UserAccessControlUtil.modifyUserAccessRight(userRepository,
                    UserRightRequest(userName, UserRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
            accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
            //test if added access control exist
            assert(accessRight?.get(topic) != null)
            //test if parameters are correct
            assert(accessRight!!.get(topic)!!.priority == PermissionPriority.LOW)
            assert(accessRight.get(topic)!!.permission == Permission.DENY)

            UserAccessControlUtil.removeUserAccessRight(userRepository, UserTopicRequest(userName, topic))
            accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName))
            //test if added access control exist
            assert(accessRight?.get(topic) == null)

            UserAccessControlUtil.addUserAccessRight(userRepository,
                    UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.OWN, PermissionPriority.HIGH))))

            try {
                //be sure that the user is not in mongodb before we start this test
                UserManagementUtil.deleteUser(userRepository, UserRequest(userName2))
            } catch (innerE: Exception) {
            }
            //create an User
            UserManagementUtil.createUser(userRepository, userTest2)
            // give user right from userName to userName2
            UserAccessControlUtil.giveUserAccessRight(userRepository,
                    UserRightRequestList(userName2, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
            accessRight = UserAccessControlUtil.getUserAccessRight(userRepository, UserRequest(userName2))
            //test if parameters are correct
            assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
            assert(accessRight.get(topic)!!.permission == Permission.READ)

            UserAccessControlUtil.modifyUserAccessRight(userRepository,
                    UserRightRequest(userName, UserRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
            // try to give user right from userName to userName2, doesn't have own access modified above
            assertFails {
                UserAccessControlUtil.giveUserAccessRight(userRepository,
                        UserRightRequestList(userName2, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)

            }

        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }

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


        try {
            //remove users
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName2))
        } catch (e: Exception) {
        }
    }
}