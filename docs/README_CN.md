[English](README.md) | [Español](README_ES.md) | **简体中文** | [繁體中文](README_TW.md) | [日本語](README_JP.md) | [한국어](README_KR.md) | [Polski](README_PL.md) | [Português (Brasil)](README_PT-BR.md) | [Türkçe](README_TR.md) | [Русский](README_RU.md) | [Tiếng Việt](README_VI.md) | [Indonesia](README_ID.md) | [עברית](README_IW.md) | [हिंदी](README_IN.md) | [Italiano](README_IT.md)

# ikunSU

这是基于 KernelSU 的二次开发版本，管理器名称为 **ikunSU**，包名为 `com.hons.ikunSU`，图标取自原来的 ikunSU.apk。内核与 JNI 的接口和命名空间保持兼容。

## 本次新增功能

- 设置 → **ikunSU 功能**：开启屏幕飘落动画，可切换 ikun 图片和雪花，调整大小、速度。
- 隐藏桌面图标后，在系统电话拨号盘输入 **`*#*#917891786688#*#*`** 恢复，无需拨打电话或设置无障碍服务。
- 本机网页管理器：`http://127.0.0.1:9178`，提供中文状态卡片、应用真实名称、内核功能说明、模块 ZIP 安装和操作、SU 日志、网页开机自启，适配手机与电脑布局。
- GitHub Actions 联动构建管理器 APK、ksud、ksuinit 和各 KMI 的 ko，并自动保持 APK 签名与内核证书校验一致。

## 拨号恢复的使用方法

1. 在“ikunSU 功能”中复制完整恢复代码，并启用“隐藏桌面图标”。
2. 在系统电话拨号盘输入 `*#*#917891786688#*#*`，最后一个字符输入后即会恢复图标，并尝试打开管理器，无需按拨号键。

修订版 **3.3.1-ikunSU.1 / 32681** 会先准备独立 Root 恢复守护；准备失败时保留图标。在 Xiaomi / HyperOS 上仅处理 ikunSU 自身的后台冻结限制，无需修改拨号器。隐藏后网页服务继续独立运行。“ikunSU 功能”页面也会沿用首页选择的 Miuix / Material 主题。

拨号器需要支持 Android 秘密代码广播。部分系统限制后台打开界面时，图标仍会恢复，可以从桌面进入。没有电话拨号器的平板可用下方 ADB 命令恢复。

如需从电脑恢复图标：

```sh
adb shell pm enable com.hons.ikunSU/me.weishu.kernelsu.Launcher
```

## 网页管理器

从“ikunSU 功能”点击“启动并打开网页管理器”，管理器会通过 Root 启动新版 ksud 的网页服务，并打开带认证令牌的本机页面。首次打开需要可用的 Root 和匹配的新内核。手动输入一个不含令牌的地址不能调用管理 API。网页仅监听手机本机回环地址，接口说明见 [IKUNSU_WEB_API.md](IKUNSU_WEB_API.md)。

## 已验证的 6.12 安装与镜像修补

用户已刷入本地制作的匹配镜像并确认 Root 可用，截图中管理器显示“工作中”和 `32681-5`：

| 项目 | 实测值 |
| --- | --- |
| 设备 | vivo iPA2691 |
| 系统 | Android 17 |
| 内核 | 6.12.30 |
| 内核 KMI | `android16-6.12` |
| 匹配版本 | ikunSU 32681 / UAPI 5 |

此前无 Root 时，设备实际运行的是 **40750 / UAPI 0**，与本版管理器所需的 32681 / UAPI 5 不一致。换用匹配的 KO、ksud 和 ksuinit 制作 `init_boot` 后验证通过；此次修复无需更换包名、签名或修改内核/ksud 接口。Android 系统版本和内核 KMI 可以不同，选 KO 应以实际内核 KMI 为准。

修补 `boot` 或 `init_boot` 请使用匹配版本 ksud 的普通 **`boot-patch`**，由它设置 ramdisk 的 ksuinit 入口并加入 `kernelsu.ko`。原 ramdisk 中有 `init` 时会保存为 `init.real`，其余有效原厂条目保留。只替换 KO 文件无法代替这一加载流程。外部修补工具的兼容性尚未验证。

