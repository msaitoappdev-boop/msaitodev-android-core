package com.msaitodev.core.cloudsync;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class CloudConfigClientImpl_Factory implements Factory<CloudConfigClientImpl> {
  private final Provider<FirebaseRemoteConfig> remoteConfigProvider;

  public CloudConfigClientImpl_Factory(Provider<FirebaseRemoteConfig> remoteConfigProvider) {
    this.remoteConfigProvider = remoteConfigProvider;
  }

  @Override
  public CloudConfigClientImpl get() {
    return newInstance(remoteConfigProvider.get());
  }

  public static CloudConfigClientImpl_Factory create(
      Provider<FirebaseRemoteConfig> remoteConfigProvider) {
    return new CloudConfigClientImpl_Factory(remoteConfigProvider);
  }

  public static CloudConfigClientImpl newInstance(FirebaseRemoteConfig remoteConfig) {
    return new CloudConfigClientImpl(remoteConfig);
  }
}
