package com.msaitodev.core.common.config;

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
public final class AdUnits_Factory implements Factory<AdUnits> {
  private final Provider<String> interstitialUnitAProvider;

  private final Provider<String> rewardedUnitAProvider;

  public AdUnits_Factory(Provider<String> interstitialUnitAProvider,
      Provider<String> rewardedUnitAProvider) {
    this.interstitialUnitAProvider = interstitialUnitAProvider;
    this.rewardedUnitAProvider = rewardedUnitAProvider;
  }

  @Override
  public AdUnits get() {
    return newInstance(interstitialUnitAProvider.get(), rewardedUnitAProvider.get());
  }

  public static AdUnits_Factory create(Provider<String> interstitialUnitAProvider,
      Provider<String> rewardedUnitAProvider) {
    return new AdUnits_Factory(interstitialUnitAProvider, rewardedUnitAProvider);
  }

  public static AdUnits newInstance(String interstitialUnitA, String rewardedUnitA) {
    return new AdUnits(interstitialUnitA, rewardedUnitA);
  }
}
