package com.websbaba.nitigrow.core.telemetry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTelemetry @Inject constructor() : Telemetry {

    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    override fun event(name: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (k, v) ->
                when (v) {
                    is String -> putString(k, v)
                    is Int -> putInt(k, v)
                    is Long -> putLong(k, v)
                    is Double -> putDouble(k, v)
                    is Float -> putFloat(k, v)
                    is Boolean -> putBoolean(k, v)
                    else -> putString(k, v.toString())
                }
            }
        }
        analytics.logEvent(name, bundle)
        Timber.tag("Telemetry").v("event=$name params=$params")
    }

    override fun setUser(userId: String?, tenantId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId.orEmpty())
        if (tenantId != null) {
            analytics.setUserProperty("tenant_id", tenantId)
            crashlytics.setCustomKey("tenant_id", tenantId)
        }
    }

    override fun breadcrumb(message: String) {
        crashlytics.log(message)
        Timber.tag("Telemetry").v(message)
    }

    override fun nonFatal(t: Throwable, extras: Map<String, String>) {
        extras.forEach { (k, v) -> crashlytics.setCustomKey(k, v) }
        crashlytics.recordException(t)
        Timber.w(t, "non-fatal")
    }
}
