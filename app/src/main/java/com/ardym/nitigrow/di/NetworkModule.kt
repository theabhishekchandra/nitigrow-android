package com.ardym.nitigrow.di

import com.ardym.nitigrow.BuildConfig
import com.ardym.nitigrow.core.network.AuthInterceptor
import com.ardym.nitigrow.core.network.RefreshTokenApi
import com.ardym.nitigrow.core.network.TokenAuthenticator
import com.ardym.nitigrow.core.util.Constants
import com.ardym.nitigrow.data.remote.api.AuthApi
import com.ardym.nitigrow.data.remote.api.CampaignsApi
import com.ardym.nitigrow.data.remote.api.ChatApi
import com.ardym.nitigrow.data.remote.api.ContactsApi
import com.ardym.nitigrow.data.remote.api.DashboardApi
import com.ardym.nitigrow.data.remote.api.InboxApi
import com.ardym.nitigrow.data.remote.api.LeadsApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
            level = if (BuildConfig.ENABLE_LOGGING) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
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
    fun provideRefreshTokenApi(retrofit: Retrofit): RefreshTokenApi =
        retrofit.create(RefreshTokenApi::class.java)

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
    fun providePushApi(retrofit: Retrofit): com.ardym.nitigrow.data.remote.api.PushApi =
        retrofit.create(com.ardym.nitigrow.data.remote.api.PushApi::class.java)

    @Provides
    @Singleton
    fun provideBillingApi(retrofit: Retrofit): com.ardym.nitigrow.data.remote.api.BillingApi =
        retrofit.create(com.ardym.nitigrow.data.remote.api.BillingApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): com.ardym.nitigrow.data.remote.api.ProfileApi =
        retrofit.create(com.ardym.nitigrow.data.remote.api.ProfileApi::class.java)
}
