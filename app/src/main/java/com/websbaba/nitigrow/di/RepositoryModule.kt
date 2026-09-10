package com.websbaba.nitigrow.di

import com.websbaba.nitigrow.data.repository.AnalyticsRepositoryImpl
import com.websbaba.nitigrow.data.repository.AuthRepositoryImpl
import com.websbaba.nitigrow.data.repository.BillingRepositoryImpl
import com.websbaba.nitigrow.data.repository.CampaignRepositoryImpl
import com.websbaba.nitigrow.data.repository.ChatRepositoryImpl
import com.websbaba.nitigrow.data.repository.ContactRepositoryImpl
import com.websbaba.nitigrow.data.repository.DashboardRepositoryImpl
import com.websbaba.nitigrow.data.repository.InboxRepositoryImpl
import com.websbaba.nitigrow.data.repository.LeadRepositoryImpl
import com.websbaba.nitigrow.data.repository.PaymentLinksRepositoryImpl
import com.websbaba.nitigrow.data.repository.ProfileRepositoryImpl
import com.websbaba.nitigrow.data.repository.PushTokenRepositoryImpl
import com.websbaba.nitigrow.data.repository.CommerceRepositoryImpl
import com.websbaba.nitigrow.data.repository.FlowsRepositoryImpl
import com.websbaba.nitigrow.data.repository.ReferralsRepositoryImpl
import com.websbaba.nitigrow.data.repository.SettingsRepositoryImpl
import com.websbaba.nitigrow.data.repository.TemplatesRepositoryImpl
import com.websbaba.nitigrow.domain.repository.AnalyticsRepository
import com.websbaba.nitigrow.domain.repository.AuthRepository
import com.websbaba.nitigrow.domain.repository.BillingRepository
import com.websbaba.nitigrow.domain.repository.CampaignRepository
import com.websbaba.nitigrow.domain.repository.ChatRepository
import com.websbaba.nitigrow.domain.repository.ContactRepository
import com.websbaba.nitigrow.domain.repository.DashboardRepository
import com.websbaba.nitigrow.domain.repository.InboxRepository
import com.websbaba.nitigrow.domain.repository.LeadRepository
import com.websbaba.nitigrow.domain.repository.PaymentLinksRepository
import com.websbaba.nitigrow.domain.repository.ProfileRepository
import com.websbaba.nitigrow.domain.repository.PushTokenRepository
import com.websbaba.nitigrow.domain.repository.CommerceRepository
import com.websbaba.nitigrow.domain.repository.FlowsRepository
import com.websbaba.nitigrow.domain.repository.ReferralsRepository
import com.websbaba.nitigrow.domain.repository.SettingsRepository
import com.websbaba.nitigrow.domain.repository.TemplatesRepository
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
    abstract fun bindCommerceRepository(impl: CommerceRepositoryImpl): CommerceRepository

    @Binds
    @Singleton
    abstract fun bindFlowsRepository(impl: FlowsRepositoryImpl): FlowsRepository

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
