@file:JvmSynthetic

package com.walletconnect.sync.di


import com.walletconnect.android.internal.common.signing.message.MessageSignatureVerifier
import com.walletconnect.sync.engine.domain.SyncEngine
import com.walletconnect.sync.engine.use_case.calls.*
import com.walletconnect.sync.engine.use_case.requests.OnDeleteRequestUseCase
import com.walletconnect.sync.engine.use_case.requests.OnSetRequestUseCase
import com.walletconnect.sync.engine.use_case.responses.OnDeleteResponseUseCase
import com.walletconnect.sync.engine.use_case.responses.OnSetResponseUseCase
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule() = module {
    single { MessageSignatureVerifier(get()) }

    single { CreateStoreUseCase(get(), get()) }
    single { DeleteStoreValueUseCase(get()) }
    single { SetStoreValueUseCase(get()) }
    single { GetStoresUseCase(get()) }
    single { RegisterAccountUseCase(get(), get()) }

    single { OnSetRequestUseCase(get(), get()) }
    single { OnDeleteRequestUseCase(get(), get()) }

    single { OnSetResponseUseCase() }
    single { OnDeleteResponseUseCase() }

    single {
        SyncEngine(
            getStoresUseCase = get(),
            registerAccountUseCase = get(),
            createStoreUseCase = get(),
            deleteStoreValueUseCase = get(),
            setStoreValueUseCase = get(),
            pairingHandler = get(),
            jsonRpcInteractor = get(),
            onSetRequestUseCase = get(),
            onDeleteRequestUseCase = get(),
            onSetResponseUseCase = get(),
            onDeleteResponseUseCase = get(),
        )
    }
}