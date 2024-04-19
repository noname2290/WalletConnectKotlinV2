@file:JvmSynthetic

package com.walletconnect.android.relay

sealed class WSSConnectionState {
	object Connected : WSSConnectionState()
	data class Disconnected(val message: String? = null) : WSSConnectionState()
}
