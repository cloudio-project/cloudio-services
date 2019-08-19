package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio2.cloud.internalservice.CertificateManagerService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.env.MockEnvironment
import java.io.StringReader
import java.util.*

@RunWith(JUnit4::class)
@SpringBootTest
class CertificateManagerTest {
    @Test
    fun certificateAuthority() {
        val environment = MockEnvironment().apply {
            setProperty("cloudio.caPrivateKey", """
                -----BEGIN PRIVATE KEY-----
                MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCpurJYJ1254tDt
                4xNXrd1QkLGri3jounN9DbnQvzfifrvg6jPkhk30wrgeh67TUDkcTOZmwFoYoBt9
                kynfv4AvWbXP1vRYU/erSglmJYPLxEJXnLI3QQtrCHmXWpEK/5pj5BLEYOqa1QVy
                FoV17lOwLO6nckxEnCVCr8fm003FPHLWhL+IwU+sV4VVqWz+W3BD6qF9t4m25XQW
                AQnvFd6YVwW4liJc1nPoNVtN0l+HogOPqshGKJVbCwovC+Ntv+Qi++8sxBE3PU4y
                IpyuVBhqA/RTSCuhTWiiKrzdbNjG5GA13YIlF37Fck4IGPSOBgMXpSRwwW+tJZtB
                oJ5H+luNAgMBAAECggEAf5nCNo/x4Kvqks1iX0pw1f7R87u5PI3gASXuikcXxoms
                yZyJQsbNmLNDJICxKGBpbyNJG/qEQXss761Rj8synWZ+TzC1JiqKShYxFsAhY3G6
                xo8hVHTsFp2ae/U0keYzteFjUoviFGEN5QWVdGGmPQ6qpOxC9lnFkWsBiEFXeG2N
                FsHVU6Igs3AEbq3vstPAzVNbQsrABocuarkbN0IH1/lohkBlAojXgvfSm0RtSRYy
                cjxJxc1O0+ajbKjYir8sadBmYApL/4Zjlg7i+V8RFP6wJTW/M7GEQ7K6WFzMmwLI
                eW+W8x1txYh9UToRYwELCUCqufj7NyNbLXzxbAmmqQKBgQDiPLiU/UO+eyCoAcAX
                FwT/1CdScjBv3zzz5yVIgw+WRvEBIJb0CdRDd1g7151oWi5SEDIaPj247TFv42ZA
                N5roGeRuat7/KIgLxeeqh7/jPFCVN51cuxYhBJTMFPLjSiLfU/hU9XpI1e0bfV+m
                7yGqdrNVQ/H7saAEfo+W9yIqBwKBgQDADuL5v4ZQtBgjDXbOdDs++g4a9gGlcCkq
                g6L0aYjRSos6ZLgi+L8xjcQ/US0DU/dhMEBjSGcfOIXBZI34J9DWNFvT2OiycbU7
                n8/yiKqJhHOyDiiWYbJupqRig8qlRk5y8isXCZ65KEH6uBOVUWYQ2f8kV52JE7tI
                AldQPWK4ywKBgQCBSGfGpFAxKtWHjLCDwWDW/RqXb6+kTnLbgmx42oRuwQ16MnBw
                9qj5ANtIHzfRaS9tL9ohyw2kfs8wfEdzTRNVrW4vdx0FvNi5uZdgiQCrz0zRaJ2h
                XzwPFDofwZznK/dvneGcU0dVFgLQIb4mfCLF8d0bTZ+b0G58AwL1Jza7wwKBgDpq
                zMMmbFZT0RuoVsDJPdQT/wJYXXvSt9LEo2YwS3tNbzfdttx8SLtr4YtYx5ZBjfcz
                1AHe3e7zJEwEp+IZSj49QskXKwBL8dIm8/tTcnvNm9tGzzWelcVuToXgjFzlG6t+
                2XhyZqLvCdKW9u3uCluJ0z9cnwA4zjhC7gfsBAEtAoGAL/Vu/COVqao0gQVVsZjl
                WlW7A7xsaBdcSTCJ5uj5BE5JD6VhBnMymz6eMgjUT9+M2DcadLjqRfvzOhSBUFpa
                xqqq7yfKPaM1+a6u+UJwXSVHqAIH6//7gKKd3oK76BdVQ5fWdIPU06oTT0UlUmPU
                v3rKeyI7tYvP/q3XHieRTPM=
                -----END PRIVATE KEY-----""".trimIndent())
            setProperty("cloudio.caCertificate", """
                -----BEGIN CERTIFICATE-----
                MIICoDCCAYgCCQCm/ar+qaI74zANBgkqhkiG9w0BAQsFADASMRAwDgYDVQQDDAdj
                bG91ZGlPMB4XDTE5MDQxNzE5MTA0NVoXDTI5MDQxNDE5MTA0NVowEjEQMA4GA1UE
                AwwHY2xvdWRpTzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKm6slgn
                Xbni0O3jE1et3VCQsauLeOi6c30NudC/N+J+u+DqM+SGTfTCuB6HrtNQORxM5mbA
                WhigG32TKd+/gC9Ztc/W9FhT96tKCWYlg8vEQlecsjdBC2sIeZdakQr/mmPkEsRg
                6prVBXIWhXXuU7As7qdyTEScJUKvx+bTTcU8ctaEv4jBT6xXhVWpbP5bcEPqoX23
                ibbldBYBCe8V3phXBbiWIlzWc+g1W03SX4eiA4+qyEYolVsLCi8L422/5CL77yzE
                ETc9TjIinK5UGGoD9FNIK6FNaKIqvN1s2MbkYDXdgiUXfsVyTggY9I4GAxelJHDB
                b60lm0Ggnkf6W40CAwEAATANBgkqhkiG9w0BAQsFAAOCAQEARQba/LkbFi21Jypw
                q71iB7qxtCfwmr/+7UHjyW9s2LolFJdmBc+9SJWPzQkYxdf7seRkkuVnjBW27hG7
                JiHo98YqQqWvfwrG8SgpXNLpBVbb5aBBhPPUq9nePyUOnXbvl0FTU6vQa1FfUAxr
                aHsjYAk06v2ilKzSrUlXWvR6Qym86QpzA7tqLaorCKOhsVKKjeGC+VCp24Bv8jI9
                nyja2xLqF0RWy9Pfht9izkjT0ISa7YzQ9FNBfcgwH4584XcZzgVmqBP1hSbyaMqS
                YbvuYfPwY7671lVKNWbdv88//aal3yt2ZnODikZCnLh7OdpNLg2wspBkxp6a7flR
                7AsFbQ==
                -----END CERTIFICATE-----""".trimIndent())
        }


        val mapper = ObjectMapper().registerModule(KotlinModule())
        val authority = CertificateManagerService(environment)
        val certAndKey = CertificateAndPrivateKey("","")
        mapper.readerForUpdating(certAndKey).readValue(authority.generateEndpointKeyAndCertificatePair(UUID.randomUUID())) as CertificateAndPrivateKey?

        assert(certAndKey.certificate.contains("-----BEGIN CERTIFICATE-----"))
        assert(certAndKey.privateKey.contains("-----BEGIN RSA PRIVATE KEY-----") || certAndKey.privateKey.contains("-----BEGIN PRIVATE KEY-----"))

        val caCert = JcaX509CertificateConverter().getCertificate(PEMParser(
                StringReader(environment.getProperty("cloudio.caCertificate"))
        ).readObject() as X509CertificateHolder)
        caCert.checkValidity()

        val clientCert = JcaX509CertificateConverter().getCertificate(PEMParser(
                StringReader(certAndKey.certificate)
        ).readObject() as X509CertificateHolder)

        clientCert.checkValidity()

        assert(clientCert.issuerX500Principal == caCert.subjectX500Principal)
    }
}
