package ch.hevs.cloudio.cloud.model

import org.junit.Test
import java.util.*

class ModelIdentifierTests {
    @Test
    fun validEndpoint() {
        val id = ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NONE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('/') == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidEndpoint() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier(".").valid)
        assert(!ModelIdentifier("00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
    }

    @Test
    fun validEndpointOnlineAMQP() {
        val id = ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_ONLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validEndpointOnlineREST() {
        val id = ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_ONLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidEndpointOnlineAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@online").valid)
        assert(!ModelIdentifier("@online.").valid)
        assert(!ModelIdentifier("@online.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@oline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidEndpointOnlineREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@online").valid)
        assert(!ModelIdentifier("@online/").valid)
        assert(!ModelIdentifier("@online/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@oline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validEndpointOfflineAMQP() {
        val id = ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_OFFLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validEndpointOfflineREST() {
        val id = ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_OFFLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidEndpointOfflineAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@offline").valid)
        assert(!ModelIdentifier("@offline.").valid)
        assert(!ModelIdentifier("@offline.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@ofline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidEndpointOfflineREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@offline").valid)
        assert(!ModelIdentifier("@offline/").valid)
        assert(!ModelIdentifier("@offline/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@ofline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validNodeAddedAMQP() {
        val id = ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_ADDED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toString() == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
    }

    @Test
    fun validNodeAddedREST() {
        val id = ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_ADDED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toString('/') == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
    }

    @Test
    fun invalidNodeAddedAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@nodeAdded").valid)
        assert(!ModelIdentifier("@nodeAdded.").valid)
        assert(!ModelIdentifier("@nodeAdded.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66..aNode").valid)
        assert(!ModelIdentifier("@nodeAded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidNodeAddedREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@nodeAdded").valid)
        assert(!ModelIdentifier("@nodeAdded/").valid)
        assert(!ModelIdentifier("@nodeAdded/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66//aNode").valid)
        assert(!ModelIdentifier("@nodeAded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validNodeRemovedAMQP() {
        val id = ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_REMOVED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toString() == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
    }

    @Test
    fun validNodeRemovedREST() {
        val id = ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_REMOVED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toString('/') == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
    }

    @Test
    fun invalidNodeRemovedAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@nodeRemoved").valid)
        assert(!ModelIdentifier("@nodeRemoved.").valid)
        assert(!ModelIdentifier("@nodeRemoved.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66..aNode").valid)
        assert(!ModelIdentifier("@nodeRmoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidNodeRemovedREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@nodeRemoved").valid)
        assert(!ModelIdentifier("@nodeRemoved/").valid)
        assert(!ModelIdentifier("@nodeRemoved/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66//aNode").valid)
        assert(!ModelIdentifier("@nodeRemved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validAttributeUpdateAMQP() {
        val id = ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_UPDATE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_UPDATE)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun validAttributeUpdateREST() {
        val id = ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_UPDATE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString('/') == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_UPDATE)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString('/') == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun invalidAttributeUpdateAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@update").valid)
        assert(!ModelIdentifier("@update.").valid)
        assert(!ModelIdentifier("@update.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject..anAttribute").valid)
        assert(!ModelIdentifier("@updat.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
    }

    @Test
    fun invalidAttributeUpdateREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@update").valid)
        assert(!ModelIdentifier("@update/").valid)
        assert(!ModelIdentifier("@update/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject//anAttribute").valid)
        assert(!ModelIdentifier("@updat/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
    }

    @Test
    fun validAttributeSetAMQP() {
        val id = ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_SET)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString() == "@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_SET)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString() == "@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun validAttributeSetREST() {
        val id = ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_SET)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString('/') == "@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_SET)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString('/') == "@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun invalidAttributeSetAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@set").valid)
        assert(!ModelIdentifier("@set.").valid)
        assert(!ModelIdentifier("@set.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject..anAttribute").valid)
        assert(!ModelIdentifier("@se.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
    }

    @Test
    fun invalidAttributeSetREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@set").valid)
        assert(!ModelIdentifier("@set/").valid)
        assert(!ModelIdentifier("@set/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject//anAttribute").valid)
        assert(!ModelIdentifier("@et/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
    }

    @Test
    fun validAttributeDidSetAMQP() {
        val id = ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_DID_SET)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString() == "@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_DID_SET)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString() == "@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun validAttributeDidSetREST() {
        val id = ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ATTRIBUTE_DID_SET)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 3)
        assert(id[0] == "aNode")
        assert(id[1] == "anObject")
        assert(id[2] == "anAttribute")
        assert(id.toString('/') == "@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val id = ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.valid)
            assert(id.action == ActionIdentifier.ATTRIBUTE_DID_SET)
            assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(id.count() == i + 2)
            assert(id[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(id[i + 1] == it)
            }
            assert(id[i + 1] == "anAttribute")
            assert(id.toString('/') == "@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
        }
    }

    @Test
    fun invalidAttributeDidSetAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@didSet").valid)
        assert(!ModelIdentifier("@didSet.").valid)
        assert(!ModelIdentifier("@didSet.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@didSet.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject..anAttribute").valid)
        assert(!ModelIdentifier("@didSe.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
    }

    @Test
    fun invalidAttributeDidSetREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@didSet").valid)
        assert(!ModelIdentifier("@didSet/").valid)
        assert(!ModelIdentifier("@didSet/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@didSet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject//anAttribute").valid)
        assert(!ModelIdentifier("@didet/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
    }

    @Test
    fun validTransactionAMQP() {
        val id = ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.TRANSACTION)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validTransactionREST() {
        val id = ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.TRANSACTION)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidTransactionAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@transaction").valid)
        assert(!ModelIdentifier("@transaction.").valid)
        assert(!ModelIdentifier("@transaction.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@transction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@transaction.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidTransactionREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@transaction").valid)
        assert(!ModelIdentifier("@transaction/").valid)
        assert(!ModelIdentifier("@transaction/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@transction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@transaction/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validExecAMQP() {
        val id = ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validExecREST() {
        val id = ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidExecAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@exec").valid)
        assert(!ModelIdentifier("@exec.").valid)
        assert(!ModelIdentifier("@exec.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@exe.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@exec.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidExecREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@exec").valid)
        assert(!ModelIdentifier("@exec/").valid)
        assert(!ModelIdentifier("@exec/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@xec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@exec/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validExecOutputAMQP() {
        val id = ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validExecOutputREST() {
        val id = ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidExecOutputAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@execOutput").valid)
        assert(!ModelIdentifier("@execOutput.").valid)
        assert(!ModelIdentifier("@execOutput.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@exeOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidExecOutputREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@execOutput").valid)
        assert(!ModelIdentifier("@execOutput/").valid)
        assert(!ModelIdentifier("@execOutput/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@xecOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validDelayedAMQP() {
        val id = ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.DELAYED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validDelayedREST() {
        val id = ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.DELAYED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidDelayedAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@delayed").valid)
        assert(!ModelIdentifier("@delayed.").valid)
        assert(!ModelIdentifier("@delayed.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@elayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidDelayedREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@delayed").valid)
        assert(!ModelIdentifier("@delayed/").valid)
        assert(!ModelIdentifier("@delayed/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@dlayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }
}
