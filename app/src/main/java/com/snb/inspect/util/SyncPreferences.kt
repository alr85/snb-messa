package com.snb.inspect.util

import android.content.Context

class SyncPreferences(context: Context) {

    private val prefs =
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOMER_SYNC = "customer_last_sync"
        private const val KEY_NOTICE_SYNC = "notice_last_sync"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
    }

    fun getCustomerLastSync(): Long =
        prefs.getLong(KEY_CUSTOMER_SYNC, 0)

    fun setCustomerLastSync(time: Long) {
        prefs.edit().putLong(KEY_CUSTOMER_SYNC, time).apply()
    }

    fun getNoticeLastSync(): Long =
        prefs.getLong(KEY_NOTICE_SYNC, 0)

    fun setNoticeLastSync(time: Long) {
        prefs.edit().putLong(KEY_NOTICE_SYNC, time).apply()
    }

    fun isAutoSyncEnabled(): Boolean =
        prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
    }
}
