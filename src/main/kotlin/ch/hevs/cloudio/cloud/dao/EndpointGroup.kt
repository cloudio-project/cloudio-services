package ch.hevs.cloudio.cloud.dao

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "cloudio_endpoint_group")
data class EndpointGroup(
        @Column(unique = true, nullable = false)
        val groupName: String = "",

        @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
        @JoinColumn(name = "endpoint_group_id")
        val permissions: MutableSet<UserEndpointGroupPermission> = mutableSetOf(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        var metaData: MutableMap<String, Any> = mutableMapOf()
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
