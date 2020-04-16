package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.model.Endpoint
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_endpoint")
data class Endpoint(
        @Id
        @GeneratedValue
        val uuid: UUID = UUID(0, 0),

        @Column(length = 1024, nullable = false)
        var friendlyName: String = "Unnamed endpoint",

        var blocked: Boolean = false,

        var online: Boolean = false,

        @Embedded
        val configuration: EndpointConfiguration = EndpointConfiguration(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        val dataModel: ch.hevs.cloudio.cloud.model.Endpoint = Endpoint(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        val metaData: MutableMap<String, Any> = mutableMapOf()
) : BinaryJsonContainingEntity()
