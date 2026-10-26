package me.weishu.kernelsu.core.elevation

import android.content.Context
import java.io.File

/**
 * Picks the SU manager whose `libksud.so` the payload stages and hands to
 * `ksud late-load`.
 *
 * DirtyFragX deliberately skipped its own package here: it did not ship the
 * daemon, so it could only elevate some *other* installed manager. ikunSU does
 * ship `libksud.so` in `jniLibs/`, which is exactly the binary the kernel module
 * needs, so this time the Manager's own package is the one that gets detected.
 * That is what lets ikunSU elevate itself rather than only being an elevation
 * tool for another root manager.
 */
internal object ElevationManagers {

    /** The daemon that `bootstrap.c` stages as `ksud` and then executes. */
    const val KSUD_LIBRARY = "libksud.so"

    /**
     * The Manager itself, when it ships the daemon. This is the only candidate:
     * an ikunSU install always contains `libksud.so`, and rooting through some
     * other package would leave ikunSU without the daemon it needs.
     */
    fun resolve(context: Context): String? =
        context.packageName.takeIf { shipsKsud(context, it) }

    /**
     * A manager is only usable when `libksud.so` was extracted next to its other
     * native libraries, because that is where the payload copies it from.
     */
    fun shipsKsud(context: Context, packageName: String): Boolean {
        val dir = runCatching {
            context.packageManager.getApplicationInfo(packageName, 0).nativeLibraryDir
        }.getOrNull() ?: return false
        return File(dir, KSUD_LIBRARY).isFile
    }

    /** The same directory, for staging. */
    fun nativeLibraryDir(context: Context, packageName: String): File? =
        runCatching {
            context.packageManager.getApplicationInfo(packageName, 0).nativeLibraryDir
        }.getOrNull()?.let(::File)
}
