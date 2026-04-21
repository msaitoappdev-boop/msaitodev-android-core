package com.msaitodev.feature.billing

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.msaitodev.core.navigation.PaywallDestination

fun NavGraphBuilder.paywallGraph(
    onBack: () -> Unit
) {
    composable(PaywallDestination.route) {
        PaywallRoute(onBackClick = onBack)
    }
}