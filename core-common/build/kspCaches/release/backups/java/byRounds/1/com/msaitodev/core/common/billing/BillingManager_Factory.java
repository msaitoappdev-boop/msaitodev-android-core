package com.msaitodev.core.common.billing;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BillingManager_Factory implements Factory<BillingManager> {
  private final Provider<Context> appContextProvider;

  private final Provider<BillingProvider> billingProvider;

  public BillingManager_Factory(Provider<Context> appContextProvider,
      Provider<BillingProvider> billingProvider) {
    this.appContextProvider = appContextProvider;
    this.billingProvider = billingProvider;
  }

  @Override
  public BillingManager get() {
    return newInstance(appContextProvider.get(), billingProvider.get());
  }

  public static BillingManager_Factory create(Provider<Context> appContextProvider,
      Provider<BillingProvider> billingProvider) {
    return new BillingManager_Factory(appContextProvider, billingProvider);
  }

  public static BillingManager newInstance(Context appContext, BillingProvider billingProvider) {
    return new BillingManager(appContext, billingProvider);
  }
}
