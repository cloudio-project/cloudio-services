package ch.hevs.cloudio.cloud.restapi

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class MongoCustomUserDetailsService(private var userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails = when (val user = userRepository.findByIdOrNull(username)) {
        null -> throw UsernameNotFoundException("User \"$username\"not found.")
        else -> if (user.banned) {
            throw DisabledException("User \"$username\" is banned.")
        } else {
            User(user.userName, user.passwordHash, user.authorities.map(Authority::name).filter{ it.startsWith("HTTP_")}.map {
                SimpleGrantedAuthority(it)
            }.toList())
        }
    }
}
