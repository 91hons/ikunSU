package me.weishu.kernelsu.core.elevation

import android.content.Context
import android.os.Build
import android.os.Process
import me.weishu.kernelsu.Natives
import java.io.File

/** Why the ikun鎻愭潈 payload cannot run on this device. */
internal enum class ElevationBlocker {
    None,
    SecondaryUser,
    Abi,
    Kernel,
    Assets,
    Manager,
}

/** A snapshot of everything the elevation screen reports and gates on. */
internal data class ElevationProbe(
    val supported: Boolean,
    val rooted: Boolean,
    val kernelVersion: String,
    val securityPatch: String,
    val managerPackage: String,
    val blocker: ElevationBlocker,
)

/**
 * Device-side checks for the ikun鎻愭潈 payload.
 *
 * The payload is a prebuilt kernel module plus AArch64 shellcode, so the checks
 * here mirror what `exp.c` can actually handle: an arm64 process, one of the KMI
 * variants a module was built for, and a staged SU daemon to hand to
 * `ksud late-load`.
 */
internal object ElevationDevice {

    const val PAYLOAD_LIBRARY = "libdfroot.so"
    const val BOOTSTRAP_LIBRARY = "libbootstrap.so"

    /** Kernel versions with a prebuilt module, mirroring `select_ko_image()` in exp.c. */
    private val supportedKernels = listOf(5 to 10, 5 to 15, 6 to 1, 6 to 6, 6 to 12, 6 to 18)

    private const val PER_USER_RANGE = 100000

    /** `/dev/df` is the shellcode's one-shot mutex; `/dev/dfm6` means ksud started. */
    private val hookMarker = File("/dev/df")
    private val rootMarker = File("/dev/dfm6")

    fun probe(context: Context): ElevationProbe {
        val kernel = System.getProperty("os.version").orEmpty().ifBlank { "unknown" }
        val manager = ElevationManagers.resolve(context).orEmpty()
        val blocker = when {
            !isPrimaryUser() -> ElevationBlocker.SecondaryUser
            !isArm64() -> ElevationBlocker.Abi
            !hasPayload(context) -> ElevationBlocker.Assets
            !isSupportedKernel(kernel) -> ElevationBlocker.Kernel
            manager.isEmpty() -> ElevationBlocker.Manager
            else -> ElevationBlocker.None
        }
        return ElevationProbe(
            supported = blocker == ElevationBlocker.None,
            rooted = isRooted(),
            kernelVersion = kernel,
            securityPatch = Build.VERSION.SECURITY_PATCH.orEmpty(),
            managerPackage = manager,
            blocker = blocker,
        )
    }

    /**
     * The payload needs both of its executables on disk. They are packaged as
     * `lib*.so` so that AGP ships them, and are executed from the extracted
     * native library directory, never loaded as libraries.
     */
    fun hasPayload(context: Context): Boolean {
        val dir = context.applicationInfo.nativeLibraryDir?.takeIf { it.isNotEmpty() } ?: return false
        return File(dir, PAYLOAD_LIBRARY).isFile && File(dir, BOOTSTRAP_LIBRARY).isFile
    }

    /**
     * Already rooted: either the payload's own markers are still in `/dev` (they
     * are recreated on every reboot, so they only describe the current boot), or
     * this Manager is already served by a loaded kernel driver.
     */
    fun isRooted(): Boolean = (hookMarker.exists() && rootMarker.exists()) || Natives.isManager

    private fun isArm64(): Boolean = Build.SUPPORTED_ABIS.firstOrNull() == "arm64-v8a"

    /**
     * Applied Android UIDs are `userId * 100000 + appId`, and the bootstrap helper
     * reaches its files through `/data/data`, which is user 0. Running the payload
     * from a work profile or a secondary user would therefore plant a module that
     * cannot find its helper, so it is refused up front instead.
     */
    private fun isPrimaryUser(): Boolean = Process.myUid() / PER_USER_RANGE == 0

    private fun isSupportedKernel(kernel: String): Boolean {
        val major = kernel.substringBefore('.').toIntOrNull() ?: return false
        val minor = kernel.substringAfter('.', "").substringBefore('.').toIntOrNull() ?: return false
        return (major to minor) in supportedKernels
    }
}
