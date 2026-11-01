package me.weishu.kernelsu.ui.cosmetic

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Portable theme packages.
 *
 * A package is a plain zip holding `theme.json` (the appearance knobs) and an
 * optional `background.<ext>` with the artwork, so a theme can be shared
 * between devices without dragging the whole app data directory along.
 */
object ThemePackage {
    private const val MANIFEST = "theme.json"
    private const val VERSION = 1
    private const val MAX_ENTRY_BYTES = 16L * 1024 * 1024

    fun export(context: Context, target: Uri): Boolean = runCatching {
        val app = context.applicationContext
        val state = BackgroundStore.state.value
        val manifest = JSONObject().apply {
            put("version", VERSION)
            put("alpha", state.alpha.toDouble())
            put("dim", state.dim.toDouble())
            put("blur", state.blur.toDouble())
        }
        val background = readBackground(app, state.uri)
        app.contentResolver.openOutputStream(target)?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(MANIFEST))
                zip.write(manifest.toString().toByteArray())
                zip.closeEntry()
                if (background != null) {
                    zip.putNextEntry(ZipEntry("background.png"))
                    zip.write(background)
                    zip.closeEntry()
                }
            }
        }
        true
    }.getOrDefault(false)

    fun import(context: Context, source: Uri): Boolean = runCatching {
        val app = context.applicationContext
        var manifest: JSONObject? = null
        var background: ByteArray? = null
        app.contentResolver.openInputStream(source)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                var total = 0L
                while (entry != null) {
                    if (!entry.isDirectory) {
                        total += entry.size.coerceAtLeast(0)
                        if (total > MAX_ENTRY_BYTES) return false
                        val name = entry.name.substringAfterLast('/')
                        when {
                            name == MANIFEST -> manifest = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                            name.startsWith("background.") -> background = zip.readBytes()
                        }
                    }
                    entry = zip.nextEntry
                }
            }
        }
        val json = manifest ?: return false
        if (json.optInt("version", 0) > VERSION) return false
        background?.let { bytes ->
            val extension = "png"
            if (!BackgroundStore.setBackgroundBytes(app, bytes, extension)) return false
        }
        val current = BackgroundStore.state.value
        BackgroundStore.setAlpha(app, json.optDouble("alpha", current.alpha.toDouble()).toFloat())
        BackgroundStore.setDim(app, json.optDouble("dim", current.dim.toDouble()).toFloat())
        BackgroundStore.setBlur(app, json.optDouble("blur", current.blur.toDouble()).toFloat())
        true
    }.getOrDefault(false)

    private fun readBackground(context: Context, uri: String): ByteArray? = runCatching {
        if (uri.isEmpty()) return null
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
    }.getOrNull()
}
