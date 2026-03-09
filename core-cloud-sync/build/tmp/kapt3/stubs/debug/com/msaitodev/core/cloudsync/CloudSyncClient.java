package com.msaitodev.core.cloudsync;

/**
 * クラウド同期（Firestore）の低レベル操作を担うクライアント。
 * 特定のドメイン（クイズ等）に依存しない汎用的な設計。
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J,\u0010\u0002\u001a\u0010\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00032\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0004H\u00a6@\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\b\u001a\u0004\u0018\u00010\u0004H\u00a6@\u00a2\u0006\u0002\u0010\tJ2\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00042\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u0003H\u00a6@\u00a2\u0006\u0002\u0010\r\u00a8\u0006\u000e"}, d2 = {"Lcom/msaitodev/core/cloudsync/CloudSyncClient;", "", "download", "", "", "collection", "documentId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserId", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upload", "", "data", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "core-cloud-sync_debug"})
public abstract interface CloudSyncClient {
    
    /**
     * 現在のユーザーIDを取得（未サインイン時は匿名サインインを試行）
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserId(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * 指定されたコレクションとドキュメントにデータをアップロード
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upload(@org.jetbrains.annotations.NotNull()
    java.lang.String collection, @org.jetbrains.annotations.NotNull()
    java.lang.String documentId, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.lang.Object> data, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * 指定されたコレクションとドキュメントからデータをダウンロード
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object download(@org.jetbrains.annotations.NotNull()
    java.lang.String collection, @org.jetbrains.annotations.NotNull()
    java.lang.String documentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.Map<java.lang.String, ? extends java.lang.Object>> $completion);
}