# Protobuf
-shrinkunusedprotofields

# Commons-compress
-dontwarn com.github.luben.zstd.**

# Called by name from ksud through app_process, outside the Android app lifecycle.
-keep class me.weishu.kernelsu.webui.WebAppLabels { *; }
-keep class me.weishu.kernelsu.ui.stealth.DialerRecoveryGuard { *; }
-keep class me.weishu.kernelsu.ui.stealth.DialerRecoveryGuard$* { *; }
