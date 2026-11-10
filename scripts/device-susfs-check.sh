#!/system/bin/sh
# Diagnose the app-bundled ksud and the SUSFS command path.
K=$(ls -d /data/app/*/com.hons.ikunSU*/lib/arm64 2>/dev/null | head -1)/libksud.so
echo "bundled: $K"
"$K" --version
echo "--- susfs status ---"
"$K" susfs status
echo "--- susfs autoload status ---"
"$K" susfs autoload status
echo "--- susfs load ---"
"$K" susfs load
echo "--- sysfs ---"
ls -d /sys/module/susfs_guard_lkm 2>/dev/null || echo "not loaded"
