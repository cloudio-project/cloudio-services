package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.Before
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserManagementUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    @Before
    fun setup() {

    }

    @Test
    fun userManagementUtilTest() {
        val userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)
        try {
            try {
                //be sure that the user is not in mongodb before we start this test
                UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            } catch (innerE: Exception) {
            }

            //create an User
            UserManagementUtil.createUser(userRepository, userTest)
            try {
                UserManagementUtil.createUser(userRepository, userTest)
                assert(false)   //shouldn't be able to creat twice an user with same username
            } catch (cloudioApiException: CloudioApiException) {
            }

            //test that user is in the user list
            var list = UserManagementUtil.getUserList(userRepository)
            assert(list.userList.contains(userName))

            //test that the user can be retreived
            var user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
            assert(user != null)
            assert(user?.userName == userName)

            //test that the random user cannot be retreived
            var user2 = UserManagementUtil.getUser(userRepository, UserRequest(randomCharacters))
            assert(user2 == null)

            //modify user password
            UserManagementUtil.modifyUserPassword(userRepository, UserPasswordRequest(userName, "password"))
            //test new password
            assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.passwordHash == "password")

            //add authority
            UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(userName, setOf(Authority.BROKER_POLICYMAKER)))
            //test authority
            assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_POLICYMAKER) == true)

            //remove authority
            UserManagementUtil.removeUserAuthority(userRepository, RemoveAuthorityRequest(userName, Authority.BROKER_POLICYMAKER))
            //test authority is removed
            assert((UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_POLICYMAKER)) == false)

            //remove user
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            //user shouldn't exist
            user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
            assert(user == null)
            //user shouldn't be in the list
            list = UserManagementUtil.getUserList(userRepository)
            assert(!list.userList.contains(userName))
        } catch (e: Exception) {
            e.printStackTrace()
            assert(false)
        }
    }


}