package ch.hevs.cloudio.cloud.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CloudioUserDetails(
        val id: Long,
        private val username: String,
        private val password: String,
        private val banned: Boolean,
        private val authorities: List<SimpleGrantedAuthority>,
        val groupMembershipIDs: List<Long>
): UserDetails {
    override fun getUsername() = username
    override fun getPassword() = password
    override fun isAccountNonLocked() = !banned
    override fun getAuthorities()= authorities.toMutableList()

    override fun isEnabled() = true
    override fun isCredentialsNonExpired() = true
    override fun isAccountNonExpired() = true
}
