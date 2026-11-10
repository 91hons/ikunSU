#!/bin/sh
# Prepare every DDK kernel tree and build the SUSFS guard LKM for each KMI.
set -u
export ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1
SRC_ROOT="$HOME/ddk-local/src"
OUT="$HOME/src/dist/susfs"
mkdir -p "$OUT" "$SRC_ROOT"

KMIS="android17-6.18 android15-6.6 android14-6.1 android14-5.15 android13-5.15 android13-5.10 android12-5.10"

for kmi in $KMIS; do
    echo "########## $kmi ##########"
    src="$SRC_ROOT/$kmi"
    if [ ! -d "$src" ]; then
        echo "== extracting src.$kmi.tar.zst"
        tar --zstd -xf "/opt/ddk/src/src.$kmi.tar.zst" -C "$SRC_ROOT" || { echo "RESULT $kmi extract-fail"; continue; }
    fi
    [ -d "$src" ] || { echo "RESULT $kmi no-source-dir"; continue; }

    if [ ! -f "$src/include/generated/autoconf.h" ]; then
        echo "== configure $kmi"
        ( cd "$src" && make gki_defconfig >/dev/null 2>&1 && make prepare modules_prepare -j"$(nproc)" >/dev/null 2>&1 ) \
            || echo "== prepare reported errors for $kmi"
    else
        echo "== already configured"
    fi

    [ -f "$src/include/generated/autoconf.h" ] || { echo "RESULT $kmi no-autoconf"; continue; }

    echo "== build module"
    DDK="$HOME/ddk-local" sh "$HOME/src/scripts/susfs-build.sh" "$kmi" "$src" > "$HOME/susfs-$kmi.log" 2>&1
    ko="$HOME/src/kernel/susfs/susfs_guard_lkm.ko"
    if [ -f "$ko" ]; then
        cp "$ko" "$OUT/${kmi}_susfs_guard_lkm.ko"
        size=$(stat -c %s "$OUT/${kmi}_susfs_guard_lkm.ko")
        echo "RESULT $kmi OK $size"
        rm -f "$ko"
        ( cd "$HOME/src/kernel/susfs" && make -C "$src" M="$PWD" clean >/dev/null 2>&1 )
    else
        echo "RESULT $kmi FAIL"
        tail -5 "$HOME/susfs-$kmi.log"
    fi
done
echo "########## summary ##########"
ls -l "$OUT"
