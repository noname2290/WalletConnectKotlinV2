package com.walletconnect.web3.modal.ui.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.walletconnect.web3.modal.ui.components.internal.commons.CloseIconButton
import com.walletconnect.web3.modal.ui.components.internal.commons.QuestionMarkIconButton
import com.walletconnect.web3.modal.ui.components.internal.walletconnect.WalletConnectLogo
import com.walletconnect.web3.modal.ui.previews.ComponentPreview
import com.walletconnect.web3.modal.ui.theme.ProvideWeb3ModalThemeComposition
import com.walletconnect.web3.modal.ui.theme.Web3ModalTheme

@Composable
internal fun Web3ModalRoot(
    navController: NavController,
    closeModal: () -> Unit,
    content: @Composable () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.Bottom
    ) {
        ProvideWeb3ModalThemeComposition() {
            Column(
                modifier = Modifier.background(Web3ModalTheme.colors.main100)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletConnectLogo(modifier = Modifier.weight(1f))
                    QuestionMarkIconButton(navController)
                    Spacer(modifier = Modifier.width(16.dp))
                    CloseIconButton { closeModal() }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Web3ModalTheme.colors.background.color100,
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
@Preview
private fun PreviewWeb3ModalRoot() {
    val navController = rememberNavController()

    ComponentPreview {
        Web3ModalRoot(
            navController = navController,
            closeModal = {},
        ) {
            Box(modifier = Modifier.size(500.dp))
        }
    }
}
