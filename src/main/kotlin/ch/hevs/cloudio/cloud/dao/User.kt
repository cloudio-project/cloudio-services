package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.Authority
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "cloudio_user")
data class User(
        @Column(unique = true, nullable = false, updatable = false)
        val userName: String = "",

        var emailAddress: EmailAddress = EmailAddress(),

        @Column(nullable = false)
        var password: String = "",

        @ElementCollection(targetClass = Authority::class, fetch = FetchType.EAGER)
        @JoinTable(name = "cloudio_user_authority")
        @Column(name = "authority", nullable = false, length = 32)
        @Enumerated(EnumType.STRING)
        val authorities: MutableSet<Authority> = Authority.DEFAULT_AUTHORITIES.toMutableSet(),

        @Column(name = "banned", nullable = false)
        var banned: Boolean = false,

        @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
        val groupMemberships: MutableSet<UserGroup> = mutableSetOf(),

        @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name = "user_id")
        val permissions: MutableSet<UserEndpointPermission> = mutableSetOf(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        var metaData: MutableMap<String, Any> = mutableMapOf()
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
