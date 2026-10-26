# ikunSU migration plan

## Local android16-6.12 cold-boot recovery test

- [x] Verify the user's eight ko files match published permanent-signature assets.
- [x] Verify the connected iPA2691 uses android16-6.12 and the exact 32681 APK.
- [x] Read the actual loaded kernel: 40750 / UAPI 0 differs from our 32681 / UAPI 5.
- [x] Keep potential early-manager scan changes out of this matched-image test.
- [x] Compile an AArch64 android16-6.12 ko locally from a fresh source snapshot.
- [x] Patch the supplied stock init_boot through matched ksud/ksuinit, preserving input.
- [x] Verify image header, ramdisk contents, module identity and artifact checksums.
- [x] User flashes the local test image and confirms Root after cold boot; the screenshot shows working status and 32681-5.

The successful screenshot identifies vivo iPA2691 / Android 17 / Linux 6.12.30,
with KMI android16-6.12 and matched 32681 / UAPI 5. The earlier failure was the
actually loaded 40750 / UAPI 0 component, not a mismatch in this fork's Manager
package or permanent certificate. No kernel/ksud interface change was needed.
The user has now explicitly requested all 16 locally built modules and GitHub
synchronization after this successful test. The user performed the image flash.

Fresh local AArch64 ko SHA-256 is 66e1f5d5740f766236328c1d91d2f8b44607d25fcbcfb8624d98f01cbf9199b2.
The supplied stock init_boot SHA-256 remains 90bd828ac4cccbc9b9700d68d8196c984e0b2a9210f666330161edf3c148d362.
The 8 MiB test image SHA-256 is 939331a758e61f99369beb2379d28fbf37b35b54b64c1d67e0c6e9624ed80f15.
Static verification passed Android v4, legacy LZ4, exact ko and static ksuinit,
original init.real and all other effective stock entries. No kernel source or
Manager signing changes were included in this test. The user subsequently
confirmed the matched 32681 / UAPI 5 installation was working.

Use matched ksud's ordinary boot-patch with the matching ko and static ksuinit.
It installs the ramdisk init entry, preserves the original init as init.real
when present, and uses ksuinit's symbol/vermagic-aware module-loading flow.
Replacing only the ko does not establish an equivalent startup path. External
patcher compatibility has not been verified; boot-patch-v2 requires a kernel
in boot and is not applicable to this kernel-free init_boot image.

## Local complete 16-module follow-up

- [x] Record the user's successful AArch64 android16-6.12 Root test without changing kernel/userspace contracts.
- [x] Compile and validate all eight KMI variants locally for AArch64 and x86_64.
- [x] Verify every module's ELF architecture, KMI, permanent Manager identity and SHA-256.
- [x] Publish and verify the matched local artifacts on GitHub.
- [ ] Push the approved documentation and run module Actions after account access is restored.
- [x] Replace the download placeholder below and in README_CN.md after publication is verified.

The local matrix is android12-5.10, android13-5.10, android13-5.15,
android14-5.15, android14-6.1, android15-6.6, android16-6.12 and android17-6.18,
each for AArch64 and x86_64. Only AArch64 android16-6.12 has the user-confirmed
Root result from this follow-up. Other combinations have compilation/artifact
checks only, with no claim of on-device Root compatibility.

