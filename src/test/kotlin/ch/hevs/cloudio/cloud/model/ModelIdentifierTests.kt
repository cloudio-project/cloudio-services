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
        assert(id.toModelPath() == "")
        assert(id.toModelPath('.') == "")
        assert(id.toAMQPTopic() == "")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "")
        assert(id.toInfluxSeriesName() == "")
        assert(id.toString() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('.') == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidEndpoint() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier(".").valid)
        assert(!ModelIdentifier("00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@upsdate.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@upsdate/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
    }

    @Test
    fun validEndpointOnlineAMQP() {
        val id = ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_ONLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toModelPath() == "")
        assert(id.toModelPath('.') == "")
        assert(id.toAMQPTopic() == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toInfluxSeriesName() == "")
        assert(id.toString() == "@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('.') == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validEndpointOnlineREST() {
        val id = ModelIdentifier("@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_ONLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toModelPath() == "")
        assert(id.toModelPath('.') == "")
        assert(id.toAMQPTopic() == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toInfluxSeriesName() == "")
        assert(id.toString() == "@online/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('.') == "@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
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
        assert(id.toModelPath() == "")
        assert(id.toModelPath('.') == "")
        assert(id.toAMQPTopic() == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toInfluxSeriesName() == "")
        assert(id.toString() == "@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('.') == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validEndpointOfflineREST() {
        val id = ModelIdentifier("@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.ENDPOINT_OFFLINE)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toModelPath() == "")
        assert(id.toModelPath('.') == "")
        assert(id.toAMQPTopic() == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toInfluxSeriesName() == "")
        assert(id.toString() == "@offline/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.toString('.') == "@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
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
        assert(id.toModelPath() == "aNode")
        assert(id.toModelPath('.') == "aNode")
        assert(id.toAMQPTopic() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toString() == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.toString('.') == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")

        val idv01 = ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.NODE_ADDED)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 1)
        assert(idv01[0] == "aNode")
        assert(idv01.toModelPath() == "aNode")
        assert(idv01.toModelPath('.') == "aNode")
        assert(idv01.toAMQPTopic() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toString() == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(idv01.toString('.') == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
    }

    @Test
    fun validNodeAddedREST() {
        val id = ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_ADDED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toModelPath() == "aNode")
        assert(id.toModelPath('.') == "aNode")
        assert(id.toAMQPTopic() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toString() == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.toString('.') == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")

        val idv01 = ModelIdentifier("@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/nodes/aNode")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.NODE_ADDED)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 1)
        assert(idv01[0] == "aNode")
        assert(idv01.toModelPath() == "aNode")
        assert(idv01.toModelPath('.') == "aNode")
        assert(idv01.toAMQPTopic() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toString() == "@nodeAdded/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(idv01.toString('.') == "@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
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
        assert(id.toModelPath() == "aNode")
        assert(id.toModelPath('.') == "aNode")
        assert(id.toAMQPTopic() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toString() == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.toString('.') == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")

        val idv01 = ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.NODE_REMOVED)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 1)
        assert(idv01[0] == "aNode")
        assert(idv01.toModelPath() == "aNode")
        assert(idv01.toModelPath('.') == "aNode")
        assert(idv01.toAMQPTopic() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toString() == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(idv01.toString('.') == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
    }

    @Test
    fun validNodeRemovedREST() {
        val id = ModelIdentifier("@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.valid)
        assert(id.action == ActionIdentifier.NODE_REMOVED)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "aNode")
        assert(id.toModelPath() == "aNode")
        assert(id.toModelPath('.') == "aNode")
        assert(id.toAMQPTopic() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(id.toString() == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(id.toString('.') == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")

        val idv01 = ModelIdentifier("@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66/nodes/aNode")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.NODE_REMOVED)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 1)
        assert(idv01[0] == "aNode")
        assert(idv01.toModelPath() == "aNode")
        assert(idv01.toModelPath('.') == "aNode")
        assert(idv01.toAMQPTopic() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
        assert(idv01.toString() == "@nodeRemoved/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode")
        assert(idv01.toString('.') == "@nodeRemoved.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode")
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
        assert(id.toModelPath() == "aNode/anObject/anAttribute")
        assert(id.toModelPath('.') == "aNode.anObject.anAttribute")
        assert(id.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.anObject.attributes.anAttribute")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(id.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")

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
            assert(id.toModelPath() == "aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.toModelPath('.') == "aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.${objectNames.joinToString(".objects.")}.attributes.anAttribute")
            assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
        }

        val idv01 = ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.anObject.attributes.anAttribute")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.ATTRIBUTE_UPDATE)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 3)
        assert(idv01[0] == "aNode")
        assert(idv01[1] == "anObject")
        assert(idv01[2] == "anAttribute")
        assert(idv01.toModelPath() == "aNode/anObject/anAttribute")
        assert(idv01.toModelPath('.') == "aNode.anObject.anAttribute")
        assert(idv01.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.anObject.attributes.anAttribute")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(idv01.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(idv01.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val idv01 = ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.${objectNames.joinToString(".objects.")}.attributes.anAttribute")
            assert(idv01.valid)
            assert(idv01.action == ActionIdentifier.ATTRIBUTE_UPDATE)
            assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(idv01.count() == i + 2)
            assert(idv01[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(idv01[i + 1] == it)
            }
            assert(idv01[i + 1] == "anAttribute")
            assert(idv01.toModelPath() == "aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(idv01.toModelPath('.') == "aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.${objectNames.joinToString(".objects.")}.attributes.anAttribute")
            assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(idv01.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
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
        assert(id.toModelPath() == "aNode/anObject/anAttribute")
        assert(id.toModelPath('.') == "aNode.anObject.anAttribute")
        assert(id.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.anObject.attributes.anAttribute")
        assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(id.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")

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
            assert(id.toModelPath() == "aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.toModelPath('.') == "aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.${objectNames.joinToString(".objects.")}.attributes.anAttribute")
            assert(id.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(id.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(id.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
        }

        val idv01 = ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/nodes/aNode/objects/anObject/attributes/anAttribute")
        assert(idv01.valid)
        assert(idv01.action == ActionIdentifier.ATTRIBUTE_UPDATE)
        assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(idv01.count() == 3)
        assert(idv01[0] == "aNode")
        assert(idv01[1] == "anObject")
        assert(idv01[2] == "anAttribute")
        assert(idv01.toModelPath() == "aNode/anObject/anAttribute")
        assert(idv01.toModelPath('.') == "aNode.anObject.anAttribute")
        assert(idv01.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.anObject.attributes.anAttribute")
        assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")
        assert(idv01.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute")
        assert(idv01.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute")

        for (i in 1..99) {
            val objectNames = List(i) { List(8) { ('a'..'z').random() }.joinToString() }
            val idv01 = ModelIdentifier("@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/nodes/aNode/objects/${objectNames.joinToString("/objects/")}/attributes/anAttribute")
            assert(idv01.valid)
            assert(idv01.action == ActionIdentifier.ATTRIBUTE_UPDATE)
            assert(idv01.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
            assert(idv01.count() == i + 2)
            assert(idv01[0] == "aNode")
            objectNames.forEachIndexed {i, it ->
                assert(idv01[i + 1] == it)
            }
            assert(idv01[i + 1] == "anAttribute")
            assert(idv01.toModelPath() == "aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(idv01.toModelPath('.') == "aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toAMQPTopic() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toAMQPTopicForMessageFormat1Endpoints() == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.nodes.aNode.objects.${objectNames.joinToString(".objects.")}.attributes.anAttribute")
            assert(idv01.toInfluxSeriesName() == "c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
            assert(idv01.toString() == "@update/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/${objectNames.joinToString("/")}/anAttribute")
            assert(idv01.toString('.') == "@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.${objectNames.joinToString(".")}.anAttribute")
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
        val id = ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.2486765")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "2486765")
        assert(id.last() == "2486765")
        assert(id.toString() == "@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/2486765")
    }

    @Test
    fun validExecOutputREST() {
        val id = ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/2486765")
        assert(id.valid)
        assert(id.action == ActionIdentifier.JOB_EXECUTE_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 1)
        assert(id[0] == "2486765")
        assert(id.last() == "2486765")
        assert(id.toString('/') == "@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/2486765")
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
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.45768434.aNode").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.45768434.aNode.anObject").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.45768434.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@execOutput.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.45768434.aNode.anObject.anotherObject.anAttribute").valid)
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
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/45768434/aNode").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/45768434/aNode/anObject").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/45768434/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@execOutput/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/45768434/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validDelayedAMQP() {
        val id = ModelIdentifier("@delayed.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.DELAYED_MESSAGES)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validDelayedREST() {
        val id = ModelIdentifier("@delayed/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.DELAYED_MESSAGES)
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

    @Test
    fun validLogLevelAMQP() {
        val id = ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.LOG_LEVEL)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validLogLevelREST() {
        val id = ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.LOG_LEVEL)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidLogLevelAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@logsLevel").valid)
        assert(!ModelIdentifier("@logsLevel.").valid)
        assert(!ModelIdentifier("@logsLevel.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@logLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@logsLevel.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidLogLevelREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@logsLevel").valid)
        assert(!ModelIdentifier("@logsLevel/").valid)
        assert(!ModelIdentifier("@logsLevel/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@logLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@logsLevel/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun validLogOutputAMQP() {
        val id = ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.LOG_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString() == "@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun validLogOutputREST() {
        val id = ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(id.valid)
        assert(id.action == ActionIdentifier.LOG_OUTPUT)
        assert(id.endpoint == UUID.fromString("c7bfaa1c-857f-438a-b5f0-447e3cd34f66"))
        assert(id.count() == 0)
        assert(id.toString('/') == "@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
    }

    @Test
    fun invalidLogOutputAMQP() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@logs").valid)
        assert(!ModelIdentifier("@logs.").valid)
        assert(!ModelIdentifier("@logs.00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@log.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anAttribute").valid)
        assert(!ModelIdentifier("@logs.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.aNode.anObject.anotherObject.anAttribute").valid)
    }

    @Test
    fun invalidLogOutputREST() {
        assert(!ModelIdentifier("").valid)
        assert(!ModelIdentifier("@logs").valid)
        assert(!ModelIdentifier("@logs/").valid)
        assert(!ModelIdentifier("@logs/00000000-0000-0000-0000-000000000000").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0--447e3cd34f66").valid)
        assert(!ModelIdentifier("@log/c7bfaa1c-857f-438a-b5f0-447e3cd34f66").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anAttribute").valid)
        assert(!ModelIdentifier("@logs/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/aNode/anObject/anotherObject/anAttribute").valid)
    }

    @Test
    fun resolveREST() {
        val model = EndpointDataModel().apply {
            nodes["node1"] = Node(implements = setOf("NODE1")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT1").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                    objects["subObj1"] = CloudioObject(conforms = "SUBOBJ1").apply {
                        attributes["subAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 42)
                        objects["subSubObj1"] = CloudioObject(conforms = "SUBSUBOBJ1").apply {
                            attributes["subSubAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 44)
                        }
                    }
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT2").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
            nodes["node2"] = Node(implements = setOf("NODE2")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT3").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT4").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
        }

        assert(ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66").resolve(model).get() is EndpointDataModel)

        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE1"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT1")
            assert(objects.count() == 1)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/subObj1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "SUBOBJ1")
            assert(objects.count() == 1)
            assert(attributes.count() == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/subObj1/subAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 42)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/subObj1/subSubObj1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "SUBSUBOBJ1")
            assert(objects.count() == 0)
            assert(attributes.count() == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/subObj1/subSubObj1/subSubAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 44)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT2")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2/attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2/attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2/attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2/attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object2/attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE2"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT3")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1/attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1/attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1/attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1/attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object1/attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT4")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2/attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2/attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2/attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2/attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node2/object2/attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        assert(ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66/node1/object1/subObj1/subSubObj1/subSubAttr1/any").resolve(model).isEmpty)
    }

    @Test
    fun resolveAMQP() {
        val model = EndpointDataModel().apply {
            nodes["node1"] = Node(implements = setOf("NODE1")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT1").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                    objects["subObj1"] = CloudioObject(conforms = "SUBOBJ1").apply {
                        attributes["subAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 42)
                        objects["subSubObj1"] = CloudioObject(conforms = "SUBSUBOBJ1").apply {
                            attributes["subSubAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 44)
                        }
                    }
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT2").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
            nodes["node2"] = Node(implements = setOf("NODE2")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT3").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT4").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
        }

        assert(ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66").resolve(model).get() is EndpointDataModel)

        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE1"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT1")
            assert(objects.count() == 1)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "SUBOBJ1")
            assert(objects.count() == 1)
            assert(attributes.count() == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 42)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subSubObj1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "SUBSUBOBJ1")
            assert(objects.count() == 0)
            assert(attributes.count() == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subSubObj1.subSubAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 44)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT2")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE2"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT3")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2").resolve(model).get() as CloudioObject).apply {
            assert(conforms == "OBJECT4")
            assert(objects.count() == 0)
            assert(attributes.count() == 5)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        assert(ModelIdentifier("c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subSubObj1.subSubAttr1.any").resolve(model).isEmpty)
    }

    @Test
    fun resolveAMQPWithAction() {
        val model = EndpointDataModel().apply {
            nodes["node1"] = Node(implements = setOf("NODE1")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT1").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                    objects["subObj1"] = CloudioObject(conforms = "SUBOBJ1").apply {
                        attributes["subAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 42)
                        objects["subSubObj1"] = CloudioObject(conforms = "SUBSUBOBJ1").apply {
                            attributes["subSubAttr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 44)
                        }
                    }
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT2").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
            nodes["node2"] = Node(implements = setOf("NODE2")).apply {
                objects["object1"] = CloudioObject(conforms = "OBJECT3").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
                objects["object2"] = CloudioObject(conforms = "OBJECT4").apply {
                    attributes["attr1"] = Attribute(AttributeConstraint.Static, AttributeType.Integer, System.currentTimeMillis().toDouble(), 1)
                    attributes["attr2"] = Attribute(AttributeConstraint.Status, AttributeType.Number, System.currentTimeMillis().toDouble(), 2.0)
                    attributes["attr3"] = Attribute(AttributeConstraint.Measure, AttributeType.String, System.currentTimeMillis().toDouble(), "three")
                    attributes["attr4"] = Attribute(AttributeConstraint.SetPoint, AttributeType.Integer, System.currentTimeMillis().toDouble(), 4)
                    attributes["attr5"] = Attribute(AttributeConstraint.Parameter, AttributeType.Boolean, System.currentTimeMillis().toDouble(), true)
                }
            }
        }

        assert(ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").resolve(model).get() is EndpointDataModel)

        (ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE1"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 42)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subSubObj1.subSubAttr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 44)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object2.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("@nodeAdded.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2").resolve(model).get() as Node).apply {
            assert(implements.contains("NODE2"))
            assert(objects.count() == 2)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object1.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr1").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Static)
            assert(type == AttributeType.Integer)
            assert(value == 1)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr2").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Status)
            assert(type == AttributeType.Number)
            assert(value == 2.0)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr3").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Measure)
            assert(type == AttributeType.String)
            assert(value == "three")
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr4").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.SetPoint)
            assert(type == AttributeType.Integer)
            assert(value == 4)
        }
        (ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node2.object2.attr5").resolve(model).get() as Attribute).apply {
            assert(constraint == AttributeConstraint.Parameter)
            assert(type == AttributeType.Boolean)
            assert(value == true)
        }
        assert(ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1.object1.subObj1.subSubObj1.subSubAttr1.any").resolve(model).isEmpty)
        assert(ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66").resolve(model).isEmpty)
        assert(ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1").resolve(model).isEmpty)
        assert(ModelIdentifier("@update.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1/object1").resolve(model).isEmpty)
        assert(ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.node1/object1/attr1").resolve(model).isEmpty)
    }

    @Test
    fun equalsTest() {
        val m1 = ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f66")
        assert(ModelIdentifier(m1.toString('.')) == m1)
        assert(ModelIdentifier("@online.c7bfaa1c-857f-438a-b5f0-447e3cd34f67") != m1)
        assert(ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66") != m1)
        assert(ModelIdentifier("@offline.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.toto") != m1)
    }

    @Test
    fun endpointSubscriptionTest() {
        assert(ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.#").valid)
        assert(ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/#").valid)
        assert(!ModelIdentifier("@set.c7bfaa1c-857f-438a-b5f0-447e3cd34f66.#.toto").valid)
        assert(!ModelIdentifier("@set/c7bfaa1c-857f-438a-b5f0-447e3cd34f66/#/toto").valid)
    }
}
