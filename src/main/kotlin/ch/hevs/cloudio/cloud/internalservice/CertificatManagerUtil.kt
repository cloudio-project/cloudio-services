package ch.hevs.cloudio.cloud.internalservice

import java.io.Serializable
import java.util.*


class CertificateAndPrivateKey (val certificate: String, val privateKey: String)

class UuidAndPublicKey(val uuid: UUID, val publicKey: String): Serializable

