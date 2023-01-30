package com.coinplug.metadiumsample

import com.metadium.did.MetadiumWallet
import com.metadium.did.protocol.MetaDelegator
import com.metadium.did.verifiable.Verifier
import com.metadium.vc.VerifiableCredential
import com.metadium.vc.VerifiablePresentation
import com.nimbusds.jwt.SignedJWT
import org.junit.Test
import java.lang.Exception
import java.net.URI
import java.util.*
import com.metadium.did.crypto.MetadiumKey
import com.metaidum.did.resolver.client.DIDResolverAPI
import org.web3j.crypto.Sign
import org.web3j.crypto.Sign.SignatureData
import org.web3j.utils.Numeric
import java.nio.ByteBuffer
import java.nio.charset.Charset


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    /**
     * VC 검증
     */
    @Test
    fun verifyCredential() {
        val verifier = Verifier()
        DIDResolverAPI.getInstance().setResolverUrl("https://mt-resolver.blockchainbusan.kr/1.0/")
        val vc: SignedJWT = SignedJWT.parse("{signedJWT}")

        if (!verifier.verify(vc)) {
            println("Verifier: Signature 검증 실패")
        } else if (vc.jwtClaimsSet.expirationTime != null && vc.jwtClaimsSet.expirationTime.time < Date().time) {
            println("Verifier: 유효기간 초과")
        }

        val credential = VerifiableCredential(vc)

        println("credential contains type: ${credential.types}")

        credential.issunaceDate

        val vcId = credential.id
        println("VCID: $vcId")

        val subjects: Map<String, String> = credential.getCredentialSubject()
        for ((claimName, claimValue) in subjects) {

            println("Verifier: clainName: $claimName, claimValue: $claimValue")
        }
    }

    /**
     * VP검증
     */
    @Test
    fun verifyVerifiablePresentation() {
        val verifier = Verifier()
        DIDResolverAPI.getInstance().setResolverUrl("https://mt-resolver.blockchainbusan.kr/1.0/")

        val vp: SignedJWT = SignedJWT.parse("{signedJWT}")

        if (!verifier.verify(vp)) {
            println("검증 실패")
            return
        } else if (vp.jwtClaimsSet.expirationTime != null && vp.jwtClaimsSet.expirationTime.time < Date().time) {
            println("유효기간 초과")
            return
        }

        val vpObj = VerifiablePresentation(vp)
        val holderDid = vpObj.holder.toString()
        println("holderDID: $holderDid")

        val vpId: URI? = vpObj.id
        println("vpID: $vpId")

        val test = vpObj.verifiableCredentials.toList()
        println("asList: $test")

        for (obj in vpObj.verifiableCredentials) {
            val serializedVc = obj as String
            println("serializedVC: $serializedVc")

            val signedCredential = SignedJWT.parse(serializedVc)
            val credential = VerifiableCredential(signedCredential)
            println("credential type: ${credential.type}")
            val subjects: Map<String, String> = credential.getCredentialSubject()

            val vcID = credential.id
            println("issuer: ${credential.issuer}")
            println("vcID: $vcID")

            for ((claimName, claimValue) in subjects) {
                println("clainName: $claimName, claimValue: $claimValue")
            }

            println("getSubjects: ${subjects["name"]}")
        }
    }

    /**
     * VC생성
     */
    @Test
    fun makeVc() {
        //Issuer. 발급기관 wallet 정보
        val did = "did:meta:testnet:000000000000000000000000000000000000000000000000000000000006254a"
        val walletJson = "{\"did\":\"did:meta:testnet:000000000000000000000000000000000000000000000000000000000006254a\",\"private_key\":\"624e846856774e972ea5d88b9373f492eab4adb8e5d32eba4466b3baccfbfdc2\"}"
        val wallet = MetadiumWallet.fromJson(walletJson)

        //1번째 Claim인 first의 VC생성
        val firstCredential: SignedJWT = wallet!!.issueCredential(
            Collections.singletonList("EmailCredential"),  // types
            null,  // credential identifier. nullable
            Date(),  // issuance date. nullable
            null,  // expiration date. nullable
            did,
            mapOf("email" to "bbang@coinplug.com") // claims
        )
        val serializedFirstCredential = firstCredential.serialize()

        println("firstVC: $serializedFirstCredential")

        //2번쨰 Claim인 second의 VC생성
        val secondCredential: SignedJWT = wallet.issueCredential(
            Collections.singletonList("IdCredential"),  // types
            null,
            Date(),
            null,
            did,
            mapOf("name" to "Gildong Hong", "phoneNumber" to "010-1234-5678")
        )
        val serializedSecondCredential = secondCredential.serialize()

        println("secondVC: $serializedSecondCredential")

        //Holder. 사용자 wallet 정보
        val walletJson2 = "{\"did\":\"did:meta:testnet:000000000000000000000000000000000000000000000000000000000006254b\",\"private_key\":\"845c8464623096c4319e9b9d40cd305267004c64fcad83d5220877942ad11c4d\"}"
        val walletHolder = MetadiumWallet.fromJson(walletJson2)

        //앞서 생성한 2개의 VC를 VP로 만듦
        val vp: SignedJWT = walletHolder.issuePresentation(
            Collections.singletonList("UserInfoPresentation"),  // types
            null,  // presentation identifier. nullable
            null,  // issuance date. nullable
            null,  // expiration date. nullable
            arrayListOf(serializedFirstCredential, serializedSecondCredential) // VC list
        )
        val serializedVP = vp.serialize()

        println("makeVP: $serializedVP")
    }

    /**
     * wallet의 private key로 signature 생성
     */
    @Test
    fun signMessage() {
        val walletJson2 = "{\"did\":\"did:meta:testnet:000000000000000000000000000000000000000000000000000000000006254b\",\"private_key\":\"845c8464623096c4319e9b9d40cd305267004c64fcad83d5220877942ad11c4d\"}"
        val walletHolder = MetadiumWallet.fromJson(walletJson2)


        val signatureData = walletHolder.key.sign("test".toByteArray(Charsets.UTF_8))

        val buffer = ByteBuffer.allocate(65)
        buffer.put(signatureData.r)
        buffer.put(signatureData.s)
        buffer.put(signatureData.v)
        val signature = Numeric.toHexString(buffer.array())

        println("signature: $signature")

    }

    /**
     * did 삭제
     */
    @Test
    fun deleteDid() {
        val walletJson2 = "{\"did\":\"did:meta:testnet:000000000000000000000000000000000000000000000000000000000006254b\",\"private_key\":\"845c8464623096c4319e9b9d40cd305267004c64fcad83d5220877942ad11c4d\"}"
        val walletHolder = MetadiumWallet.fromJson(walletJson2)

        try {
            walletHolder.deleteDid(MetaDelegator())
            println("delete Result: success")
        } catch (e: Exception) {
            println("지갑 삭제 실패")
        }

    }

}