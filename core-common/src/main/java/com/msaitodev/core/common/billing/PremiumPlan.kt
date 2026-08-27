package com.msaitodev.core.common.billing

/**
 * プレミアムプランの状態を定義する列挙型。
 */
enum class PremiumPlan {
    /**
     * 未加入（無料版）
     */
    NONE,

    /**
     * 月額プラン（1日の上限あり）
     */
    MONTHLY,

    /**
     * 全問解放プラン（買い切り・無制限）
     */
    LIFETIME
}