例如，准备好匹配文件和 `output` 目录后，用 AArch64 版 ksud 生成镜像：

```sh
ksud boot-patch -b init_boot.img --kmi android16-6.12 \
  -m lkm-aarch64-android16-6.12_kernelsu.ko -i ksuinit-aarch64 \
  -o output --out-name init_boot_ikunSU_android16-6.12.img
```

该命令只生成文件，没有 `--flash` 参数。在非 Android 主机版 ksud 上另加 `--arch aarch64`。`boot-patch-v2` 要求镜像包含内核，不能用于本例只有 ramdisk 的 `init_boot`。静态镜像检查通过仍需刷入后的 Root 验证；目前这项真机结论仅覆盖上述 AArch64 / `android16-6.12` 组合。

本轮已在本机完成 **8 个 KMI × AArch64、x86_64 两种架构，共 16 个 KO**：`android12-5.10`、`android13-5.10`、`android13-5.15`、`android14-5.15`、`android14-6.1`、`android15-6.6`、`android16-6.12`、`android17-6.18`。6.12 ARM64 保留上述已实测的本地编译文件，其余 15 个本次重新编译。全部采用同一永久管理器身份，并已独立检查 ELF 架构、KMI、包名、证书和 SHA-256。其他组合的构建检查不代表已在真机验证 Root。

