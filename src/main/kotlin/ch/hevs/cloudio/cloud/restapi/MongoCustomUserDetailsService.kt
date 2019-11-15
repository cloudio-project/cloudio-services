package ch.hevs.cloudio.cloud.restapi

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.apache.commons.logging.LogFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class MongoCustomUserDetailsService (var userRepository: UserRepository) : UserDetailsService {

    companion object {
        private val log = LogFactory.getLog(MongoCustomUserDetailsService::class.java)
    }


    override fun loadUserByUsername(username: String?): UserDetails {

        val user = userRepository.findByIdOrNull(username)
        if(user == null) {
            log.error("User not found")
            throw UsernameNotFoundException("User not found")
        }
        else {
            val authorities = listOf(SimpleGrantedAuthority("user"))

            if(!user.authorities.contains(Authority.HTTP_ACCESS)){
                log.error("$username tried to log in without HTTP_ACCES authority")
                throw CloudioAuthorityException("User don't have http access as authority")
            }

            return org.springframework.security.core.userdetails.User(
                    user.userName, user.passwordHash, authorities
            )
        }
    }
}