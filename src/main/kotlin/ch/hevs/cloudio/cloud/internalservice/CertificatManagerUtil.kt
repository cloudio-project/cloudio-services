package ch.hevs.cloudio.cloud.internalservice

import java.util.*


data class CertificateAndPrivateKey (val certificate: String, val privateKey: String)

data class UuidAndPublicKey(val uuid: UUID, val publicKey: String)

data class CertificateFromKey(val certificate: String)

