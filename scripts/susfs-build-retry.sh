#!/bin/sh
# Build susfs_guard_lkm.ko for the KMIs that failed, using the nested DDK clang tools.
set -u
SRC_ROOT="$HOME/ddk-local/src"
OUT="$HOME/src/dist/susfs"
DDK="$HOME/ddk-local"
MOD="$HOME/src/kernel/susfs"
mkdir -p "$OUT"

clang_bin() {
    # $1 = toolchain dir name, e.g. clang-r450784e
    d="$DDK/clang/$1/$1/bin"
    [ -x "$d/clang" ] && { echo "$d"; return; }
    find "$DDK/clang/$1" -maxdepth 4 -type f -name clang -path '*/bin/*' -printf '%h\n' 2>/dev/null | head -1
}

build_one() {
    kmi="$1"; tc="$2"
    src="$SRC_ROOT/$kmi"
    bin=$(clang_bin "$tc")
    echo "########## $kmi (clang: ${bin:-NOT FOUND}) ##########"
    [ -n "$bin" ] || { echo "RESULT $kmi no-clang"; return; }
    [ -d "$src" ] || { echo "RESULT $kmi no-source"; return; }

    export PATH="$bin:$PATH"
    export ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1

    echo "== toolchain check: $(clang --version | head -1)"
    echo "== ar: $(command -v llvm-ar) $(llvm-ar --version 2>/dev/null | head -1)"

    echo "== re-prepare"
    ( cd "$src" && make gki_defconfig >/dev/null 2>&1 && make prepare modules_prepare -j"$(nproc)" >/dev/null 2>&1 ) \
        || echo "== prepare reported errors"

    echo "== clean"
    rm -f "$MOD"/*.o "$MOD"/*.ko "$MOD"/.*.cmd "$MOD"/modules.order "$MOD"/Module.symvers 2>/dev/null
    rm -f "$MOD"/kallrecon/lib/*.o "$MOD"/kallrecon/lib/.*.cmd 2>/dev/null

    echo "== build"
    ( cd "$MOD" && make -C "$src" M="$PWD" ARCH=arm64 CC=clang LLVM=1 LLVM_IAS=1 modules -j"$(nproc)" ) \
        > "$HOME/susfs-retry3-$kmi.log" 2>&1
    if [ -f "$MOD/susfs_guard_lkm.ko" ]; then
        cp "$MOD/susfs_guard_lkm.ko" "$OUT/${kmi}_susfs_guard_lkm.ko"
        echo "RESULT $kmi OK $(stat -c %s "$OUT/${kmi}_susfs_guard_lkm.ko")"
        rm -f "$MOD/susfs_guard_lkm.ko"
    else
        echo "RESULT $kmi FAIL"
        grep -E "error:" "$HOME/susfs-retry3-$kmi.log" | head -4
    fi
}

build_one android14-5.15 clang-r487747c
build_one android13-5.15 clang-r450784e
build_one android13-5.10 clang-r450784e
build_one android12-5.10 clang-r416183b

echo "########## summary ##########"
ls -l "$OUT"