[下载完整本地 KO 整包](https://github.com/Hons123/ikunSU/releases/download/v3.3.1-ikunSU.1/ikunSU-local-LKM-v3.3.1-ikunSU.1_32681-20261014.zip)。对应[构建清单](https://github.com/Hons123/ikunSU/releases/download/v3.3.1-ikunSU.1/local-lkm-manifest-32681-20261014.json)和[SHA-256 校验值](https://github.com/Hons123/ikunSU/releases/download/v3.3.1-ikunSU.1/SHA256SUMS-local-lkm-32681-20261014)也已发布。原有管理器、ksud、ksuinit 和 Actions KO 的完整匹配发布集保留在同一 Release。

## 上传 GitHub 与自动构建

将完整源码上传到自己的仓库，例如 `Hons123/ikunSU`，必须包含隐藏的 `.github` 目录。可以使用本次生成的纯源码 ZIP；不要上传 `private-signing`、JKS、密码文件、旧参考 APK、构建缓存或 `dist`。

在仓库 Settings → Secrets and variables → Actions 新增四个仓库 Secrets：

| 名称 | 值 |
| --- | --- |
| `KEYSTORE` | 正式 JKS 的 Base64 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | `ikunsu` |
| `KEY_PASSWORD` | 私钥密码 |

这些值已保存在本机被忽略的 `private-signing/github-secrets.json` 中。请逐个复制值到 Secrets，**不要把这个 JSON 上传到仓库**。正式密钥为 `private-signing/ikunSU-release.jks`，密码单独保存在 `password.txt`，请另做备份。`public-identity.json` 只有公开证书信息。

进入 Actions → **Build ikunSU** → Run workflow，或向 main/master/dev/ci 分支推送代码，即可构建。完成后下载 `manager`、`ksud-目标架构`、`ksuinit-架构`、`架构-KMI-lkm` 等产物。ko 按设备的架构和内核 KMI 选择，不能仅按 Android 系统版本选择。

未配置 Secrets 的普通 CI 会为该次构建生成临时测试签名，并同步生成匹配的 ko；临时签名每次变化，不适合长期使用。配置正式 Secrets 后，推送 `v*` 标签会构建并发布 GitHub Release。更新检查地址会自动指向运行工作流的仓库。完整 ko 构建矩阵已在 GitHub 上成功编译。

因为旧版签名密钥已丢失，新密钥签名的 APK **无法直接覆盖安装旧版**。首次安装新版需要卸载旧版，请先保留需要的应用数据。以后持续使用本次正式密钥，就可以覆盖升级。旧 ko 也需要重新编译以识别新证书。

版本由根目录 `version.properties` 统一为 **3.3.1-ikunSU.1 / 32681**，基于上游 KernelSU v3.3.1 / UAPI 5。此前 v3.3.1 / 32680 发布保留不变；当前修复版在 [v3.3.1-ikunSU.1](https://github.com/Hons123/ikunSU/releases/tag/v3.3.1-ikunSU.1) 下载。之前的 32602 本地包未嵌入 ko 和 ksuinit，不能用于修补内核；现在 release 构建会检查完整资源，缺失时立即失败。

**Build ikunSU kernel modules** 可以单独手动编译 8 个 KMI × 2 个架构共 16 个 ko，`lkm-all` 产物包含官方格式文件名、哈希和清单。**Release ikunSU** 只允许从与版本文件一致的已有标签运行；正式签名、16 个 ko、完整管理器、3 个 Android ksud 和 3 个 ksuinit 全部检查成功后才会发布。认证原理见 [认证说明](IKUNSU_AUTHENTICATION.md)。

## 原项目说明与许可

下面保留 KernelSU 的原项目说明和开源许可。界面行为参考了旧版 ikunSU APK 与 CosmeticEdition 源码，新增动画和拨号恢复使用当前 Compose/Android 接口实现。

<img src="https://kernelsu.org/logo.png" style="width: 96px;" alt="logo">

一个 Android 上基于内核的 root 方案。

[![Latest release](https://img.shields.io/github/v/release/tiann/KernelSU?label=Release&logo=github)](https://github.com/tiann/KernelSU/releases/latest)
[![Weblate](https://img.shields.io/badge/Localization-Weblate-teal?logo=weblate)](https://hosted.weblate.org/engage/kernelsu)
[![Channel](https://img.shields.io/badge/Follow-Telegram-blue.svg?logo=telegram)](https://t.me/KernelSU)
[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-orange.svg?logo=gnu)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)
[![GitHub License](https://img.shields.io/github/license/tiann/KernelSU?logo=gnu)](/LICENSE)

## 特性

- 基于内核的 `su` 和权限管理。
- 基于 [metamodules](https://kernelsu.org/zh_CN/guide/metamodule.html) 的模块系统：可插拔的模块架构。
- [App Profile](https://kernelsu.org/zh_CN/guide/app-profile.html): 把 Root 权限关进笼子里。

## 兼容状态

KernelSU 官方支持 GKI 2.0 的设备（内核版本5.10以上）；旧内核也是兼容的（最低4.14+），不过需要自己编译内核。

WSA, ChromeOS 和运行在容器上的 Android 也可以与 KernelSU 一起工作。

目前支持 `arm64-v8a` 和 `x86_64` 架构。

> [!CAUTION]
> 最近的内核版本引入了一项破坏性更改，导致 KernelSU 在 `x86_64` 上运行失败，甚至可能引发内核恐慌 (kernel panic)！请查看网站获取更多信息！

## 使用方法

- [安装教程](https://kernelsu.org/zh_CN/guide/installation.html)
- [如何构建？](https://kernelsu.org/zh_CN/guide/how-to-build.html)
- [官方网站](https://kernelsu.org/zh_CN/)

## 参与翻译

要将 KernelSU 翻译成您的语言，或完善现有的翻译，请使用 [Weblate](https://hosted.weblate.org/engage/kernelsu/)。现已不再接受有关管理器翻译的PR，因为这会与Weblate冲突。

## 讨论

- Telegram: [@KernelSU](https://t.me/KernelSU)

## 安全性

有关报告 KernelSU 安全漏洞的信息，请参阅 [SECURITY.md](/SECURITY.md)。

## 许可证

- 目录 `kernel` 下所有文件为 [GPL-2.0-only](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)。
- 除 `kernel` 目录的其他部分均为 [GPL-3.0-or-later](https://www.gnu.org/licenses/gpl-3.0.html)。

## 鸣谢

- [kernel-assisted-superuser](https://git.zx2c4.com/kernel-assisted-superuser/about/)：KernelSU 的灵感。
- [Magisk](https://github.com/topjohnwu/Magisk)：强大的 root 工具箱。
- [genuine](https://github.com/brevent/genuine/)：apk v2 签名验证。
- [Diamorphine](https://github.com/m0nad/Diamorphine)：一些 rootkit 技巧。
