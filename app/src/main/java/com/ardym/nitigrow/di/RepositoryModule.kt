package com.ardym.nitigrow.di

import com.ardym.nitigrow.data.repository.AnalyticsRepositoryImpl
import com.ardym.nitigrow.data.repository.AuthRepositoryImpl
import com.ardym.nitigrow.data.repository.BillingRepositoryImpl
import com.ardym.nitigrow.data.repository.CampaignRepositoryImpl
import com.ardym.nitigrow.data.repository.ChatRepositoryImpl
import com.ardym.nitigrow.data.repository.ContactRepositoryImpl
import com.ardym.nitigrow.data.repository.DashboardRepositoryImpl
import com.ardym.nitigrow.data.repository.InboxRepositoryImpl
import com.ardym.nitigrow.data.repository.LeadRepositoryImpl
import com.ardym.nitigrow.data.repository.PaymentLinksRepositoryImpl
import com.ardym.nitigrow.data.repository.ProfileRepositoryImpl
import com.ardym.nitigrow.data.repository.PushTokenRepositoryImpl
import com.ardym.nitigrow.data.repository.ReferralsRepositoryImpl
import com.ardym.nitigrow.data.repository.SettingsRepositoryImpl
import com.ardym.nitigrow.data.repository.TemplatesRepositoryImpl
import com.ardym.nitigrow.domain.repository.AnalyticsRepository
import com.ardym.nitigrow.domain.repository.AuthRepository
import com.ardym.nitigrow.domain.repository.BillingRepository
import com.ardym.nitigrow.domain.repository.CampaignRepository
import com.ardym.nitigrow.domain.repository.ChatRepository
import com.ardym.nitigrow.domain.repository.ContactRepository
import com.ardym.nitigrow.domain.repository.DashboardRepository
import com.ardym.nitigrow.domain.repository.InboxRepository
import com.ardym.nitigrow.domain.repository.LeadRepository
import com.ardym.nitigrow.domain.repository.PaymentLinksRepository
import com.ardym.nitigrow.domain.repository.ProfileRepository
import com.ardym.nitigrow.domain.repository.PushTokenRepository
import com.ardym.nitigrow.domain.repository.ReferralsRepository
import com.ardym.nitigrow.domain.repository.SettingsRepository
import com.ardym.nitigrow.domain.repository.TemplatesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindInboxRepository(impl: InboxRepositoryImpl): InboxRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindLeadRepository(impl: LeadRepositoryImpl): LeadRepository

    @Binds
    @Singleton
    abstract fun bindCampaignRepository(impl: CampaignRepositoryImpl): CampaignRepository

    @Binds
    @Singleton
    abstract fun bindPushTokenRepository(impl: PushTokenRepositoryImpl): PushTokenRepository

    @Binds
    @Singleton
    abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindReferralsRepository(impl: ReferralsRepositoryImpl): ReferralsRepository

    @Binds
    @Singleton
    abstract fun bindTemplatesRepository(impl: TemplatesRepositoryImpl): TemplatesRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPaymentLinksRepository(impl: PaymentLinksRepositoryImpl): PaymentLinksRepository
}
