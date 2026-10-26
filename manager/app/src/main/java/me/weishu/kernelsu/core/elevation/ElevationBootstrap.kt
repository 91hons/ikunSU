package me.weishu.kernelsu.core.elevation

/**
 * The path the prebuilt kernel modules execute the bootstrap helper from.
 *
 * The modules hard-code that path, and it occupies a fixed [SLOT_LENGTH]-character
 * field in their read-only data whose following byte belongs to neighbouring
 * data. A replacement therefore has to be exactly as long as the original, which
 * was `/data/user_de/0/df.root/bootstrap` - 33 characters.
 *
 * ikunSU's own data directory is usually shorter, so the surplus is filled with
 * redundant `/` separators. The kernel resolves them to the same file, and the
 * helper itself does not depend on the spelling it was started with.
 */
internal object ElevationBootstrap {

    /** Length of the bootstrap path field inside the prebuilt modules. */
    const val SLOT_LENGTH = 33

    private const val NAME = "boot"

    /**
     * File name to stage the helper as, inside [dataDirectory]. Kept short enough
     * that the joined path fits the slot, which matters for PR builds whose
     * application id is longer than the release one.
     */
    fun fileName(dataDirectory: String): String {
        val room = SLOT_LENGTH - dataDirectory.length - 1
        require(room >= 1) { "Manager data directory is too long for the payload: $dataDirectory" }
        return NAME.take(room)
    }

    /** Exactly [SLOT_LENGTH] characters, resolving to `<dataDirectory>/<fileName>`. */
    fun path(dataDirectory: String): String {
        val name = fileName(dataDirectory)
        val padding = SLOT_LENGTH - (dataDirectory.length + 1 + name.length)
        return dataDirectory + "/" + "/".repeat(padding) + name
    }
}
