#!/bin/sh
# Compare the CI-built SUSFS module with the kernelsu.ko that does load on device.
SUSFS=/mnt/c/Users/87318/Desktop/Downloads/ikunSU_run42910568054/aarch64-android12-5.10-susfs-lkm
KSU=/home/HONS/src/userspace/ksud/bin/aarch64

echo "===== susfs 6.12 ====="
readelf -p .modinfo "$SUSFS/android16-6.12_susfs_guard_lkm.ko" 2>/dev/null | grep -E 'vermagic|^ *\[ *[0-9]+\] +name=' | head -4
echo "-- sections --"
readelf -S "$SUSFS/android16-6.12_susfs_guard_lkm.ko" 2>/dev/null | grep -E 'versions|modinfo' | head -4

echo "===== kernelsu 6.12 ====="
readelf -p .modinfo "$KSU/android16-6.12_kernelsu.ko" 2>/dev/null | grep -E 'vermagic|^ *\[ *[0-9]+\] +name=' | head -4
echo "-- sections --"
readelf -S "$KSU/android16-6.12_kernelsu.ko" 2>/dev/null | grep -E 'versions|modinfo' | head -4
