package com.ardym.nitigrow.di

import com.ardym.nitigrow.data.repository.AuthRepositoryImpl
import com.ardym.nitigrow.data.repository.BillingRepositoryImpl
import com.ardym.nitigrow.data.repository.CampaignRepositoryImpl
import com.ardym.nitigrow.data.repository.ChatRepositoryImpl
import com.ardym.nitigrow.data.repository.ContactRepositoryImpl
import com.ardym.nitigrow.data.repository.DashboardRepositoryImpl
import com.ardym.nitigrow.data.repository.InboxRepositoryImpl
import com.ardym.nitigrow.data.repository.LeadRepositoryImpl
import com.ardym.nitigrow.data.repository.ProfileRepositoryImpl
import com.ardym.nitigrow.data.repository.PushTokenRepositoryImpl
import com.ardym.nitigrow.domain.repository.AuthRepository
import com.ardym.nitigrow.domain.repository.BillingRepository
import com.ardym.nitigrow.domain.repository.CampaignRepository
import com.ardym.nitigrow.domain.repository.ChatRepository
import com.ardym.nitigrow.domain.repository.ContactRepository
import com.ardym.nitigrow.domain.repository.DashboardRepository
import com.ardym.nitigrow.domain.repository.InboxRepository
import com.ardym.nitigrow.domain.repository.LeadRepository
import com.ardym.nitigrow.domain.repository.ProfileRepository
import com.ardym.nitigrow.domain.repository.PushTokenRepository
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
}
