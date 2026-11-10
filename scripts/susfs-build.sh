#!/bin/sh
# Build the SUSFS guard LKM against the DDK kdir-min tree (same one used for kernelsu.ko).
set -eu
KMI="${1:-android16-6.12}"
DDK="${DDK:-/opt/ddk}"
KDIR="${2:-$DDK/kdir-min/$KMI}"
MOD_DIR="$(cd "$(dirname "$0")/../kernel/susfs" && pwd)"

CLANG_DIR=""
for c in "$DDK"/clang/*; do
    [ -x "$c/bin/clang" ] && CLANG_DIR="$c" && break
done

echo "KMI     : $KMI"
echo "KDIR    : $KDIR"
echo "CLANG   : ${CLANG_DIR:-system}"
echo "MOD_DIR : $MOD_DIR"

[ -d "$KDIR" ] || { echo "error: missing $KDIR" >&2; exit 2; }
[ -n "$CLANG_DIR" ] && export PATH="$CLANG_DIR/bin:$PATH"
export ARCH=arm64 CROSS_COMPILE=aarch64-linux-gnu- LLVM=1 LLVM_IAS=1

cd "$MOD_DIR"
make -C "$KDIR" M="$PWD" ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1 modules -j"$(nproc)" 2>&1 | tail -n 45
ls -l susfs_guard_lkm.ko 2>/dev/null || echo "NO KO PRODUCED"
