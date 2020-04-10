package ch.hevs.cloudio.cloud.dao

import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import javax.persistence.MappedSuperclass

@MappedSuperclass
@TypeDefs(TypeDef(name = "jsonb", typeClass = JsonBinaryType::class))
open class BinaryJsonContainingEntity