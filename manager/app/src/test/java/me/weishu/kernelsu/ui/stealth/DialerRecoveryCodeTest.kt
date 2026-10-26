package me.weishu.kernelsu.ui.stealth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialerRecoveryCodeTest {
    @Test fun acceptsModernDialerBroadcast() {
        assertTrue(DialerRecoveryCode.matches(DialerRecoveryCode.MODERN_ACTION, "android_secret_code://917891786688"))
    }
    @Test fun acceptsLegacyDialerBroadcast() {
        assertTrue(DialerRecoveryCode.matches(DialerRecoveryCode.LEGACY_ACTION, "android_secret_code://917891786688"))
    }
    @Test fun rejectsOtherActionsAndMissingData() {
        assertFalse(DialerRecoveryCode.matches("android.intent.action.VIEW", "android_secret_code://917891786688"))
        assertFalse(DialerRecoveryCode.matches(null, "android_secret_code://917891786688"))
        assertFalse(DialerRecoveryCode.matches(DialerRecoveryCode.MODERN_ACTION, null))
    }
    @Test fun rejectsDifferentOrExtendedCodes() {
        for (data in listOf("tel:917891786688", "android_secret_code://70707", "android_secret_code://91789178668", "android_secret_code://9178917866880", "android_secret_code://917891786688/path", "android_secret_code://917891786688?extra=1")) {
            assertFalse(DialerRecoveryCode.matches(DialerRecoveryCode.MODERN_ACTION, data))
        }
    }
}
