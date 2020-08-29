package ch.hevs.cloudio.cloud.model

import org.junit.Test

class AttributeTypeTests {
    @Test
    fun invalidCheckType() {
        assert(!AttributeType.Invalid.checkType(false))
        assert(!AttributeType.Invalid.checkType(8))
        assert(!AttributeType.Invalid.checkType(8L))
        assert(!AttributeType.Invalid.checkType(1.2f))
        assert(!AttributeType.Invalid.checkType(2.3))
        assert(!AttributeType.Invalid.checkType("TEST"))
    }

    @Test
    fun booleanCheckType() {
        assert(AttributeType.Boolean.checkType(false))
        assert(!AttributeType.Boolean.checkType(8))
        assert(!AttributeType.Boolean.checkType(8L))
        assert(!AttributeType.Boolean.checkType(1.2f))
        assert(!AttributeType.Boolean.checkType(2.3))
        assert(!AttributeType.Boolean.checkType("TEST"))
    }

    @Test
    fun integerCheckType() {
        assert(!AttributeType.Integer.checkType(false))
        assert(AttributeType.Integer.checkType(8))
        assert(AttributeType.Integer.checkType(8L))
        assert(!AttributeType.Integer.checkType(1.2f))
        assert(!AttributeType.Integer.checkType(2.3))
        assert(!AttributeType.Integer.checkType("TEST"))
    }

    @Test
    fun numberCheckType() {
        assert(!AttributeType.Number.checkType(false))
        assert(!AttributeType.Number.checkType(8))
        assert(!AttributeType.Number.checkType(8L))
        assert(AttributeType.Number.checkType(1.2f))
        assert(AttributeType.Number.checkType(2.3))
        assert(!AttributeType.Number.checkType("TEST"))
    }

    @Test
    fun stringCheckType() {
        assert(!AttributeType.String.checkType(false))
        assert(!AttributeType.String.checkType(8))
        assert(!AttributeType.String.checkType(8L))
        assert(!AttributeType.String.checkType(1.2f))
        assert(!AttributeType.String.checkType(2.3))
        assert(AttributeType.String.checkType("TEST"))
    }
}
