package com.walletconnect.wcmodal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walletconnect.android.CoreClient
import com.walletconnect.android.internal.common.explorer.data.model.Wallet
import com.walletconnect.android.internal.common.explorer.domain.usecase.GetWalletsUseCaseInterface
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import com.walletconnect.wcmodal.domain.RecentWalletsRepository
import com.walletconnect.wcmodal.domain.WalletConnectModalDelegate
import com.walletconnect.wcmodal.domain.usecase.GetRecentWalletUseCase
import com.walletconnect.wcmodal.domain.usecase.SaveRecentWalletUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val WCM_SDK = "wcm"

internal class WalletConnectModalViewModel : ViewModel() {

    private val getWalletsUseCase: GetWalletsUseCaseInterface = wcKoinApp.koin.get()
    private val getRecentWalletUseCase: GetRecentWalletUseCase = wcKoinApp.koin.get()
    private val saveRecentWalletUseCase: SaveRecentWalletUseCase = wcKoinApp.koin.get()

    private val pairing by lazy {
        CoreClient.Pairing.create { error ->
            throw IllegalStateException("Creating Pairing failed: ${error.throwable.stackTraceToString()}")
        }!!
    }

    private var wallets = emptyList<Wallet>()

    private val _modalState: MutableStateFlow<WalletConnectModalState> = MutableStateFlow(WalletConnectModalState.Loading)

    val modalState: StateFlow<WalletConnectModalState>
        get() = _modalState

    init {
        initModalState()
    }

    private fun initModalState() {
        val sessionParams = WalletConnectModal.sessionParams ?: throw IllegalStateException("Session params missing")
        try {
            val connectParams = Modal.Params.Connect(
                sessionParams.requiredNamespaces,
                sessionParams.optionalNamespaces,
                sessionParams.properties,
                pairing
            )
            val chains = sessionParams.requiredNamespaces.values.toList().map { it.chains?.joinToString() }.filterNotNull().joinToString()
            WalletConnectModal.connect(
                connect = connectParams,
                onSuccess = { createModalState(pairing.uri, chains) },
                onError = { handleError(it.throwable) }
            )
        } catch (e: Exception) {
            handleError(e)
        }
    }

    internal fun retry(onSuccess: () -> Unit) {
        (_modalState.value as? WalletConnectModalState.Connect)?.let {
            try {
                val sessionParams = WalletConnectModal.sessionParams ?: throw IllegalStateException("Session params missing")
                val connectParams = Modal.Params.Connect(
                    sessionParams.requiredNamespaces,
                    sessionParams.optionalNamespaces,
                    sessionParams.properties,
                    pairing
                )
                WalletConnectModal.connect(
                    connect = connectParams,
                    onSuccess = {
                        _modalState.value = (_modalState.value as WalletConnectModalState.Connect).copy(uri = pairing.uri)
                        onSuccess()
                    },
                    onError = { Timber.e(it.throwable) }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        } ?: Timber.e("Invalid modal state")
    }

    private fun handleError(error: Throwable) {
        Timber.e(error)
        _modalState.value = WalletConnectModalState.Error(error)
    }

    private fun createModalState(uri: String, chains: String) {
        viewModelScope.launch {
            try {
                wallets = if (WalletConnectModal.recommendedWalletsIds.isEmpty()) {
                    getWalletsUseCase(sdkType = WCM_SDK, chains = chains, excludedIds = WalletConnectModal.excludedWalletsIds)
                } else {
                    getWalletsUseCase(sdkType = WCM_SDK, chains = chains, excludedIds = WalletConnectModal.excludedWalletsIds, recommendedIds = WalletConnectModal.recommendedWalletsIds).union(
                        getWalletsUseCase(sdkType = WCM_SDK, chains = chains, excludedIds = WalletConnectModal.excludedWalletsIds)
                    ).toList()
                }
                _modalState.value = WalletConnectModalState.Connect(uri, wallets.mapRecentWallet(getRecentWalletUseCase()))
            } catch (e: Exception) {
                Timber.e(e)
                _modalState.value = WalletConnectModalState.Connect(uri)
            }
        }
    }

    fun updateRecentWalletId(id: String) = with(_modalState) {
        saveRecentWalletUseCase(id)
        value = value?.copy(wallets = wallets.mapRecentWallet(id))
    }
}

private fun List<Wallet>.mapRecentWallet(id: String?) = map {
    it.apply { it.isRecent = it.id == id }
}.sortedWith(compareByDescending<Wallet> { it.isRecent }.thenByDescending { it.isWalletInstalled })