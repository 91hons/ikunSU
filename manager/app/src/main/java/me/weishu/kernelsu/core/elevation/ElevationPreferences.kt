package me.weishu.kernelsu.core.elevation

import android.content.Context

/**
 * Preferences shared with the root-side helpers of the ikun提权 payload.
 *
 * `bootstrap.c` runs as root before any app process exists, so it parses this
 * SharedPreferences XML directly instead of going through Android. The file name
 * and the key names below are therefore a contract with the native side, and the
 * file has to stay in credential-protected storage where a normal
 * `getSharedPreferences` call puts it.
 */
internal class ElevationPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    /** Package name handed to `ksud late-load --package-name`. */
    var suManager: String?
        get() = prefs.getString(KEY_SU_MANAGER, null)
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_SU_MANAGER) else putString(KEY_SU_MANAGER, value)
            }.commit()
        }

    /** Mark every installed module disabled before ksud runs. */
    var disableModules: Boolean
        get() = prefs.getBoolean(KEY_DISABLE_MODULES, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DISABLE_MODULES, value).commit()
        }

    /** Reboot once `ksud late-load` succeeded, so the new root is actually live. */
    var softReboot: Boolean
        get() = prefs.getBoolean(KEY_SOFT_REBOOT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SOFT_REBOOT, value).commit()
        }

    companion object {
        /** Must match `PREFS_PATH` in `dfroot/bootstrap.c`. */
        const val FILE_NAME = "ikun_elevation"

        /** Must match the `name="..."` lookups in `dfroot/bootstrap.c`. */
        const val KEY_SU_MANAGER = "su_manager"
        const val KEY_DISABLE_MODULES = "disable_modules"
        const val KEY_SOFT_REBOOT = "soft_reboot"
    }
}
