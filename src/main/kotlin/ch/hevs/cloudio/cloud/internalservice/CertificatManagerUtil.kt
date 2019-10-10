package ch.hevs.cloudio.cloud.internalservice

import java.security.KeyFactory
import java.util.*
import java.security.spec.X509EncodedKeySpec
import java.security.PublicKey



class CertificateAndPrivateKey (val certificate: String, val privateKey: String)

class UuidAndPublicKey(val uuid: UUID, val publicKey: String)

