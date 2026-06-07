package com.ardym.nitigrow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ardym.nitigrow.data.local.dao.AppMetaDao
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
import com.ardym.nitigrow.data.local.entity.AppMetaEntity
import com.ardym.nitigrow.data.local.entity.CampaignEntity
import com.ardym.nitigrow.data.local.entity.ContactEntity
import com.ardym.nitigrow.data.local.entity.ConversationEntity
import com.ardym.nitigrow.data.local.entity.DashboardStatsEntity
import com.ardym.nitigrow.data.local.entity.LeadEntity
import com.ardym.nitigrow.data.local.entity.MessageEntity
import com.ardym.nitigrow.data.local.entity.PaymentEntity
import com.ardym.nitigrow.data.local.entity.PlanEntity
import com.ardym.nitigrow.data.local.entity.ProfileEntity
import com.ardym.nitigrow.data.local.entity.SubscriptionEntity
import com.ardym.nitigrow.data.local.entity.TeamMemberEntity
import com.ardym.nitigrow.data.local.entity.TemplateEntity
import com.ardym.nitigrow.data.local.entity.TenantEntity

/**
 * Add entities and DAOs as features land. Bump `version` + provide Migration on schema change.
 *
 * v2: added `tenantId` to ConversationEntity + MessageEntity for multi-tenant
 * scoping. No hand-written Migration is supplied because the database is built
 * with `fallbackToDestructiveMigration()` (see DatabaseModule) — acceptable
 * pre-launch, where the local cache is disposable and re-synced from the API.
 * Replace with an explicit Migration before the first production release.
 */
@Database(
    entities = [
        AppMetaEntity::class,
        DashboardStatsEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ContactEntity::class,
        LeadEntity::class,
        CampaignEntity::class,
        TemplateEntity::class,
        PlanEntity::class,
        SubscriptionEntity::class,
        PaymentEntity::class,
        ProfileEntity::class,
        TenantEntity::class,
        TeamMemberEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NitiGrowDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun leadDao(): LeadDao
    abstract fun campaignDao(): CampaignDao
    abstract fun templateDao(): TemplateDao
    abstract fun planDao(): PlanDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun profileDao(): ProfileDao
    abstract fun tenantDao(): TenantDao
    abstract fun teamDao(): TeamDao
}
