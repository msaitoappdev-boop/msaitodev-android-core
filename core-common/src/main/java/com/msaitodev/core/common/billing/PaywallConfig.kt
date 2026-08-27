package com.msaitodev.core.common.billing

/**
 * ペイウォール画面に表示するテキスト群を定義するデータクラス。
 */
data class PaywallConfig(
    val title: String,
    val headline: String,
    val monthly: PlanItemConfig,
    val lifetime: PlanItemConfig
)

/**
 * 各プランごとの詳細設定。
 */
data class PlanItemConfig(
    val planTitle: String,
    val planPrice: String,
    val purchaseButtonText: String,
    val purchasedButtonText: String,
    val description: String,
    val benefits: List<String>
)
