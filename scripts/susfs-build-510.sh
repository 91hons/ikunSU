#!/bin/sh
# android12-5.10: its LSE atomic asm does not assemble with the older DDK clangs.
# Build this one with clang 17 and disable ARM64_LSE_ATOMICS for the probe, which
# selects the LL/SC implementation instead of arch/arm64/include/asm/atomic_lse.h.
set -u
SRC="$HOME/ddk-local/src/android12-5.10"
MOD="$HOME/src/kernel/susfs"
OUT="$HOME/src/dist/susfs"
BIN=$(find "$HOME/ddk-local/clang/clang-r487747c" -maxdepth 4 -type f -name clang -path '*/bin/*' -printf '%h\n' | head -1)
export PATH="$BIN:$PATH"
export ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1
echo "clang: $(clang --version | head -1)"

cd "$SRC"
make mrproper >/dev/null 2>&1
make gki_defconfig >/dev/null 2>&1
# force the LL/SC path
sed -i 's/^CONFIG_ARM64_LSE_ATOMICS=y/# CONFIG_ARM64_LSE_ATOMICS is not set/' .config
grep -q '^# CONFIG_ARM64_LSE_ATOMICS is not set' .config || echo '# CONFIG_ARM64_LSE_ATOMICS is not set' >> .config
make olddefconfig >/dev/null 2>&1
grep -E 'CONFIG_ARM64_LSE_ATOMICS' .config || echo "lse flag absent"
make prepare modules_prepare -j"$(nproc)" >/dev/null 2>&1 || echo "prepare reported errors"

cd "$MOD"
rm -f ./*.o ./*.ko ./.*.cmd ./modules.order ./Module.symvers ./kallrecon/lib/*.o ./kallrecon/lib/.*.cmd 2>/dev/null
make -C "$SRC" M="$PWD" ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1 modules -j"$(nproc)" > "$HOME/susfs-512-lse.log" 2>&1
if [ -f susfs_guard_lkm.ko ]; then
    cp susfs_guard_lkm.ko "$OUT/android12-5.10_susfs_guard_lkm.ko"
    echo "RESULT android12-5.10 OK $(stat -c %s "$OUT/android12-5.10_susfs_guard_lkm.ko")"
    rm -f susfs_guard_lkm.ko
else
    echo "RESULT android12-5.10 FAIL"
    grep -E "error:" "$HOME/susfs-512-lse.log" | head -4
fi
ls -l "$OUT"
