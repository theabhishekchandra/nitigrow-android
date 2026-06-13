package com.websbaba.nitigrow.core.payments

import android.app.Activity
import com.websbaba.nitigrow.domain.model.CheckoutOrder
import com.razorpay.Checkout
import org.json.JSONObject
import timber.log.Timber

/**
 * Builds the JSON options Razorpay expects + opens checkout sheet.
 * Activity must implement `PaymentResultWithDataListener` (or `PaymentResultListener`).
 */
object RazorpayLauncher {

    fun open(activity: Activity, order: CheckoutOrder) {
        Checkout.preload(activity.applicationContext)
        val co = Checkout()
        co.setKeyID(order.keyId)
        try {
            val opts = JSONObject().apply {
                put("name", order.name)
                put("description", order.description)
                put("order_id", order.razorpayOrderId)
                put("currency", order.currency)
                put("amount", order.amountPaise)
                put("send_sms_hash", true)
                put("allow_rotation", false)
                put("retry", JSONObject().apply { put("enabled", false) })
                put("prefill", JSONObject().apply {
                    order.prefillEmail?.let { put("email", it) }
                    order.prefillContact?.let { put("contact", it) }
                })
                put("theme", JSONObject().apply { put("color", "#075E54") })
            }
            co.open(activity, opts)
        } catch (t: Throwable) {
            Timber.e(t, "Razorpay launch failed")
        }
    }
}
