package com.msaitodev.feature.billing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msaitodev.core.common.billing.PremiumPlan

private const val BENEFIT_LINE_BREAK = "\n"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    onPurchaseClick: (PremiumPlan) -> Unit,
    onBackClick: () -> Unit
) {
    val config = uiState.config ?: return
    // デフォルトの選択プランを月額プラン(MONTHLY)に変更
    var selectedPlan by remember { mutableStateOf(PremiumPlan.MONTHLY) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(config.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = config.headline,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))

            // プラン選択エリア
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlanCard(
                    modifier = Modifier.weight(1f),
                    title = config.monthly.planTitle,
                    price = config.monthly.planPrice,
                    isSelected = selectedPlan == PremiumPlan.MONTHLY,
                    isPurchased = uiState.premiumPlan == PremiumPlan.MONTHLY,
                    onClick = { selectedPlan = PremiumPlan.MONTHLY }
                )
                PlanCard(
                    modifier = Modifier.weight(1f),
                    title = config.lifetime.planTitle,
                    price = config.lifetime.planPrice,
                    isSelected = selectedPlan == PremiumPlan.LIFETIME,
                    isPurchased = uiState.premiumPlan == PremiumPlan.LIFETIME,
                    onClick = { selectedPlan = PremiumPlan.LIFETIME }
                )
            }

            Spacer(Modifier.height(24.dp))

            // 選択中プランのメリット表示
            val currentPlanConfig = if (selectedPlan == PremiumPlan.MONTHLY) config.monthly else config.lifetime
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = currentPlanConfig.benefits.joinToString(BENEFIT_LINE_BREAK),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // 購入ボタンの有効判定：未加入時のみ有効とする
            val isButtonEnabled = uiState.premiumPlan == PremiumPlan.NONE
            
            Button(
                onClick = { onPurchaseClick(selectedPlan) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isButtonEnabled
            ) {
                val buttonText = when {
                    uiState.premiumPlan == selectedPlan -> currentPlanConfig.purchasedButtonText
                    uiState.isPremium -> "プレミアムプラン適用中" // いずれかのプラン加入中の汎用文言
                    else -> currentPlanConfig.purchaseButtonText
                }
                Text(buttonText, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = currentPlanConfig.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    modifier: Modifier,
    title: String,
    price: String,
    isSelected: Boolean,
    isPurchased: Boolean,
    onClick: () -> Unit
) {
    val border = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.clickable { onClick() },
        border = border,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isPurchased) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "適用中",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
