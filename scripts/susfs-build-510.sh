#!/bin/sh
# android12-5.10 final attempt: the DDK kdir tree is configured for the
# stack-protector "sysreg" ABI, which needs clang 19 (the system toolchain).
set -u
MOD="$HOME/src/kernel/susfs"
OUT="$HOME/src/dist/susfs"
KDIR="/opt/ddk/kdir/android12-5.10"
BIN=$(find "$HOME/ddk-local/clang/clang-r536225" -maxdepth 4 -type f -name clang -path "*/bin/*" -printf "%h\n" | head -1)
export PATH="$BIN:$PATH"
export ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1
echo "clang: $(clang --version | head -1)"

cd "$MOD"
rm -f ./*.o ./*.ko ./.*.cmd ./modules.order ./Module.symvers ./kallrecon/lib/*.o ./kallrecon/lib/.*.cmd 2>/dev/null
make -C "$KDIR" M="$PWD" ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1 modules -j"$(nproc)" > "$HOME/susfs-512-sys.log" 2>&1
if [ -f susfs_guard_lkm.ko ]; then
    cp susfs_guard_lkm.ko "$OUT/android12-5.10_susfs_guard_lkm.ko"
    echo "RESULT android12-5.10 OK $(stat -c %s "$OUT/android12-5.10_susfs_guard_lkm.ko")"
    rm -f susfs_guard_lkm.ko
else
    echo "RESULT android12-5.10 FAIL"
    grep -E "error:" "$HOME/susfs-512-sys.log" | sed 's/.*error: //' | sort -u | head -4
fi
ls -l "$OUT"
