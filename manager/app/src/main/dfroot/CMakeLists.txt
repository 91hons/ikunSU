# DirtyFragX (CVE-2026-43284) privilege-escalation payload, integrated into the
# ikunSU Manager. See README.md in this directory for provenance and licensing.
#
# Only arm64-v8a is built: the LKMs and the libc++ shellcode are AArch64-only,
# and the Manager already declares that ABI as its primary one. Every other ABI
# simply ships without the payload, and the Manager UI reports the device as
# unsupported instead of trying to run it.

if(NOT DFROOT_PACKAGE)
    message(FATAL_ERROR "DFROOT_PACKAGE must be set to the Manager applicationId")
endif()

if(NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(STATUS "ikun提权 payload skipped for ${ANDROID_ABI}")
    return()
endif()

enable_language(ASM)

set(DFROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}")

# `as` resolves .incbin paths against its include path, so the module blobs are
# addressed as ko/<name>.ko relative to this directory and the generated
# splicehelper blob by its name in the build directory (the source tree stays
# clean).
set(DFROOT_ASM_INCLUDES "-I${DFROOT_DIR} -I${CMAKE_CURRENT_BINARY_DIR}")
set(CMAKE_ASM_FLAGS "${CMAKE_ASM_FLAGS} ${DFROOT_ASM_INCLUDES}")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${DFROOT_ASM_INCLUDES} -Wall -Wextra -fno-stack-protector -fomit-frame-pointer")

set(DFROOT_KO_FILES
    "${DFROOT_DIR}/ko/dfroot-android12-5.10.ko"
    "${DFROOT_DIR}/ko/dfroot-android13-5.10.ko"
    "${DFROOT_DIR}/ko/dfroot-android13-5.15.ko"
    "${DFROOT_DIR}/ko/dfroot-android14-5.15.ko"
    "${DFROOT_DIR}/ko/dfroot-android14-6.1.ko"
    "${DFROOT_DIR}/ko/dfroot-android15-6.6.ko"
    "${DFROOT_DIR}/ko/dfroot-android16-6.12.ko"
    "${DFROOT_DIR}/ko/dfroot-android17-6.18.ko"
)
foreach(KO IN LISTS DFROOT_KO_FILES)
    if(NOT EXISTS "${KO}")
        message(FATAL_ERROR "Missing prebuilt kernel module ${KO}")
    endif()
endforeach()

set(SPLICEHELPER "${CMAKE_CURRENT_BINARY_DIR}/splicehelper")
add_custom_command(
    OUTPUT "${SPLICEHELPER}"
    COMMAND "${CMAKE_C_COMPILER}"
            --target=${CMAKE_C_COMPILER_TARGET}
            --sysroot=${CMAKE_SYSROOT}
            "${DFROOT_DIR}/splicehelper.c"
            -o "${SPLICEHELPER}"
            -nodefaultlibs -nostartfiles -ffreestanding -static
    COMMAND "${CMAKE_STRIP}" "${SPLICEHELPER}"
    DEPENDS "${DFROOT_DIR}/splicehelper.c"
    COMMENT "Building splicehelper"
)
add_custom_target(splicehelper_bin DEPENDS "${SPLICEHELPER}")

# AGP only packages lib*.so, so these executables are named libdfroot.so and
# libbootstrap.so and land in the APK's native lib dir. The app execs them from
# nativeLibraryDir (or a copy in app data), never from the APK itself.
add_executable(dfroot exp.c libcxx.S elf_parser.c)
add_dependencies(dfroot splicehelper_bin)
set_target_properties(dfroot PROPERTIES
    PREFIX "lib"
    SUFFIX ".so"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)
set_source_files_properties(exp.c PROPERTIES
    OBJECT_DEPENDS "${DFROOT_KO_FILES};${SPLICEHELPER}"
)
# The Manager compiles its native code as strict ISO C; exp.c embeds the module
# blobs through file-scope asm declarations, which is a GNU extension. LTO is
# disabled because the release configuration enables it globally and the linker
# would then assemble the .incbin directives from a different working directory.
target_compile_options(dfroot PRIVATE -std=gnu2x -fno-lto)
target_link_libraries(dfroot log)
target_link_options(dfroot PRIVATE -fno-lto -Wl,-z,max-page-size=16384)

# The kernel module execs this helper as root; it reads the escalation
# preferences from the Manager's own data directory.
add_executable(bootstrap bootstrap.c)
set_target_properties(bootstrap PROPERTIES
    PREFIX "lib"
    SUFFIX ".so"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)
target_compile_definitions(bootstrap PRIVATE DFROOT_PACKAGE="${DFROOT_PACKAGE}")
target_link_options(bootstrap PRIVATE -Wl,-z,max-page-size=16384)
