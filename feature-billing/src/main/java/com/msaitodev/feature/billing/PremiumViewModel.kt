package com.msaitodev.feature.billing

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.msaitodev.core.common.billing.BillingManager
import com.msaitodev.core.common.billing.BillingProvider
import com.msaitodev.core.common.billing.PaywallConfig
import com.msaitodev.core.common.billing.PremiumPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaywallUiState(
    val premiumPlan: PremiumPlan = PremiumPlan.NONE,
    val config: PaywallConfig? = null
) {
    /** 既存コードとの互換性のためのプロパティ */
    val isPremium: Boolean get() = premiumPlan != PremiumPlan.NONE
}

sealed interface PaywallEvent {
    data class ShowMessage(@StringRes val messageResId: Int) : PaywallEvent
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billing: BillingManager,
    private val billingProvider: BillingProvider
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = billing.premiumPlan
        .map { plan ->
            PaywallUiState(
                premiumPlan = plan,
                config = billingProvider.paywallConfig
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PaywallUiState(
                premiumPlan = billing.premiumPlan.value,
                config = billingProvider.paywallConfig
            )
        )

    private val _event = MutableSharedFlow<PaywallEvent>()
    val event: SharedFlow<PaywallEvent> = _event.asSharedFlow()

    fun onPurchaseClick(activity: Activity, plan: PremiumPlan) {
        viewModelScope.launch {
            val (productId, productType) = when (plan) {
                PremiumPlan.MONTHLY -> billingProvider.productIdMonthly to BillingClient.ProductType.SUBS
                PremiumPlan.LIFETIME -> billingProvider.productIdLifetime to BillingClient.ProductType.INAPP
                else -> return@launch
            }

            val productDetails = billing.queryProductDetails(productId, productType)
            if (productDetails == null) {
                _event.emit(PaywallEvent.ShowMessage(R.string.paywall_error_product_details))
                return@launch
            }
            billing.launchPurchase(activity, productDetails)
        }
    }

    fun refresh() {
        viewModelScope.launch { billing.refreshEntitlements() }
    }

    fun devTogglePremium(plan: PremiumPlan) {
        viewModelScope.launch { billing.setPremiumForDebug(plan) }
    }
}
