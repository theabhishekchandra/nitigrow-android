package com.ardym.nitigrow

import android.app.Application
import androidx.work.Configuration
import com.ardym.nitigrow.core.notifications.NotificationChannels
import com.ardym.nitigrow.core.storage.TokenDataStore
import com.ardym.nitigrow.core.util.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class NitiGrowApplication : Application(), Configuration.Provider {

    @Inject lateinit var workConfiguration: Configuration
    @Inject lateinit var tokenStore: TokenDataStore

    override val workManagerConfiguration: Configuration
        get() = workConfiguration

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) enableStrictMode()
        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
        NotificationChannels.createAll(this)

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            val tag = tokenStore.languageTag.first()
            if (tag.isNotBlank()) LocaleManager.apply(tag)
        }
    }
}

private fun enableStrictMode() {
    android.os.StrictMode.setThreadPolicy(
        android.os.StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            .penaltyLog()
            .build()
    )
    android.os.StrictMode.setVmPolicy(
        android.os.StrictMode.VmPolicy.Builder()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .penaltyLog()
            .build()
    )
}

private class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) return
        val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
        crashlytics.log(if (tag != null) "$tag: $message" else message)
        if (priority == android.util.Log.ERROR && t != null) {
            crashlytics.recordException(t)
        }
    }
}
