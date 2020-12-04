package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_endpoint")
data class Endpoint(
        @Column(length = 1024, nullable = false)
        var friendlyName: String = "Unnamed endpoint",

        var banned: Boolean = false,

        var online: Boolean = false, // TODO: Remove, this will be stored in InfluxDB

        @Embedded
        val configuration: EndpointConfiguration = EndpointConfiguration(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        val dataModel: EndpointDataModel = EndpointDataModel(),

        @Type(type = "jsonb")
        @Column(columnDefinition = "jsonb")
        val metaData: MutableMap<String, Any> = mutableMapOf()
) : BaseEntity() {
    @Id
    @GeneratedValue
    val uuid: UUID = UUID(0, 0)
}
