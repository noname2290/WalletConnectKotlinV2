package com.walletconnect.notify.data.jwt

import com.walletconnect.foundation.util.jwt.JwtClaims

interface NotifyJwtBase: JwtClaims {
    val action: String
    val issuedAt: Long
    val expiration: Long
    val keyserverUrl: String
}