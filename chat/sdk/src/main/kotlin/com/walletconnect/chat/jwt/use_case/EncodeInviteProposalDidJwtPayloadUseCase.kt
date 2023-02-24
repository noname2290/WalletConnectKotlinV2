@file:JvmSynthetic

package com.walletconnect.chat.jwt.use_case

import com.walletconnect.chat.common.model.AccountId
import com.walletconnect.chat.jwt.ChatDidJwtClaims
import com.walletconnect.foundation.common.model.PublicKey
import com.walletconnect.foundation.util.jwt.encodeDidPkh
import com.walletconnect.foundation.util.jwt.encodeX25519DidKey

internal class EncodeInviteProposalDidJwtPayloadUseCase(
    private val inviterPublicKey: PublicKey,
    private val inviteeAccountId: AccountId,
    private val openingMessage: String,
) : EncodeDidJwtPayloadUseCase {

    override operator fun invoke(issuer: String, keyserverUrl: String, issuedAt: Long, expiration: Long): ChatDidJwtClaims = ChatDidJwtClaims.InviteProposal(
        issuer = issuer,
        issuedAt = issuedAt,
        expiration = expiration,
        keyserverUrl = keyserverUrl,
        subject = openingMessage,
        audience = encodeDidPkh(inviteeAccountId.value),
        inviterPublicKey = encodeX25519DidKey(inviterPublicKey.keyAsBytes)
    )
}