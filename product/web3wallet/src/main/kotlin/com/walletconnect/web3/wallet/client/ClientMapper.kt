package com.walletconnect.web3.wallet.client

import com.walletconnect.auth.client.Auth
import com.walletconnect.sign.client.Sign

@JvmSynthetic
internal fun Map<String, Wallet.Model.Namespace.Session>.toSign(): Map<String, Sign.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Session>.toWallet(): Map<String, Wallet.Model.Namespace.Session> =
    mapValues { (_, namespace) ->
        Wallet.Model.Namespace.Session(namespace.chains, namespace.accounts, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Sign.Model.Namespace.Proposal>.toWalletProposalNamespaces(): Map<String, Wallet.Model.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Wallet.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Map<String, Wallet.Model.Namespace.Proposal>.toSignProposalNamespaces(): Map<String, Sign.Model.Namespace.Proposal> =
    mapValues { (_, namespace) ->
        Sign.Model.Namespace.Proposal(namespace.chains, namespace.methods, namespace.events)
    }

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.toSign(): Sign.Model.JsonRpcResponse =
    when (this) {
        is Wallet.Model.JsonRpcResponse.JsonRpcResult -> this.toSign()
        is Wallet.Model.JsonRpcResponse.JsonRpcError -> this.toSign()
    }

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.JsonRpcResult.toSign(): Sign.Model.JsonRpcResponse.JsonRpcResult =
    Sign.Model.JsonRpcResponse.JsonRpcResult(id, result)

@JvmSynthetic
internal fun Wallet.Model.JsonRpcResponse.JsonRpcError.toSign(): Sign.Model.JsonRpcResponse.JsonRpcError =
    Sign.Model.JsonRpcResponse.JsonRpcError(id, code, message)

@JvmSynthetic
internal fun Wallet.Params.AuthRequestResponse.toAuth(): Auth.Params.Respond = when (this) {
    is Wallet.Params.AuthRequestResponse.Result -> Auth.Params.Respond.Result(id, signature.toAuth(), issuer)
    is Wallet.Params.AuthRequestResponse.Error -> Auth.Params.Respond.Error(id, code, message)
}

@JvmSynthetic
internal fun Wallet.Model.Cacao.Signature.toAuth(): Auth.Model.Cacao.Signature = Auth.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Wallet.Model.Cacao.Signature.toSign(): Sign.Model.Cacao.Signature = Sign.Model.Cacao.Signature(t, s, m)

@JvmSynthetic
internal fun Wallet.Model.SessionEvent.toSign(): Sign.Model.SessionEvent = Sign.Model.SessionEvent(name, data)

@JvmSynthetic
internal fun Wallet.Model.PayloadParams.toAuth(): Auth.Model.PayloadParams =
    Auth.Model.PayloadParams(
        type = type,
        chainId = chainId,
        domain = domain,
        aud = aud,
        version = version,
        nonce = nonce,
        iat = iat,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
    )

@JvmSynthetic
internal fun Wallet.Model.PayloadAuthRequestParams.toSign(): Sign.Model.PayloadParams =
    Sign.Model.PayloadParams(
        type = type,
        chains = chains,
        domain = domain,
        aud = aud,
        version = version,
        nonce = nonce,
        iat = iat,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
    )

@JvmSynthetic
internal fun Sign.Model.Session.toWallet(): Wallet.Model.Session = Wallet.Model.Session(
    pairingTopic, topic, expiry, requiredNamespaces.toWalletProposalNamespaces(), optionalNamespaces?.toWalletProposalNamespaces(), namespaces.toWallet(), metaData
)

@JvmSynthetic
internal fun List<Sign.Model.PendingRequest>.mapToPendingRequests(): List<Wallet.Model.PendingSessionRequest> = map { request ->
    Wallet.Model.PendingSessionRequest(
        request.requestId,
        request.topic,
        request.method,
        request.chainId,
        request.params
    )
}

@JvmSynthetic
internal fun List<Sign.Model.SessionRequest>.mapToPendingSessionRequests(): List<Wallet.Model.SessionRequest> = map { request ->
    Wallet.Model.SessionRequest(
        request.topic,
        request.chainId,
        request.peerMetaData,
        Wallet.Model.SessionRequest.JSONRPCRequest(request.request.id, request.request.method, request.request.params)
    )
}

internal fun Auth.Model.PayloadParams.toWallet(): Wallet.Model.PayloadParams =
    Wallet.Model.PayloadParams(
        type = type,
        chainId = chainId,
        domain = domain,
        aud = aud,
        version = version,
        nonce = nonce,
        iat = iat,
        nbf = nbf,
        exp = exp,
        statement = statement,
        requestId = requestId,
        resources = resources,
    )

@JvmSynthetic
internal fun List<Auth.Model.PendingRequest>.toWallet(): List<Wallet.Model.PendingAuthRequest> =
    map { request ->
        Wallet.Model.PendingAuthRequest(
            request.id,
            request.pairingTopic,
            request.payloadParams.toWallet()
        )
    }

@JvmSynthetic
internal fun Sign.Model.SessionProposal.toWallet(): Wallet.Model.SessionProposal =
    Wallet.Model.SessionProposal(
        pairingTopic,
        name,
        description,
        url,
        icons,
        redirect,
        requiredNamespaces.toWalletProposalNamespaces(),
        optionalNamespaces.toWalletProposalNamespaces(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun Sign.Model.SessionAuthenticate.toWallet(): Wallet.Model.SessionAuthenticated =
    Wallet.Model.SessionAuthenticated(id, pairingTopic, payloadParams.toWallet())

@JvmSynthetic
internal fun Sign.Model.PayloadParams.toWallet(): Wallet.Model.PayloadAuthRequestParams =
    Wallet.Model.PayloadAuthRequestParams(type, chains, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)

internal fun Sign.Model.VerifyContext.toWallet(): Wallet.Model.VerifyContext =
    Wallet.Model.VerifyContext(id, origin, this.validation.toWallet(), verifyUrl, isScam)

internal fun Sign.Model.Validation.toWallet(): Wallet.Model.Validation =
    when (this) {
        Sign.Model.Validation.VALID -> Wallet.Model.Validation.VALID
        Sign.Model.Validation.INVALID -> Wallet.Model.Validation.INVALID
        Sign.Model.Validation.UNKNOWN -> Wallet.Model.Validation.UNKNOWN
    }

internal fun Auth.Model.Validation.toWallet(): Wallet.Model.Validation =
    when (this) {
        Auth.Model.Validation.VALID -> Wallet.Model.Validation.VALID
        Auth.Model.Validation.INVALID -> Wallet.Model.Validation.INVALID
        Auth.Model.Validation.UNKNOWN -> Wallet.Model.Validation.UNKNOWN
    }

@JvmSynthetic
internal fun Sign.Model.SessionRequest.toWallet(): Wallet.Model.SessionRequest =
    Wallet.Model.SessionRequest(
        topic = topic,
        chainId = chainId,
        peerMetaData = peerMetaData,
        request = Wallet.Model.SessionRequest.JSONRPCRequest(
            id = request.id,
            method = request.method,
            params = request.params
        )
    )

@JvmSynthetic
internal fun Sign.Model.DeletedSession.toWallet(): Wallet.Model.SessionDelete =
    when (this) {
        is Sign.Model.DeletedSession.Success -> Wallet.Model.SessionDelete.Success(topic, reason)
        is Sign.Model.DeletedSession.Error -> Wallet.Model.SessionDelete.Error(error)
    }

@JvmSynthetic
internal fun Sign.Model.SettledSessionResponse.toWallet(): Wallet.Model.SettledSessionResponse =
    when (this) {
        is Sign.Model.SettledSessionResponse.Result -> Wallet.Model.SettledSessionResponse.Result(session.toWallet())
        is Sign.Model.SettledSessionResponse.Error -> Wallet.Model.SettledSessionResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun Sign.Model.SessionUpdateResponse.toWallet(): Wallet.Model.SessionUpdateResponse =
    when (this) {
        is Sign.Model.SessionUpdateResponse.Result -> Wallet.Model.SessionUpdateResponse.Result(topic, namespaces.toWallet())
        is Sign.Model.SessionUpdateResponse.Error -> Wallet.Model.SessionUpdateResponse.Error(errorMessage)
    }

@JvmSynthetic
internal fun Auth.Event.AuthRequest.toWallet(): Wallet.Model.AuthRequest = Wallet.Model.AuthRequest(id, pairingTopic, payloadParams.toWallet())

@JvmSynthetic
internal fun Auth.Event.VerifyContext.toWallet(): Wallet.Model.VerifyContext = Wallet.Model.VerifyContext(id, origin, this.validation.toWallet(), verifyUrl, isScam)

@JvmSynthetic
internal fun Auth.Model.VerifyContext.toWallet(): Wallet.Model.VerifyContext = Wallet.Model.VerifyContext(id, origin, this.validation.toWallet(), verifyUrl, isScam)

@JvmSynthetic
internal fun Wallet.Model.SessionProposal.toSign(): Sign.Model.SessionProposal =
    Sign.Model.SessionProposal(
        pairingTopic,
        name,
        description,
        url,
        icons,
        redirect,
        requiredNamespaces.toSignProposalNamespaces(),
        optionalNamespaces.toSignProposalNamespaces(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun Sign.Model.Message.SessionProposal.toWallet(): Wallet.Model.Message.SessionProposal =
    Wallet.Model.Message.SessionProposal(
        id,
        pairingTopic,
        name,
        description,
        url,
        icons,
        redirect,
        requiredNamespaces.toWalletProposalNamespaces(),
        optionalNamespaces.toWalletProposalNamespaces(),
        properties,
        proposerPublicKey,
        relayProtocol,
        relayData
    )

@JvmSynthetic
internal fun Sign.Model.Message.SessionRequest.toWallet(): Wallet.Model.Message.SessionRequest =
    Wallet.Model.Message.SessionRequest(
        topic,
        chainId,
        peerMetaData,
        Wallet.Model.Message.SessionRequest.JSONRPCRequest(request.id, request.method, request.params)
    )

@JvmSynthetic
internal fun Auth.Model.Message.AuthRequest.toWallet(): Wallet.Model.Message.AuthRequest = with(payloadParams) {
    Wallet.Model.Message.AuthRequest(
        id,
        pairingTopic,
        metadata,
        Wallet.Model.Message.AuthRequest.PayloadParams(type, chainId, domain, aud, version, nonce, iat, nbf, exp, statement, requestId, resources)
    )
}

@JvmSynthetic
internal fun List<Wallet.Model.Cacao>.toSign(): List<Sign.Model.Cacao> = mutableListOf<Sign.Model.Cacao>().apply {
    this@toSign.forEach { cacao: Wallet.Model.Cacao ->
        with(cacao) {
            add(
                Sign.Model.Cacao(
                    Sign.Model.Cacao.Header(header.t),
                    Sign.Model.Cacao.Payload(
                        payload.iss,
                        payload.domain,
                        payload.aud,
                        payload.version,
                        payload.nonce,
                        payload.iat,
                        payload.nbf,
                        payload.exp,
                        payload.statement,
                        payload.requestId,
                        payload.resources
                    ),
                    Sign.Model.Cacao.Signature(signature.t, signature.s, signature.m)
                )
            )
        }
    }
}

@JvmSynthetic
internal fun Sign.Model.Cacao.toWallet(): Wallet.Model.Cacao = with(this) {
    Wallet.Model.Cacao(
        Wallet.Model.Cacao.Header(header.t),
        Wallet.Model.Cacao.Payload(
            payload.iss,
            payload.domain,
            payload.aud,
            payload.version,
            payload.nonce,
            payload.iat,
            payload.nbf,
            payload.exp,
            payload.statement,
            payload.requestId,
            payload.resources
        ),
        Wallet.Model.Cacao.Signature(signature.t, signature.s, signature.m)
    )
}