package com.websbaba.nitigrow.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.websbaba.nitigrow.core.util.Constants
import com.websbaba.nitigrow.data.local.NitiGrowDatabase
import com.websbaba.nitigrow.data.local.dao.BillingStatusDao
import com.websbaba.nitigrow.data.local.dao.CampaignDao
import com.websbaba.nitigrow.data.local.dao.ContactDao
import com.websbaba.nitigrow.data.local.dao.ConversationDao
import com.websbaba.nitigrow.data.local.dao.DashboardDao
import com.websbaba.nitigrow.data.local.dao.InvoiceDao
import com.websbaba.nitigrow.data.local.dao.LeadDao
import com.websbaba.nitigrow.data.local.dao.MessageDao
import com.websbaba.nitigrow.data.local.dao.ProfileDao
import com.websbaba.nitigrow.data.local.dao.TeamDao
import com.websbaba.nitigrow.data.local.dao.TemplateDao
import com.websbaba.nitigrow.data.local.dao.TenantDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // v1 -> v2 added `tenantId` to the conversations and messages cache tables.
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `conversations` ADD COLUMN `tenantId` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `messages` ADD COLUMN `tenantId` TEXT NOT NULL DEFAULT ''")
        }
    }

    // v2 -> v3 billing went read-only: drop the Razorpay-era plans/subscription/payments
    // cache and create the new billing_status snapshot + invoices cache. These tables are
    // disposable (re-synced from the API), so dropping them loses nothing.
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS `plans`")
            db.execSQL("DROP TABLE IF EXISTS `subscription`")
            db.execSQL("DROP TABLE IF EXISTS `payments`")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `billing_status` (" +
                    "`id` INTEGER NOT NULL, `plan` TEXT NOT NULL, `accountStatus` TEXT, " +
                    "`subStatus` TEXT, `trialEndsAtEpochMs` INTEGER, `periodStartEpochMs` INTEGER, " +
                    "`periodEndEpochMs` INTEGER, `gatewaySubscriptionId` TEXT, " +
                    "`cancelAtPeriodEnd` INTEGER NOT NULL, `billingCycle` TEXT, " +
                    "`msgUsed` INTEGER NOT NULL, `msgLimit` INTEGER NOT NULL, " +
                    "`aiUsed` INTEGER NOT NULL, `aiLimit` INTEGER NOT NULL, " +
                    "`contactsUsed` INTEGER NOT NULL, `contactsLimit` INTEGER NOT NULL, " +
                    "`usersUsed` INTEGER NOT NULL, `usersLimit` INTEGER NOT NULL, " +
                    "`pricesJson` TEXT NOT NULL, `referralCreditPaise` INTEGER NOT NULL, " +
                    "`branding` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `invoices` (" +
                    "`id` TEXT NOT NULL, `number` TEXT, `status` TEXT, " +
                    "`amountPaise` INTEGER NOT NULL, `paidAtEpochMs` INTEGER, PRIMARY KEY(`id`))"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_paidAtEpochMs` ON `invoices` (`paidAtEpochMs`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NitiGrowDatabase =
        Room.databaseBuilder(context, NitiGrowDatabase::class.java, Constants.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            // Destructive only on downgrade (dev rollbacks) — never silently wipe
            // user data on a forward upgrade.
            .fallbackToDestructiveMigrationOnDowngrade()
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
    fun provideBillingStatusDao(db: NitiGrowDatabase): BillingStatusDao = db.billingStatusDao()

    @Provides
    fun provideInvoiceDao(db: NitiGrowDatabase): InvoiceDao = db.invoiceDao()

    @Provides
    fun provideProfileDao(db: NitiGrowDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideTenantDao(db: NitiGrowDatabase): TenantDao = db.tenantDao()

    @Provides
    fun provideTeamDao(db: NitiGrowDatabase): TeamDao = db.teamDao()
}
