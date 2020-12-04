package ch.hevs.cloudio.cloud.dao

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@RunWith(SpringRunner::class)
@SpringBootTest
class UserGroupRepositoryTests {
    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userGroupRepository.deleteAll()
        userGroupRepository.save(UserGroup(groupName = "TestGroup"))
    }

    private fun transaction(block: () -> Unit) = transactionTemplate!!.executeWithoutResult {
        block()
    }

    @Test
    fun addMinimalUserGroup() {
        transaction {
            userGroupRepository.save(UserGroup(
                    groupName = "MinimalUserGroup"
            ))
        }

        transaction {
            assert(userGroupRepository.count() == 2L)
            val userGroup = userGroupRepository.findByGroupName("MinimalUserGroup").orElseThrow()
            assert(userGroup.groupName == "MinimalUserGroup")
            assert(userGroup.permissions.isEmpty())
            assert(userGroup.metaData.isEmpty())
        }
    }

    @Test
    fun addCompleteUserGroup() {
        transaction {
            userGroupRepository.save(UserGroup(
                    groupName = "CompleteUserGroup",
                    metaData = mutableMapOf(
                            "url" to "https://nothing.here.org",
                            "tags" to listOf("data", "analyst"),
                            "inHouse" to false,
                            "toto" to mapOf("a" to 0, "b" to 1
                            ))
            ))
        }

        transaction {
            assert(userGroupRepository.count() == 2L)
            val userGroup = userGroupRepository.findByGroupName("CompleteUserGroup").orElseThrow()
            assert(userGroup.groupName == "CompleteUserGroup")
            assert(userGroup.permissions.isEmpty())
            assert(userGroup.metaData["url"] == "https://nothing.here.org")
            assert(userGroup.metaData["tags"] == listOf("data", "analyst"))
            assert(userGroup.metaData["inHouse"] == false)
            assert(userGroup.metaData["toto"] == mapOf("a" to 0, "b" to 1))
        }
    }

    @Test
    fun addExistingUserGroup() {
        assertThrows<DataIntegrityViolationException> {
            userGroupRepository.save(UserGroup(
                    groupName = "TestGroup"
            ))
        }
    }

    @Test
    fun modifyUserGroup() {
        transaction {
            val userGroup = userGroupRepository.findByGroupName("TestGroup").orElseThrow()
            userGroup.metaData["test"] = true
            userGroupRepository.save(userGroup)
        }

        transaction {
            assert(userGroupRepository.count() == 1L)
            val userGroup = userGroupRepository.findByGroupName("TestGroup").orElseThrow()
            assert(userGroup.groupName == "TestGroup")
            assert(userGroup.metaData.count() == 1)
            assert(userGroup.metaData["test"] == true)
        }
    }

    @Test
    fun deleteUserGroup() {
        transaction {
            userGroupRepository.deleteByGroupName("TestGroup")
        }

        transaction {
            assert(userGroupRepository.count() == 0L)
        }
    }
}
