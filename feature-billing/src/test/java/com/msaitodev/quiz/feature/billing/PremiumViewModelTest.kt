package com.msaitodev.quiz.feature.billing

import android.app.Activity
import app.cash.turbine.test
import com.android.billingclient.api.ProductDetails
import com.google.common.truth.Truth.assertThat
import com.msaitodev.core.common.billing.BillingManager
import com.msaitodev.core.common.billing.BillingProvider
import com.msaitodev.core.common.billing.PaywallConfig
import com.msaitodev.core.common.billing.PlanItemConfig
import com.msaitodev.core.common.billing.PremiumPlan
import com.msaitodev.feature.billing.PaywallEvent
import com.msaitodev.feature.billing.PremiumViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class PremiumViewModelTest {

    private val billingManager: BillingManager = mock(BillingManager::class.java)
    private val billingProvider: BillingProvider = mock(BillingProvider::class.java)
    private val activity: Activity = mock(Activity::class.java)
    private lateinit var viewModel: PremiumViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val fakeConfig = PaywallConfig(
        title = "Title",
        headline = "Headline",
        monthly = PlanItemConfig("Monthly", "Price", "Buy", "Owned", "Desc", listOf("Benefit")),
        lifetime = PlanItemConfig("Lifetime", "Price", "Buy", "Owned", "Desc", listOf("Benefit"))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        `when`(billingProvider.paywallConfig).thenReturn(fakeConfig)
        `when`(billingManager.premiumPlan).thenReturn(MutableStateFlow(PremiumPlan.NONE))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState reflects premium status`() = runTest {
        val premiumPlanFlow = MutableStateFlow(PremiumPlan.NONE)
        `when`(billingManager.premiumPlan).thenReturn(premiumPlanFlow)
        viewModel = PremiumViewModel(billingManager, billingProvider)

        viewModel.uiState.test {
            assertThat(awaitItem().premiumPlan).isEqualTo(PremiumPlan.NONE)

            premiumPlanFlow.value = PremiumPlan.MONTHLY
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem().premiumPlan).isEqualTo(PremiumPlan.MONTHLY)
        }
    }

    @Test
    fun `onPurchaseClick launches billing flow when product details are available`() = runTest {
        val productDetails: ProductDetails = mock(ProductDetails::class.java)
        `when`(billingProvider.productIdLifetime).thenReturn("lifetime_id")
        `when`(billingManager.queryProductDetails("lifetime_id", "inapp")).thenReturn(productDetails)
        viewModel = PremiumViewModel(billingManager, billingProvider)

        viewModel.onPurchaseClick(activity, PremiumPlan.LIFETIME)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(billingManager).launchPurchase(activity, productDetails)
    }

    @Test
    fun `refresh calls billingManager`() = runTest {
        viewModel = PremiumViewModel(billingManager, billingProvider)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(billingManager).refreshEntitlements()
    }

    @Test
    fun `devTogglePremium calls billingManager`() = runTest {
        viewModel = PremiumViewModel(billingManager, billingProvider)
        viewModel.devTogglePremium(PremiumPlan.LIFETIME)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(billingManager).setPremiumForDebug(PremiumPlan.LIFETIME)
    }
}
