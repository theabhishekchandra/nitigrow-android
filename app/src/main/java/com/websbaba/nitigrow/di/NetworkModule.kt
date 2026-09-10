package com.websbaba.nitigrow.di

import com.websbaba.nitigrow.BuildConfig
import com.websbaba.nitigrow.core.network.AuthInterceptor
import com.websbaba.nitigrow.core.network.TokenAuthenticator
import com.websbaba.nitigrow.core.util.Constants
import com.websbaba.nitigrow.data.remote.api.AuthApi
import com.websbaba.nitigrow.data.remote.api.CampaignsApi
import com.websbaba.nitigrow.data.remote.api.ChatApi
import com.websbaba.nitigrow.data.remote.api.CommerceApi
import com.websbaba.nitigrow.data.remote.api.FlowsApi
import com.websbaba.nitigrow.data.remote.api.ContactsApi
import com.websbaba.nitigrow.data.remote.api.DashboardApi
import com.websbaba.nitigrow.data.remote.api.InboxApi
import com.websbaba.nitigrow.data.remote.api.LeadsApi
import com.websbaba.nitigrow.data.remote.api.PushApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // Never log bodies: they carry auth/refresh tokens and customer chat
            // payloads, and debug builds point at the production API. Cap at
            // HEADERS with the Authorization header redacted.
            level = if (BuildConfig.ENABLE_LOGGING) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }

    /**
     * Stamps every outbound request with `x-client: mobile`. The backend uses
     * this to take the native auth path (refresh token in the JSON body instead
     * of an httpOnly cookie, and no CSRF requirement).
     */
    @Provides
    @Singleton
    @Named("clientHeader")
    fun provideClientHeaderInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("x-client", "mobile")
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        @Named("clientHeader") clientHeaderInterceptor: Interceptor,
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(clientHeaderInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .authenticator(authenticator)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi =
        retrofit.create(DashboardApi::class.java)

    @Provides
    @Singleton
    fun provideInboxApi(retrofit: Retrofit): InboxApi =
        retrofit.create(InboxApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi =
        retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideCommerceApi(retrofit: Retrofit): CommerceApi =
        retrofit.create(CommerceApi::class.java)

    @Provides
    @Singleton
    fun provideFlowsApi(retrofit: Retrofit): FlowsApi =
        retrofit.create(FlowsApi::class.java)

    @Provides
    @Singleton
    fun provideContactsApi(retrofit: Retrofit): ContactsApi =
        retrofit.create(ContactsApi::class.java)

    @Provides
    @Singleton
    fun provideLeadsApi(retrofit: Retrofit): LeadsApi =
        retrofit.create(LeadsApi::class.java)

    @Provides
    @Singleton
    fun provideCampaignsApi(retrofit: Retrofit): CampaignsApi =
        retrofit.create(CampaignsApi::class.java)

    @Provides
    @Singleton
    fun providePushApi(retrofit: Retrofit): PushApi =
        retrofit.create(PushApi::class.java)

    @Provides
    @Singleton
    fun provideReferralsApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.ReferralsApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.ReferralsApi::class.java)

    @Provides
    @Singleton
    fun provideTemplatesApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.TemplatesApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.TemplatesApi::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.AnalyticsApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.AnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideSettingsApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.SettingsApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.SettingsApi::class.java)

    @Provides
    @Singleton
    fun providePaymentLinksApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.PaymentLinksApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.PaymentLinksApi::class.java)

    @Provides
    @Singleton
    fun provideBillingApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.BillingApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.BillingApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): com.websbaba.nitigrow.data.remote.api.ProfileApi =
        retrofit.create(com.websbaba.nitigrow.data.remote.api.ProfileApi::class.java)
}
