# DirtyFragX integration

This directory carries the DirtyFragX privilege-escalation payload
(CVE-2026-43284, the AES-CBC `splice()` page-cache write primitive) that ikunSU
exposes as **ikun提权** from the install page, which stays reachable before Root
exists. It is built into the Manager's native libraries and runs entirely on the
device; nothing here talks to the network.

## Provenance

- Upstream: <https://github.com/mitschud/DirtyFrag> (via
  <https://github.com/dugongzi/DirtyFragX>), itself derived from
  <https://github.com/diabl0w/DFRoot>.
- The eight modules in `ko/` are the prebuilt `dfroot.ko` images published by
  DirtyFragX, built from `lkm/dfroot.c` for each KMI.
- Both projects are GPL-3.0, matching the license of this repository.

## Changes against upstream

`bootstrap.c` used to run from `/data/user_de/0/df.root/`. The Manager now stages
`ksud`, `bootstrap` and its SharedPreferences in its own data directory and CMake
defines `DFROOT_PACKAGE` from the Gradle `applicationId`, so the same source
works for a PR build (`com.hons.ikunSU.pr`) as well.

The kernel module cannot be rebuilt here, so its hard-coded bootstrap path still
is DirtyFragX's. `exp.c` therefore rewrites that path inside the in-memory copy
of the module before it is written to the page cache: the slot is a fixed
33-character field, and the Manager passes a path of exactly that length (it pads
with redundant `/` separators, which the kernel resolves to the same file). Only
the 8 MiB of module image in the page cache is affected; the on-disk `.ko`
snapshot in `ko/` stays byte-identical to upstream.

## Constraints

- `arm64-v8a` only. The shellcode and the modules are AArch64, so CMake returns
  early for the other ABIs and the Manager reports the device as unsupported.
- Supported kernels: 5.10, 5.15, 6.1, 6.6, 6.12 and 6.18, on Android 12 or
  newer. A matching entry here means the module compiles for that KMI, not that
  the kernel is unpatched.
- Root is obtained by `ksud late-load`, so the Manager must ship `libksud.so`,
  which `jniLibs/` already does. That library is both the SU daemon and the
  payload the module stages, which is why the Manager can elevate itself.
