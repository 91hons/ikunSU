#!/bin/sh
# Prepare a DDK kernel source tree so out-of-tree modules can be built against it.
set -eu
SRC="${1:-$HOME/ddk-local/src/android16-6.12}"
export ARCH=arm64
export LLVM=1
export LLVM_IAS=1
export CC=clang

cd "$SRC"
echo "=== configs available ==="
ls arch/arm64/configs/ 2>/dev/null | head -10

if [ -f arch/arm64/configs/gki_defconfig ]; then
    DEFCONFIG=gki_defconfig
elif [ -f arch/arm64/configs/defconfig ]; then
    DEFCONFIG=defconfig
else
    echo "error: no defconfig found" >&2
    exit 2
fi
echo "=== using $DEFCONFIG ==="

make "$DEFCONFIG" 2>&1 | tail -n 5
make prepare modules_prepare -j"$(nproc)" 2>&1 | tail -n 20

echo "=== check ==="
ls -l include/generated/autoconf.h include/config/auto.conf 2>/dev/null || echo "autoconf missing"
