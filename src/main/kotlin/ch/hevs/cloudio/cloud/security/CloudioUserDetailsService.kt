package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.dao.UserRepository
import org.apache.juli.logging.LogFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Service
class CloudioUserDetailsService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) : UserDetailsService {
    private val log = LogFactory.getLog(CloudioUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(userName: String): UserDetails = userRepository.findByUserName(userName).orElseThrow {
        UsernameNotFoundException("User \"$userName\"not found.")
    }.let { user ->
        CloudioUserDetails(user.id, user.userName, user.password, user.banned, user.authorities.map(Authority::name).map { authority ->
        SimpleGrantedAuthority(authority)
    }, user.groupMemberships.map { it.id }) }


    @PostConstruct
    @Transactional
    fun createAdminUserIfUserRepoIsEmpty() {
        if (userRepository.count() == 0L) {
            val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val password = List(16) { alphabet.random() }.joinToString("")
            log.warn("*** User repository is empty - creating default admin user 'admin' with password '$password' ***")
            userRepository.save(User(
                    userName = "admin",
                    emailAddress = EmailAddress("root@localhost"),
                    password = passwordEncoder.encode(password),
                    authorities = mutableSetOf(
                            Authority.BROKER_ACCESS, Authority.BROKER_MANAGEMENT_ADMINISTRATOR,
                            Authority.HTTP_ACCESS, Authority.HTTP_ADMIN),
                    metaData = mutableMapOf(
                            "createdBy" to "Bootstrap process"
                    )
            ))
        }
    }
}
