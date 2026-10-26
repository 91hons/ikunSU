package me.weishu.kernelsu.core.elevation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ElevationBootstrapTest {

    private val releaseDir = "/data/user/0/com.hons.ikunSU"
    private val prDir = "/data/user/0/com.hons.ikunSU.pr"

    @Test fun releasePathIsTheCanonicalOne() {
        assertEquals("boot", ElevationBootstrap.fileName(releaseDir))
        assertEquals("$releaseDir/boot", ElevationBootstrap.path(releaseDir))
    }

    @Test fun everyPathFillsTheModuleSlot() {
        for (dir in listOf(releaseDir, prDir, "/data/user/0/a", "/data/user/0/abcdefghij")) {
            assertEquals(ElevationBootstrap.SLOT_LENGTH, ElevationBootstrap.path(dir).length)
        }
    }

    @Test fun longerApplicationIdShrinksTheFileName() {
        assertEquals("b", ElevationBootstrap.fileName(prDir))
        assertEquals("$prDir/b", ElevationBootstrap.path(prDir))
    }

    @Test fun paddingKeepsTheSameFile() {
        val dir = "/data/user/0/short"
        val path = ElevationBootstrap.path(dir)
        assertEquals(ElevationBootstrap.SLOT_LENGTH, path.length)
        // Redundant separators collapse, so the padded form names the staged file.
        assertEquals("$dir/boot", path.replace(Regex("/+"), "/"))
        assertTrue(path.startsWith("$dir/"))
    }

    @Test fun rejectsADirectoryThatCannotFit() {
        val tooLong = "/data/user/0/" + "x".repeat(40)
        assertThrows(IllegalArgumentException::class.java) { ElevationBootstrap.path(tooLong) }
    }
}
