# Local web manager API

## Hidden launcher and phone recovery

The Manager and `/api/feature_toggle` prepare recovery before disabling the
launcher alias. Preparation failures leave the icon visible. The recovery code
is `*#*#917891786688#*#*`; no phone call is required. The exported receiver accepts
only the matching modern/legacy Android secret-code URI and restores that alias.

On MIUI/HyperOS, the protected installed daemon starts `DialerRecoveryGuard`
through the same certificate-pinned Manager APK descriptor used for app labels.
It keeps only `com.hons.ikunSU` in `MILLET_NO_RESTRICT_APP`, retains other entries,
and permits this package's vendor self-start operation. The isolated root helper
leaves application cgroups and checks membership every five seconds while the
icon is hidden; restoring the icon stops the helper. Private PID/nonce records
are root-only; stop validates process start time, root UID, arguments and APK
inode before using a pidfd. It never modifies the dialer or global freezer policy.

```sh
ksud stealth start
ksud stealth status
ksud stealth stop
```

After boot, a hidden launcher gets recovery prepared again. If preparation fails,
ksud restores the icon. On other systems the normal secret-code receiver is used;
OEM dialer support and background permission requirements can vary.

Start the service from Settings > ikunSU features > Start and open web manager,
or run the following commands as root with the freshly built daemon:

```sh
ksud webui start
ksud webui url
ksud webui autostart on
ksud webui autostart off
ksud webui autostart status
ksud webui stop
```

The service listens only on `127.0.0.1:9178`. The `url` command prints a URL whose
fragment contains the authentication token. The browser stores it for that tab
and removes the fragment from the address bar. API requests require
`Authorization: Bearer <token>`. The token is random, root-owned, mode 0600, and
stored at `/data/adb/ksu/webui.token`. The page never grants access to other origins.
Host must be `127.0.0.1:9178`; an Origin header, if supplied, must match the local
origin. Do not share the authenticated URL or token.

Every successful server start first binds the loopback port, then generates and
atomically replaces its token, and finally publishes a root-owned mode 0600
`/data/adb/ksu/webui.pid` record. CLI health checks, stop and URL commands require
that PID record to identify a root process whose command line is exactly
`<executable> webui serve` before they read or transmit the token. A process
merely occupying port 9178 cannot obtain credentials for the next real server.
Previous URLs and browser sessionStorage tokens stop working after a restart;
open a fresh URL from the Manager. Normal shutdown removes only the service's
own PID record before releasing its socket.

If an earlier daemon without a PID record is already running, stop it once with
the earlier Manager/daemon, or reboot with autostart disabled, before starting
this updated service. The updated CLI rejects unverified legacy listeners.

| Endpoint | Method | Body / response |
| --- | --- | --- |
| `/api/status` | GET | Manager/kernel versions, runtime mode, launcher hiding, boot autostart |
| `/api/features` | GET | Supported, enabled, forced and module-managed feature states |
| `/api/apps` | GET | Primary-user package names, real application labels, UIDs, system flags and root grants |
| `/api/superuser` | GET | Granted packages |
| `/api/superuser_toggle` | POST | `{"package":"example.app","uid":10001,"granted":true}` |
| `/api/modules` | GET | Existing ksud module list |
| `/api/module_action` | POST | `{"id":"module_id","act":"enable"}`; actions: enable, disable, uninstall, undo, action |
| `/api/module_install` | POST | Raw ZIP, `Content-Type: application/zip`, known Content-Length, maximum 256 MiB |
| `/api/feature_toggle` | POST | `{"name":"su_compat","enabled":true}`; names from features, plus `stealth` and `webui_autostart` |
| `/api/feature_save` | POST | Persist feature configuration |
| `/api/su_log_dump` | GET | `{"log":"..."}`; last 256 KiB of the most recent SU log |
| `/api/ping` | POST | Health check |
| `/api/shutdown` | POST | Stop service |

JSON request bodies are limited to 16 KiB. Errors return JSON with an `error`
message. Mutations require POST and an exact token. Package/UID combinations and
module IDs are validated; commands receive separate arguments. Root grants are
per UID and use the default profile, so changing them resets a custom profile.

Application cards use real localized labels from Android PackageManager. The
one-shot `WebAppLabels` helper reads APK resources without loading third-party
application code. ksud verifies the installed Manager's kernel identity and
its APK v2 certificate against the same build pin before loading that helper.
Package and locale changes refresh a bounded cache; successful entries expire
after ten minutes, with thirty-second retry backoff after a failure. Labels are
attached only when the UID matches the live primary-user package record. Missing
labels fall back to package names. These display fields never authorize an app.
An application with only an English label keeps its original name.

Launcher hiding applies to Android user 0. Enter `*#*#917891786688#*#*`
in the system phone dialer to restore the icon. Both modern and legacy Android
secret-code broadcasts are supported; no call or accessibility service is needed.
Dialer support depends on the ROM. To restore the icon through ADB:

```sh
adb shell pm enable com.hons.ikunSU/me.weishu.kernelsu.Launcher
```

Service autostart is integrated into ksud's service event and respects safe mode.
The background server starts from the installed `/data/adb/ksud` executable,
detaches its session, and moves out of the Manager's application cgroups before
creating HTTP worker threads. Hiding the launcher or closing the Manager should
therefore leave the server running. The installed binary must be root-owned and
not writable by other users; the Manager installs its bundled daemon on startup.
It requires the updated ksud to be installed on the device. Startup diagnostics
are in `/data/adb/ksu/webui.log`. A Manager APK alone cannot validate runtime root
behavior without a kernel that recognizes its package and signing certificate.
