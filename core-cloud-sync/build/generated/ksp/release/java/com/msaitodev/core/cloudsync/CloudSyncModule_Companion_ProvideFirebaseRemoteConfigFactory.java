package com.msaitodev.core.cloudsync;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class CloudSyncModule_Companion_ProvideFirebaseRemoteConfigFactory implements Factory<FirebaseRemoteConfig> {
  @Override
  public FirebaseRemoteConfig get() {
    return provideFirebaseRemoteConfig();
  }

  public static CloudSyncModule_Companion_ProvideFirebaseRemoteConfigFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseRemoteConfig provideFirebaseRemoteConfig() {
    return Preconditions.checkNotNullFromProvides(CloudSyncModule.Companion.provideFirebaseRemoteConfig());
  }

  private static final class InstanceHolder {
    private static final CloudSyncModule_Companion_ProvideFirebaseRemoteConfigFactory INSTANCE = new CloudSyncModule_Companion_ProvideFirebaseRemoteConfigFactory();
  }
}
