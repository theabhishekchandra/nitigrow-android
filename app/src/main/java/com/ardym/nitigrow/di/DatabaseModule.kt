package com.ardym.nitigrow.di

import android.content.Context
import androidx.room.Room
import com.ardym.nitigrow.core.util.Constants
import com.ardym.nitigrow.data.local.NitiGrowDatabase
import com.ardym.nitigrow.data.local.dao.CampaignDao
import com.ardym.nitigrow.data.local.dao.ContactDao
import com.ardym.nitigrow.data.local.dao.ConversationDao
import com.ardym.nitigrow.data.local.dao.DashboardDao
import com.ardym.nitigrow.data.local.dao.LeadDao
import com.ardym.nitigrow.data.local.dao.MessageDao
import com.ardym.nitigrow.data.local.dao.PaymentDao
import com.ardym.nitigrow.data.local.dao.PlanDao
import com.ardym.nitigrow.data.local.dao.ProfileDao
import com.ardym.nitigrow.data.local.dao.SubscriptionDao
import com.ardym.nitigrow.data.local.dao.TeamDao
import com.ardym.nitigrow.data.local.dao.TemplateDao
import com.ardym.nitigrow.data.local.dao.TenantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NitiGrowDatabase =
        Room.databaseBuilder(context, NitiGrowDatabase::class.java, Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDashboardDao(db: NitiGrowDatabase): DashboardDao = db.dashboardDao()

    @Provides
    fun provideConversationDao(db: NitiGrowDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: NitiGrowDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideContactDao(db: NitiGrowDatabase): ContactDao = db.contactDao()

    @Provides
    fun provideLeadDao(db: NitiGrowDatabase): LeadDao = db.leadDao()

    @Provides
    fun provideCampaignDao(db: NitiGrowDatabase): CampaignDao = db.campaignDao()

    @Provides
    fun provideTemplateDao(db: NitiGrowDatabase): TemplateDao = db.templateDao()

    @Provides
    fun providePlanDao(db: NitiGrowDatabase): PlanDao = db.planDao()

    @Provides
    fun provideSubscriptionDao(db: NitiGrowDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun providePaymentDao(db: NitiGrowDatabase): PaymentDao = db.paymentDao()

    @Provides
    fun provideProfileDao(db: NitiGrowDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideTenantDao(db: NitiGrowDatabase): TenantDao = db.tenantDao()

    @Provides
    fun provideTeamDao(db: NitiGrowDatabase): TeamDao = db.teamDao()
}
