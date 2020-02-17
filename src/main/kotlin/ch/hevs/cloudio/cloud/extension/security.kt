package ch.hevs.cloudio.cloud.extension

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.OutputStream
import java.io.StringReader
import java.io.StringWriter
import java.math.BigInteger
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.util.*

fun UUID.toBigInteger(): BigInteger {
    var lo = BigInteger.valueOf(this.leastSignificantBits)
    var hi = BigInteger.valueOf(this.mostSignificantBits)
    if (lo.signum() < 0) lo = lo.add(BigInteger.ONE.shiftLeft(64))
    if (hi.signum() < 0) hi = hi.add(BigInteger.ONE.shiftLeft(64))
    return lo.add(hi.shiftLeft(64))
}

fun String.toX509Certificate() = JcaX509CertificateConverter().getCertificate(PEMParser(
        StringReader(this)).readObject() as X509CertificateHolder)

fun X509Certificate.toPEMString() = StringWriter().also { writer ->
    JcaPEMWriter(writer).let {
        it.writeObject(this)
        it.flush()
        it.close()
    }
}.toString()

fun String.toPrivateKey() = JcaPEMKeyConverter().getPrivateKey(PEMParser(
        StringReader(this)).readObject() as PrivateKeyInfo)

fun PrivateKey.toPEMString() = StringWriter().also { writer ->
    JcaPEMWriter(writer).let {
        it.writeObject(this)
        it.flush()
        it.close()
    }
}.toString()

fun String.toPublicKey() = JcaPEMKeyConverter().getPublicKey(PEMParser(
        StringReader(this)).readObject() as SubjectPublicKeyInfo)

fun PublicKey.toPEMString() = StringWriter().also { writer ->
    JcaPEMWriter(writer).let {
        it.writeObject(this)
        it.flush()
        it.close()
    }
}.toString()

fun OutputStream.writePKCS12Keystore(keyStorePassword: String, certificate: X509Certificate,  privateKey: PrivateKey) {
    val pkcs12 = KeyStore.getInstance("PKCS12")
    pkcs12.load(null, null)
    pkcs12.setKeyEntry("", privateKey, "".toCharArray(), arrayOf(certificate))
    pkcs12.store(this, keyStorePassword.toCharArray())
}

fun OutputStream.writeJKSTruststore(keyStorePassword: String, certificate: X509Certificate) {
    val jks = KeyStore.getInstance("JKS")
    jks.load(null, null)
    jks.setCertificateEntry("", certificate)
    jks.store(this, keyStorePassword.toCharArray())
}

fun String.Companion.generateRandomPassword(length: Int = 16): String {
    val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return List(length) { alphabet.random() }.joinToString("")
}
