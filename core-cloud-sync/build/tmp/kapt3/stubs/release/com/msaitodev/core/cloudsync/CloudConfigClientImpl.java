package com.msaitodev.core.cloudsync;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u0007\u001a\u00020\bH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/msaitodev/core/cloudsync/CloudConfigClientImpl;", "Lcom/msaitodev/core/cloudsync/CloudConfigClient;", "remoteConfig", "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;", "(Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;)V", "getBoolean", "", "key", "", "getLong", "", "core-cloud-sync_release"})
public final class CloudConfigClientImpl implements com.msaitodev.core.cloudsync.CloudConfigClient {
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.remoteconfig.FirebaseRemoteConfig remoteConfig = null;
    
    @javax.inject.Inject()
    public CloudConfigClientImpl(@org.jetbrains.annotations.NotNull()
    com.google.firebase.remoteconfig.FirebaseRemoteConfig remoteConfig) {
        super();
    }
    
    @java.lang.Override()
    public long getLong(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return 0L;
    }
    
    @java.lang.Override()
    public boolean getBoolean(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return false;
    }
}