Local 16-module [bundle](https://github.com/Hons123/ikunSU/releases/download/v3.3.1-ikunSU.1/ikunSU-local-LKM-v3.3.1-ikunSU.1_32681-20261014.zip), public manifest and checksums are published on v3.3.1-ikunSU.1.
The ZIP SHA-256 is 9dbe9f6a600b06a537737c292bbc6e6e37b86d6f0c8ca92e5f07986cb9381590.
All 16 modules passed two independent artifact checks; the golden module hash
is unchanged. GitHub digests match all three uploaded supplemental files, and
the 26 original canonical asset hashes are unchanged. The local snapshot uses
commit 629e09f; kernel/UAPI/version contents remain unchanged in the docs update.
ARM64 check_symbol passed against each DDK vmlinux. x86_64 uses the existing CI
modules_prepare/MODPOST mode, without an x86 vmlinux or on-device ABI check.

The three supplemental assets uploaded successfully and the downloaded public
ZIP matched the local ZIP byte-for-byte. Immediately afterwards, Git push and
authenticated REST endpoints returned HTTP 403 with account-suspended messages.
No source push, release-note update or new Actions dispatch completed after
that response. Local documentation commits and a clean source ZIP are preserved;
the account owner must restore GitHub access before those steps can continue.

## 3.3.1-ikunSU.1 runtime fixes

- [x] Reproduce Xiaomi Android 17 dialer recovery: both matching manifest broadcasts are skipped with `Greezer Denial`.
- [x] Trace web daemon lifetime: `setsid` alone leaves the process in the Manager application cgroup.
- [x] Make the native feature page inherit the selected home theme and components.
- [x] Start web services from the protected installed daemon and detach Android cgroups before threads are created.
- [x] Protect the native dialer receiver from Xiaomi background freezing with a signed-APK Root settings guard; require readiness before hiding, and restore after boot failures.
- [x] Run required Android Rust and Manager checks; verify hiding, dialing and web shutdown/restart on the affected phone.
- [x] Upload source and publish a separate hotfix release with complete matched build artifacts.

The hotfix retains upstream 3.3.1 and UAPI 5. Version code 32681 identifies the
fork revision without replacing the already published v3.3.1 release.

The Xiaomi device delivered the exact dual SECRET_CODE broadcast but Greezer
skipped the manifest receiver. Vendor self-start permission alone did not fix
this. Adding only ikunSU to MILLET_NO_RESTRICT_APP and legitimately waking the
already frozen Manager prevented freezing; real dialer recovery also passed
after the Manager process was killed (cold start). Standalone Android 17
app_process cannot register a dynamic broadcast receiver without an AMS
ProcessRecord, so that rejected prototype is excluded from the final source.

- Final Android Rust check, Clippy and formatting passed in sequence. Fresh
  release ksud was staged before Manager clean assembleRelease/lintRelease/
  testDebugUnitTest: 78 tests, no failures, 0 Lint errors (421 advisory warnings).
- APK v2 signer and freshly packaged daemon bytes match the permanent identity.
  The R8 release helper passed real startup, PID identity and pidfd stop checks.
- Xiaomi 14 Ultra / Android 17 / HyperOS 4.0.0.18 retained a responsive root web
  PID outside app cgroups after native hiding and Manager closure, with 436 apps.
- Freezing only the Manager cgroup, then using web hiding, safely terminated only
  that verified process while retaining the web PID. Real dialer entry restored
  the launcher and cold-started a new Manager; web access stayed available.
- The local test APK reused the previously verified eight 3.3.1 modules. Public
  hotfix artifacts must be rebuilt by Actions with code 32681 before publication.
- Miuix native page was rendered on the device with matching home cards, colors,
  typography and controls. Material inherits the active theme and passed build.
- Source commit ea8295d passed Build run 38442070823 and Release run 38442075631.
  The separate stable v3.3.1-ikunSU.1 release contains 26 public assets and the
  permanent APK certificate; the older v3.3.1 tag and release remain intact.
- All 26 downloaded asset sizes and GitHub SHA-256 digests match. The 24 payload
  records match the manifest and checksum list. Each of the three packaged ksud
  binaries equals its public counterpart after stripping; 19 decompressed
  embedded ko/init payloads equal the public files byte-for-byte. All 16 modules
  have the expected architecture, KMI, module name and permanent Manager identity.
- The user confirmed manually installing the cloud 32681 APK after device tests.
  USB was then disconnected, so the final cloud installation was not rechecked
  over ADB. No kernel flashing or reboot was performed by the agent.

- [x] Inspect the existing APK and reference sources for identity, UI effects, web APIs, and stealth behavior.
- [x] Apply the APK identity and icon; keep JNI namespaces and kernel/userspace interfaces compatible.
- [x] Port configurable cosmetic effects, web/API entry points, and recoverable launcher hiding to the current Manager.
- [x] Configure GitHub Actions to build matched Manager, ksud, ksuinit, and LKM artifacts with one signing identity.
- [x] Run build/static checks, document reproducible setup, and report any remaining external requirements.

## v3.3.1 and complete flash assets follow-up

- [x] Compare core sources with the official v3.3.1 tag; kernel, UAPI and ksuinit already match.
- [x] Use version.properties for Manager, ksud and kernel version 3.3.1 / 32680.
- [x] Build all eight KMI variants for AArch64 and x86_64 with the permanent Manager identity.
- [x] Require valid embedded ko and ksuinit before any release ksud build.
- [x] Validate all release artifacts before publishing an existing v3.3.1 tag.
- [x] Build and verify a complete signed ARM64 Manager APK; replace the incomplete development package.
- [x] Rotate web tokens only after binding and verify the root service process before credential transmission.
- [x] Publish and verify the complete v3.3.1 release from the successful source commit.

The previous local 32602 APK lacked embedded LKM and ksuinit. The reported
android14-6.1 patch failure confirms that it cannot be used to patch boot images.
Future release builds must fail when any required embedded asset is missing.

## Initial findings (original migration)

- The original workspace was a source archive without a `.git` directory.
- The `7k SU` reference is decompiled Java, not a buildable Android project.
- CosmeticEdition has particle effects and launcher hiding with a dial-code receiver.
- Preserve the original `ikunSU.apk` as an identity/reference artifact.

## Implementation decisions

- Manager package: `com.hons.ikunSU`; display name: `ikunSU`. Keep the JNI namespace
  `me.weishu.kernelsu` and existing kernel IOCTL/profile structures.
- Falling overlay uses a drawing-only Compose Canvas, with user-controlled snow,
  image size and speed. Animation stops when the activity is not resumed.
- Launcher recovery now uses the system phone dialer and the exact code
  `*#*#917891786688#*#*`, supporting modern and legacy Android secret-code actions.
  Calculator accessibility, app selection and verification were removed at the
  user's request. Only the launcher alias is disabled; the recovery receiver and
  MainActivity remain enabled.
- The root web manager binds to loopback only, authenticates all API requests,
  checks Host/Origin and validates mutations. It integrates into ksud autostart.
- A permanent RSA-2048 JKS was generated after the user confirmed the previous
  signing key was lost. Its 878-byte certificate fits the kernel's 1024-byte
  verifier limit. Private signing files are excluded from Git and source ZIPs.
- Public certificate SHA-256:
  `f00129f638639a1d41cb71d4f3e63c25b3a87127ac8eb038dd74ca0774d05c5f`.
- Actions derives certificate identity from the actual signing key and supplies
  it to kernel builds. PR/unsigned CI has a separate temporary identity; release
  tags require permanent secrets. The DDK workspace path is repository-independent.
- Windows archive symlink placeholders are handled by direct shared-header
  includes. Local Android builds use an ASCII path mirror.
- Cache replacement uses an atomic replace supported on Windows, and test path
  comparisons use invariant separators. Partial locale translations are Lint
  warnings and fall back to English, while other errors still fail the build.

## Initial migration validation history

- Android-target Rust check, Clippy and formatting passed in the required order.
- ARM64 release ksud links at API 26 with 16 KiB LOAD alignment.
- Six web authentication/PID tests passed; all Android XML and web JavaScript parse.
- All workflow YAML parses; actionlint passed for every workflow. Main action
  version tags were verified against their public upstream repositories.
- Final Manager `clean assembleRelease lintRelease testDebugUnitTest` passed after
  calculator event filtering changes; all 76 JVM tests passed. Lint has no errors;
  partial translations and existing advisory warnings remain visible.
- APK v2 signature, package, label and ARM64 ABI were verified against the new key.
- On-device dialer recovery and root web operations still require ROM/runtime
  verification; no app was installed or flashed during this source update.
- GitHub repository `Hons123/ikunSU` was created after explicit authorization;
  all four permanent signing secrets are configured. Full LKM builds and the
  first cloud release are now being verified.
- The local ARM64 ksuinit is a static AArch64 ELF with 16 KiB LOAD alignment.
- New asset validation has eight passing tests for a complete matrix, missing
  modules, wrong architecture/KMI/module name, invalid init, and RISC-V init.
- GitHub run `38408970680` successfully built all 16 ko, three Android ksud,
  three ksuinit and the universal Manager after fixing SDK package initialization.
- The complete local ARM64 APK passed 76 JVM tests, 0 Lint errors (413 warnings),
  APK v2 verification and byte comparison with the freshly built embedded daemon.
  Informational commands on the connected device reported version 3.3.1 / UAPI 5
  and all eight KMI assets. No device flash or installation was performed.
- The web grant helper preserves Manager-only kernel policy operations using
  a validated Manager real UID in an isolated root subprocess. Runtime behavior
  still requires a matching loaded kernel and on-device testing.

## Dialer recovery and web presentation follow-up

- [x] Remove calculator accessibility/picker/verification and restore dialer recovery.
- [x] Replace JSON/internal identifiers with responsive Chinese cards and controls.
- [x] Read real installed application labels through Android PackageManager.
- [x] Run the final Rust, Manager, authentication and UI checks.
- [x] Upload the final source and publish the complete v3.3.1 release.

- Final web presentation passed Chrome/Playwright mock checks on 375 px mobile,
  1366 px desktop and dark mode: 48 requests across 11 endpoints, no console errors.
  Dynamic app/module injection samples remained plain text.
- The fixed-FD PackageManager helper read 353 real installed applications in
  about 0.75 seconds. System Chinese labels included Settings and Calculator;
  helper-only English locale produced their English names, confirming localized
  APK resources. No APK installation or system locale change was performed.
- Final Android-target Rust check, Clippy and formatting passed in sequence.
  The freshly built daemon reported 3.3.1 / UAPI 5 and all eight embedded KMIs
  via informational ADB commands; the temporary binary was then removed.

- Final Manager clean assembleRelease / lintRelease / testDebugUnitTest passed:
  78 JVM tests, no failures, 0 Lint errors (417 advisory warnings). The signed
  3.3.1 / 32680 ARM64 APK contains the fresh complete daemon byte-for-byte after
  stripping, with eight LKM assets and ksuinit. Packaged manifest has both dialer
  actions and no calculator accessibility service.
- The actual R8-minified release APK helper, loaded read-only via app_process
  with a fixed FD, also read all 353 real applications with Chinese names.
  No APK installation or device flashing was performed.

- Final source commit c6a9229 passed full GitHub run 38420373575. The existing
  v3.3.1 tag points to that exact commit. Release run 38422876276 passed and
  published 26 assets: Manager, 16 ko, three ksud, three ksuinit, public build
  identity, manifest and checksums. All GitHub asset SHA-256 digests match the
  verified release manifest and downloaded metadata. The release is public,
  stable and uses the permanent signing identity.

## Local home card UI follow-up (2026-10-14)

- [x] Update the active status caption while keeping inactive and warning states accurate.
- [x] Add an icon avatar, introduction, rotating local quotes and attribution card below status in both home themes.
- [x] Use existing theme colors and responsive wrapping; preserve the manager/kernel signing identity.
- [x] Stage matched ksud and init assets, run clean assembleRelease and relevant Manager checks locally.
- [x] Verify the release APK identity and complete embedded KMI assets, then deliver a local APK only.

Both home themes now show the custom active caption and an app-icon introduction
card. Seven local quotes rotate without immediate repetition; state is saved
across Compose recreation. Theme colors, narrow widths and large text are handled.
Emulator previews covered Material and Miuix in light/dark mode and 320 dp width
at 1.5 font scale. Each theme passed seven actual refresh-button clicks. Activated
status in those previews is synthetic UI data in a disposable debug-only harness;
the harness is absent from the signed release APK. No physical device was changed.

The existing local ARM daemon staging contained older embedded KO payloads.
All 16 verified local modules were staged, then Android check, Clippy, formatting,
clean and ARM release compilation passed. The final APK's stripped daemon was
decoded independently: all eight ARM modules exactly match the local collection,
including the user-tested 6.12 golden module, plus the exact tested static init.
Manager clean assembleRelease, lintRelease and all 78 JVM tests passed; Lint has
zero errors (411 existing/translation advisories). APK v2 signature, permanent
certificate, package, label and ARM64 ABI were verified.

This local UI build keeps version 3.3.1-ikunSU.1 / 32681 and uses
ikun91789178/ikunSU as its update repository. The user created that repository
and now requests manual publication instructions: no GitHub creation, push,
asset upload or Actions dispatch is authorized for this UI follow-up.
APK SHA-256: fb1dc4d76d60a14a9b74bcf37bcccd26fa5ddeec4b3402db2309aeaeeb2bb6b9.

## Complete local resource delivery (2026-10-14)

- [x] Update the introduction to include basketball and music in both default and Chinese resources.
- [x] Verify all 16 local modules and stage matching complete daemon/init resources for all Manager ABIs.
- [x] Build and check the signed universal Manager locally.
- [x] Verify APK identity and decoded embedded payloads against the delivered native files.
- [x] Prepare a source-only upload folder plus binaries, checksums and a complete resource archive inside the workspace.

GitHub work is deferred at the user's request. No remote upload, account login,
repository creation, support submission or Actions dispatch belongs to this step.

The signed universal APK contains arm64-v8a, x86_64 and riscv64 Manager JNI and
daemon libraries. All 16 actual compressed KO payloads and all three static init
payloads were decoded from the final APK and matched against this delivery.
Android check, Clippy and formatting passed; x86_64 ksud was built locally with
the complete local KO matrix. RISC-V ksud and the three static init binaries
were reused only after verifying unchanged source/version/signing contracts,
ELF architecture and embedded resource hashes. No RISC-V KO target is offered.
The exact device-tested ARM android16-6.12 module remains byte-identical.

Manager clean assembleRelease, lintRelease and all 78 JVM tests passed, with zero
Lint errors and 411 advisories. The production APK excludes the debug preview
harness and passes permanent-certificate APK v2 signature validation.
APK SHA-256: 44b5551c1e5dd0e363dcf3e8002e2860e0c87395760689d06fde4b754770fe57.

The local delivery is `dist/ikunSU-complete-32681-20261014`: one universal APK,
16 KOs, three ksud binaries and three matching ksuinit binaries. The source
folder contains all 734 current tracked/non-ignored source files, including the
local home UI edits, original licenses and all 12 Actions workflows. Local
signing keys/passwords, Git metadata, machine SDK configuration and build output
are excluded. Workflow syntax checks passed; no remote workflow was dispatched.
Source-only and complete ZIP archives were verified against every file byte;
SHA256SUMS and source/native/Manager manifests are included in the delivery.

## ikun閹绘劖娼?payload integration (2026-10-23)

- [x] Port DirtyFragX's escalation payload into the Manager native build for arm64-v8a.
- [x] Drop its in-app update checker, tutorial and repository links; title the screen ikun閹绘劖娼?
- [x] Let the payload detect this Manager's own package instead of skipping it, so it elevates ikunSU itself.
- [x] Retarget the kernel module's bootstrap slot to the Manager data directory without rebuilding the prebuilt modules.
- [x] Move the entry point out of settings: without Root the bottom bar is hidden, so settings is unreachable.
- [x] Put it on the install page that "閺堫亜鐣ㄧ憗?/ 閻愮懓鍤€瑰顥? opens, which is reachable on an unrooted phone.
- [x] Run the Android checks and build the signed universal Manager locally.

The payload is DirtyFragX (CVE-2026-43284): an AES-CBC `splice()` page-cache
write that patches `crash_dump64`, `libc++` and a vendor library, then loads a
prebuilt `dfroot.ko` which sets SELinux permissive and runs a bootstrap helper as
root. That helper starts `ksud late-load --package-name <manager>`, which is the
same late-load path ikunSU already supports. Only that engine was ported; the
update checker, announcement feed, tutorial page, repository links, boot-time
autorun and the Samsung download-mode indicator were deliberately left out.

DirtyFragX skipped its own package when picking an SU manager because it did not
ship `libksud.so`. ikunSU does ship it in `jniLibs/`, so the Manager is now the
first candidate: `ksud late-load --package-name com.hons.ikunSU` makes ikunSU the
manager of its own elevation instead of only being able to elevate another
installed manager. The screen reports the resolved package, so a wrong detection
is visible rather than silent.

The prebuilt modules cannot be rebuilt here, and they exec their bootstrap helper
from a hard-coded path that was `/data/user_de/0/df.root/bootstrap`. That string
occupies a fixed 33-character slot in the module's read-only data, so `exp.c`
rewrites it in the in-memory copy and the Manager supplies a path of exactly that
length (redundant `/` separators pad it, and the kernel resolves the same file).
The `.ko` snapshots under `manager/app/src/main/dfroot/ko/` stay byte-identical to
upstream. All eight blob images were verified to be embedded byte-exactly in the
built `libdfroot.so`.

The entry point is a single row on the install page, between "use a local LKM
file" and the advanced options. That page is opened by the home status card
("閺堫亜鐣ㄧ憗?/ 閻愮懓鍤€瑰顥?), so it is reachable on an unrooted phone; the home page
itself deliberately carries no second entry. Nothing is placed in settings,
because `BottomBar` returns early when the kernel is not full-featured and the
pager disables scrolling in the same state, so settings cannot be reached at all
on the phones that need the payload. The screen follows both Manager themes and
gates itself on an arm64 process in the primary user, one of the six supported KMI
kernels, the two payload executables and a resolvable `libksud.so`.

This checkout is under a non-ASCII path, which AGP rejects, so local Manager
builds run from an ASCII mirror: `scripts/sync-ascii-mirror.ps1` copies
`manager/`, `uapi/` and `version.properties` to `D:\ikunSU-build` first. The
release APK was built there with the permanent keystore and passes
`assembleRelease`, `lintRelease` (0 errors, only the fork's usual missing
translations plus one intentional `commit()` warning) and 83 JVM tests, five of
them new for the bootstrap path slot.

The signed universal APK keeps `com.hons.ikunSU` / 32681 and the permanent
certificate `f00129f6...5c5f`. It carries `lib/arm64-v8a/libdfroot.so` and
`libbootstrap.so` and still ships `libksud.so` for all three ABIs, which is what
the self-detection depends on. All eight module images were decoded back out of
the packaged `libdfroot.so` and matched the snapshots byte for byte.
APK SHA-256: 8335c82e1938c5921051706e3979186aed30f6bb4207802487406347a67d575b.

No device was flashed or installed during this change, and no remote upload or
Actions dispatch was performed.


## Upstream sync to main 75e179d (2026-10-25)

- [x] Verify the supplied snapshot is upstream `main` at `75e179d` (25 commits after v3.3.1).
- [x] Three-way merge v3.3.1 -> ikunSU fork -> upstream main, keeping every fork feature.
- [x] Resolve the six real conflicts (kernel/Kbuild, install screen/state, three strings files).
- [x] Re-point the fork's version block and signature identity in the merged Kbuild.
- [x] Set the release version to the upstream describe string and commit count.
- [x] Build ksuinit for aarch64 and x86_64 and compile-check ksud and the Manager.
- [ ] Build the eight KMI modules locally (blocked, see below).
- [ ] Build the release ksud and the signed Manager APK once the modules exist.

The merge is a file-level three-way merge with the fork's v3.3.1 base: 533 files
identical, 76 taken from upstream, 76 kept from the fork, 10 merged cleanly, 87
added by upstream, 90 fork-only. Upstream brings the `kernel/preinit` nolibc
runner, `ksud` hot-install and preinit support, the module script system in the
Manager (`data/script`, ScriptRun screens), and bumps `KERNEL_SU_UAPI_VERSION` to
6, so kernel, ksud and Manager all move together.

The fork's ksud commands (`webui`, `stealth`) and Manager additions (ikun UI,
stealth recovery, web manager, ikun閹绘劖娼? survived the merge; `cargo ndk` and the
Manager `compileDebugKotlin` both pass on the merged tree.

Version: `3.3.1-25-g75e179d-ikunSU.1` / `32705`, matching upstream's
`git describe` (`v3.3.1-25-g75e179d`) and its `30000 + commit_count` code.

All eight KMI modules, ksuinit and ksud were built locally. The DDK prebuilts are
Git LFS pointers, but they can be fetched straight through the LFS batch API on
`cnb.cool`; the `kdir-min` variants are the ones that match the container images
(the full kdirs keep `MODVERSIONS` on, which KernelSU's own `check_symbol` rejects
because `__versions` must stay empty). Two host gaps were worked around rather
than fixed: the 6.12/6.18 trees pass `--btf_features` to a pahole 1.25 that does
not know the flag, so a shim drops just that argument, and 6.18's generated
`kdir-min` Makefile hardcodes `KBUILD_OUTPUT=/opt/ddk/kdir/android17-6.18`, so
that path is a symlink into the extracted tree.

Everything is AArch64 `ET_REL`, `name=kernelsu`, carries the permanent Manager
identity, and reports the expected vermagic per KMI (5.10.252, 5.10.245,
5.15.202-android13, 5.15.202, 6.1.166, 6.6.127, 6.12.76, 6.18.32). They were
stripped the way the release workflow strips them, landing at 322-490 KiB.

Local delivery for this round is arm64 only: the eight modules plus ksuinit are
staged in `userspace/ksud/bin/aarch64/`, the release ksud passes its embedded
asset validation, and the Manager was built with `-PKSU_ABIS=arm64-v8a`. The
x86_64 and riscv64 entries under `userspace/ksud/bin/` and `jniLibs/` are still
the previous v3.3.1 artifacts and must be regenerated before a universal APK is
published; building them needs the x86_64 kdir preparation step and a nightly
Rust toolchain for riscv64.

## Local build result (2026-10-25)

- [x] Merge upstream main `75e179d` into the fork and resolve all six conflicts.
- [x] Sync the version to `3.3.1-25-g75e179d-ikunSU.1` / `32705`.
- [x] Fetch the DDK prebuilts through the LFS API and build all eight KMI modules.
- [x] Build static ksuinit for aarch64 and x86_64.
- [x] Build the release ksud with the complete embedded asset matrix.
- [x] Build and sign the arm64 Manager APK with the permanent identity.

APK: `ikunSU_v3.3.1-25-g75e179d-ikunSU.1_32705-release.apk`, 9,249,474 bytes,
`com.hons.ikunSU` / 32705, arm64-v8a only, signed with the permanent certificate
`f00129f6...5c5f`. It carries `libksud.so` (all eight modules plus ksuinit
embedded), `libkernelsu.so`, `libadbroot.so`, and the ikun提权 payload
(`libdfroot.so`, `libbootstrap.so`).
APK SHA-256: 1606d375dfb1ca33d98c4755f61722d8f8fdc6c04f57c6b9a8c66ed16ca827a1.

No device was flashed or installed, and no remote upload or Actions dispatch was
performed.
