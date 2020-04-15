package ch.hevs.cloudio.cloud.dao

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "cloudio_user_group")
data class UserGroup(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @Column(unique = true, nullable = false)
        val groupName: String = "",

        @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name = "user_group_id")
        val permissions: MutableSet<UserGroupEndpointPermission> = mutableSetOf(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        val metaData: MutableMap<String, Any> = mutableMapOf()
) : BinaryJsonContainingEntity()
