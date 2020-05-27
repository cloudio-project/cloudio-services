package ch.hevs.cloudio.cloud.dao

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.util.*
import javax.persistence.MappedSuperclass

@MappedSuperclass
@TypeDefs(TypeDef(name = "jsonb", typeClass = JsonBinaryType::class))
open class BaseEntity {
    @CreatedDate
    private val created = Date()

    @CreatedBy
    private val createdBy: Long = 0

    @LastModifiedDate
    private val lastModified = Date()

    @LastModifiedBy
    private val lastModifiedBy: Long = 0
}
