//! SUSFS guard LKM control.
//!
//! The module ships inside the ksud assets as `<kmi>_susfs_guard_lkm.ko`. Loading
//! goes through [`ksuinit::load_module`], which rewrites the module vermagic to the
//! value the running kernel actually asks for, so a module built against one GKI
//! patch level still loads on a device that ships another one.

use anyhow::{Context, Result, bail};
use std::ffi::CString;
use std::path::Path;

use crate::{assets, boot_patch};

/// Kernel-side module name; also its directory under `/sys/module`.
const MODULE_NAME: &str = "susfs_guard_lkm";
const MODULE_SYSFS: &str = "/sys/module/susfs_guard_lkm";

/// Values shared with `kernel/susfs/susfs_abi.h`.
const SUSFS_MAGIC: usize = 0xFAFA_FAFA;
const KSU_INSTALL_MAGIC1: usize = 0xDEAD_BEEF;
const CMD_SUSFS_SHOW_VERSION: usize = 0x5_55e1;
const CMD_SUSFS_SHOW_VARIANT: usize = 0x5_55e3;
const ERR_CMD_NOT_SUPPORTED: i32 = 126;
const VERSION_BUFSIZE: usize = 16;
const VARIANT_BUFSIZE: usize = 16;

#[repr(C)]
struct SusfsString {
    value: [u8; VERSION_BUFSIZE],
    err: i32,
}

#[repr(C)]
struct SusfsVariant {
    value: [u8; VARIANT_BUFSIZE],
    err: i32,
}

pub fn is_loaded() -> bool {
    Path::new(MODULE_SYSFS).exists()
}

fn detect_kmi() -> Result<String> {
    boot_patch::get_current_kmi().context("failed to detect kernel KMI")
}

/// Loads the guard module, auto-detecting the KMI when `kmi` is `None`.
pub fn load(kmi: Option<&str>) -> Result<()> {
    if is_loaded() {
        println!("SUSFS guard LKM is already loaded");
        return Ok(());
    }
    let kmi = match kmi {
        Some(value) => value.to_owned(),
        None => detect_kmi()?,
    };
    let name = format!("{kmi}_susfs_guard_lkm.ko");
    let data = assets::get_asset_data(&name)
        .with_context(|| format!("SUSFS guard asset is unavailable for KMI {kmi}"))?;
    let params = CString::new("").expect("empty module params are valid");
    ksuinit::load_module(&data, &params).with_context(|| format!("failed to load {name}"))?;
    println!("Loaded SUSFS guard LKM for KMI {kmi}");
    Ok(())
}

pub fn unload() -> Result<()> {
    if !is_loaded() {
        println!("SUSFS guard LKM is not loaded");
        return Ok(());
    }
    rustix::system::delete_module(c"susfs_guard_lkm", 0)
        .context("failed to unload SUSFS guard LKM")?;
    println!("Unloaded SUSFS guard LKM");
    Ok(())
}

fn show_version() -> Option<String> {
    let mut info = SusfsString {
        value: [0; VERSION_BUFSIZE],
        err: ERR_CMD_NOT_SUPPORTED,
    };
    unsafe {
        libc::syscall(
            libc::SYS_reboot,
            KSU_INSTALL_MAGIC1,
            SUSFS_MAGIC,
            CMD_SUSFS_SHOW_VERSION,
            &raw mut info,
        );
    }
    if info.err != 0 {
        return None;
    }
    let length = info
        .value
        .iter()
        .position(|b| *b == 0)
        .unwrap_or(VERSION_BUFSIZE);
    (length != 0).then(|| String::from_utf8_lossy(&info.value[..length]).into_owned())
}

fn show_variant() -> Option<String> {
    let mut info = SusfsVariant {
        value: [0; VARIANT_BUFSIZE],
        err: ERR_CMD_NOT_SUPPORTED,
    };
    unsafe {
        libc::syscall(
            libc::SYS_reboot,
            KSU_INSTALL_MAGIC1,
            SUSFS_MAGIC,
            CMD_SUSFS_SHOW_VARIANT,
            &raw mut info,
        );
    }
    if info.err != 0 {
        return None;
    }
    let length = info
        .value
        .iter()
        .position(|b| *b == 0)
        .unwrap_or(VARIANT_BUFSIZE);
    (length != 0).then(|| String::from_utf8_lossy(&info.value[..length]).into_owned())
}

/// Prints `v2.4.0 (GKI-LKM)` style output.
pub fn version() -> Result<()> {
    if !is_loaded() {
        bail!("SUSFS guard LKM is not loaded");
    }
    let Some(version) = show_version() else {
        bail!("kernel did not answer CMD_SUSFS_SHOW_VERSION");
    };
    match show_variant() {
        Some(variant) => println!("{version} ({variant})"),
        None => println!("{version}"),
    }
    Ok(())
}

/// Machine readable state used by the Manager.
pub fn status() {
    let loaded = is_loaded();
    let version = if loaded {
        show_version().unwrap_or_default()
    } else {
        String::new()
    };
    let variant = if loaded {
        show_variant().unwrap_or_default()
    } else {
        String::new()
    };
    println!("loaded={loaded}");
    println!("module={MODULE_NAME}");
    println!("version={version}");
    println!("variant={variant}");
}

const AUTOLOAD_CONFIG: &str = "/data/adb/ksu/susfs.autoload";

/// Whether the guard module should be loaded on every boot.
pub fn autoload_enabled() -> bool {
    std::fs::read_to_string(AUTOLOAD_CONFIG).is_ok_and(|content| content.trim() == "1")
}

pub fn set_autoload(enabled: bool) -> Result<()> {
    let path = std::path::Path::new(AUTOLOAD_CONFIG);
    if let Some(parent) = path.parent() {
        std::fs::create_dir_all(parent).ok();
    }
    std::fs::write(path, if enabled { "1\n" } else { "0\n" })
        .with_context(|| format!("failed to write {AUTOLOAD_CONFIG}"))?;
    println!(
        "SUSFS auto-load {}",
        if enabled { "enabled" } else { "disabled" }
    );
    Ok(())
}

/// Called from the boot events: loads the module when auto-load is on.
pub fn load_if_autoload_enabled() {
    if !autoload_enabled() || is_loaded() {
        return;
    }
    if let Err(error) = load(None) {
        eprintln!("susfs auto-load failed: {error:#}");
    }
}
