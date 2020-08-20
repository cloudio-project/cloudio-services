package ch.hevs.cloudio.cloud.model

import org.junit.Test

class ActionIdentifierTest {
    @Test
    fun testFromURINoneOrInvalid() {
        mutableListOf<String>().let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.NONE)
            assert(it.count() == 1)
            assert(action.toString() == "")
        }

        (1..100).forEach { objectCount ->
            mutableListOf("e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
                for (i in 0 until objectCount) {
                    it.add("OBJECT_NAME")
                }
                it.add("ATTRIBUTE_NAME")
                val action = ActionIdentifier.fromURI(it)
                assert(action == ActionIdentifier.NONE)
                assert(it.count() == objectCount + 3)
                assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
                assert(action.toString() == "")
            }
        }

        mutableListOf("@nothing").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@nothing", "at", "all").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "at")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIOnline() {
        mutableListOf("@online").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@online", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.ENDPOINT_ONLINE)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@online")
        }

        mutableListOf("@online", "e7b45562-cf44-4940-8441-f0892e32328c", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIOffline() {
        mutableListOf("@offline").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@offline", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.ENDPOINT_OFFLINE)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@offline")
        }

        mutableListOf("@offline", "e7b45562-cf44-4940-8441-f0892e32328c", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURINodeAdded() {
        mutableListOf("@nodeAdded").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@nodeAdded", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@nodeAdded", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.NODE_ADDED)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@nodeAdded")
        }

        mutableListOf("@nodeAdded", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURINodeRemoved() {
        mutableListOf("@nodeRemoved").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@nodeRemoved", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@nodeRemoved", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.NODE_REMOVED)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@nodeRemoved")
        }

        mutableListOf("@nodeRemoved", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIUpdate() {
        mutableListOf("@update").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@update", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@update", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@update", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@update", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME", "ATTRIBUTE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.ATTRIBUTE_UPDATE)
            assert(it.count() == 4)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@update")
        }

        (1..100).forEach { objectCount ->
            mutableListOf("@update", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
                for (i in 0 until objectCount) {
                    it.add("OBJECT_NAME")
                }
                it.add("ATTRIBUTE_NAME")
                val action = ActionIdentifier.fromURI(it)
                assert(action == ActionIdentifier.ATTRIBUTE_UPDATE)
                assert(it.count() == objectCount + 3)
                assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
                assert(action.toString() == "@update")
            }
        }
    }

    @Test
    fun testFromURISet() {
        mutableListOf("@set").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@set", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@set", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@set", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@set", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME", "ATTRIBUTE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.ATTRIBUTE_SET)
            assert(it.count() == 4)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@set")
        }

        (1..100).forEach { objectCount ->
            mutableListOf("@set", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
                for (i in 0 until objectCount) {
                    it.add("OBJECT_NAME")
                }
                it.add("ATTRIBUTE_NAME")
                val action = ActionIdentifier.fromURI(it)
                assert(action == ActionIdentifier.ATTRIBUTE_SET)
                assert(it.count() == objectCount + 3)
                assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
                assert(action.toString() == "@set")
            }
        }
    }

    @Test
    fun testFromURIDidSet() {
        mutableListOf("@didSet").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@didSet", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@didSet", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@didSet", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@didSet", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME", "OBJECT_NAME", "ATTRIBUTE_NAME").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.ATTRIBUTE_DID_SET)
            assert(it.count() == 4)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@didSet")
        }

        (1..100).forEach { objectCount ->
            mutableListOf("@didSet", "e7b45562-cf44-4940-8441-f0892e32328c", "NODE_NAME").let {
                for (i in 0 until objectCount) {
                    it.add("OBJECT_NAME")
                }
                it.add("ATTRIBUTE_NAME")
                val action = ActionIdentifier.fromURI(it)
                assert(action == ActionIdentifier.ATTRIBUTE_DID_SET)
                assert(it.count() == objectCount + 3)
                assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
                assert(action.toString() == "@didSet")
            }
        }
    }

    @Test
    fun testFromURITransaction() {
        mutableListOf("@transaction").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@transaction", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.TRANSACTION)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@transaction")
        }

        mutableListOf("@transaction", "e7b45562-cf44-4940-8441-f0892e32328c", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIExec() {
        mutableListOf("@exec").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@exec", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.JOB_EXECUTE)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@exec")
        }

        mutableListOf("@exec", "e7b45562-cf44-4940-8441-f0892e32328c", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIExecOutput() {
        mutableListOf("@execOutput").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@execOutput", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@execOutput", "e7b45562-cf44-4940-8441-f0892e32328c", "CORRELATION_ID").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.JOB_EXECUTE_OUTPUT)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@execOutput")
        }

        mutableListOf("@execOutput", "e7b45562-cf44-4940-8441-f0892e32328c", "CORRELATION_ID", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 3)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }

    @Test
    fun testFromURIDelayed() {
        mutableListOf("@delayed").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 0)
            assert(action.toString() == "INVALID")
        }

        mutableListOf("@delayed", "e7b45562-cf44-4940-8441-f0892e32328c").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.DELAYED)
            assert(it.count() == 1)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "@delayed")
        }

        mutableListOf("@delayed", "e7b45562-cf44-4940-8441-f0892e32328c", "to_long").let {
            val action = ActionIdentifier.fromURI(it)
            assert(action == ActionIdentifier.INVALID)
            assert(it.count() == 2)
            assert(it.first() == "e7b45562-cf44-4940-8441-f0892e32328c")
            assert(action.toString() == "INVALID")
        }
    }
}