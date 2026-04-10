package com.campusconnectplus.core.util

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * Debug-session logger: appends NDJSON to app filesDir for later adb pull.
 * Log path on device: context.filesDir + "/debug-15ec8c.log"
 * Pull to workspace: adb pull /data/data/com.campusconnectplus/files/debug-15ec8c.log .
 */
object DebugLog {
    private const val LOG_FILE = "debug-15ec8c.log"
    private const val SESSION_ID = "15ec8c"

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    fun log(
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap(),
        hypothesisId: String? = null,
        runId: String? = null
    ) {
        // #region agent log
        try {
            val ctx = appContext ?: return
            val payload = JSONObject().apply {
                put("sessionId", SESSION_ID)
                put("timestamp", System.currentTimeMillis())
                put("location", location)
                put("message", message)
                data.forEach { (k, v) -> put(k, v?.toString() ?: "null") }
                hypothesisId?.let { put("hypothesisId", it) }
                runId?.let { put("runId", it) }
            }
            val file = File(ctx.filesDir, LOG_FILE)
            synchronized(this) {
                file.appendText(payload.toString() + "\n")
            }
        } catch (_: Throwable) { }
        // #endregion
    }
}
