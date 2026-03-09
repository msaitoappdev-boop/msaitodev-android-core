package com.msaitodev.core.cloudsync;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\tH\'\u00a8\u0006\u000b"}, d2 = {"Lcom/msaitodev/core/cloudsync/CloudSyncModule;", "", "()V", "bindCloudConfigClient", "Lcom/msaitodev/core/cloudsync/CloudConfigClient;", "impl", "Lcom/msaitodev/core/cloudsync/CloudConfigClientImpl;", "bindCloudSyncClient", "Lcom/msaitodev/core/cloudsync/CloudSyncClient;", "Lcom/msaitodev/core/cloudsync/CloudSyncClientImpl;", "Companion", "core-cloud-sync_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class CloudSyncModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.msaitodev.core.cloudsync.CloudSyncModule.Companion Companion = null;
    
    public CloudSyncModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.msaitodev.core.cloudsync.CloudSyncClient bindCloudSyncClient(@org.jetbrains.annotations.NotNull()
    com.msaitodev.core.cloudsync.CloudSyncClientImpl impl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.msaitodev.core.cloudsync.CloudConfigClient bindCloudConfigClient(@org.jetbrains.annotations.NotNull()
    com.msaitodev.core.cloudsync.CloudConfigClientImpl impl);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007\u00a8\u0006\t"}, d2 = {"Lcom/msaitodev/core/cloudsync/CloudSyncModule$Companion;", "", "()V", "provideFirebaseAuth", "Lcom/google/firebase/auth/FirebaseAuth;", "provideFirebaseFirestore", "Lcom/google/firebase/firestore/FirebaseFirestore;", "provideFirebaseRemoteConfig", "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;", "core-cloud-sync_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.google.firebase.auth.FirebaseAuth provideFirebaseAuth() {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.google.firebase.firestore.FirebaseFirestore provideFirebaseFirestore() {
            return null;
        }
        
        @dagger.Provides()
        @javax.inject.Singleton()
        @org.jetbrains.annotations.NotNull()
        public final com.google.firebase.remoteconfig.FirebaseRemoteConfig provideFirebaseRemoteConfig() {
            return null;
        }
    }
}