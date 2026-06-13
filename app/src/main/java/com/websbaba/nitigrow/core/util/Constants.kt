package com.websbaba.nitigrow.core.util

object Constants {
    const val DATABASE_NAME = "nitigrow.db"
    const val DATASTORE_NAME = "nitigrow_prefs"
    const val NETWORK_TIMEOUT_SECONDS = 30L

    // media limits (bytes) — enforce client-side per CLAUDE.md
    const val MAX_IMAGE_BYTES = 5L * 1024 * 1024
    const val MAX_VIDEO_BYTES = 16L * 1024 * 1024
    const val MAX_DOC_BYTES = 100L * 1024 * 1024
}
