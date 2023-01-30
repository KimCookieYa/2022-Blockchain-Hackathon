package com.coinplug.metadiumsample

import com.metadium.did.protocol.MetaDelegator
import com.metaidum.did.resolver.client.DIDResolverAPI

object MetadiumModule {
    private const val BCBT_DELEGATOR_URL = "https://mt-delegator.blockchainbusan.kr"
    private const val BCBT_NODE_URL = "https://mt-api.blockchainbusan.kr"
    private const val BCBT_DID_PREFIX = "did:b-space"

    val delegator: MetaDelegator = MetaDelegator(BCBT_DELEGATOR_URL, BCBT_NODE_URL, BCBT_DID_PREFIX)

    //TestNet
    //val delegator: MetaDelegator = MetaDelegator("https://testdelegator.metadium.com", "https://api.metadium.com/dev", "did:meta:testnet")

}