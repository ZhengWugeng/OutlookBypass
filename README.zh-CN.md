# OutlookBypass

[English](README.md) | 简体中文

适用于 Android 的 LSPosed 模块，跳过 **Microsoft Outlook for Android**（`com.microsoft.office.outlook`）的 MDM / 设备管理员激活页，让你不用把手机注册为受管设备就能登录 Exchange 账号。

## 原理

Outlook 在 `com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy` 里判断是否需要设备管理：

- `requiresDeviceManagement()` 强制返回 `false`
- `isPolicyApplied()` 强制返回 `true`

两个 Hook 只在 Outlook 进程内生效（静态作用域），不触碰其他应用。

## 已验证环境

- Android 17（SDK 37），HyperOS 4.0.6
- KernelSU 32680（LKM）+ Zygisk Next 1.5.2
- LSPosed 2.2.1（libxposed API 102）
- Outlook 5.2645.1

Android 15 / 16 与 Outlook 5.2+ 同样可用。模块基于 libxposed API 101 构建，需要支持新 API 的 LSPosed（不再支持旧版 Xposed API 82 模块）。

## 安装

1. 从 [Releases](../../releases) 或 Actions 构建产物下载 APK 并安装（`minSdk 26`）。
2. 在 LSPosed 中启用模块，并确认作用域包含 Outlook。
3. 强行停止 Outlook，然后重新打开。

如果已经装了同包名但签名不同的版本，需要先卸载。

### KernelSU + Zygisk Next

当 Zygisk Next 处于白名单模式（`enforce_denylist=1`）时，只有 KernelSU 白名单里的应用会被注入：

1. 把 Outlook 加入 KernelSU 超级用户列表（root 开关可以保持关闭）。
2. 重启手机，因为内核在开机时读取 `/data/adb/ksu/.allowlist`。

不做这一步，LSPosed 不会注入 Outlook，模块日志也不会有任何记录。

## 构建

推送到 `main` 或提 PR 会运行 `.github/workflows/build.yml`，构建 release APK 并上传为 artifact；推送 `v*` 标签还会发布 GitHub Release。

本地构建（JDK 21、Android SDK 36）：

```bash
./gradlew :app:assembleRelease
```

产物：`app/build/outputs/apk/release/app-release.apk`。

release 默认使用 debug 签名，可以直接安装。如需自己的签名，提供 `KEYSTORE_FILE`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`（本地用环境变量，CI 用仓库 Secrets）。

## 上游与同步

本仓库 fork 自 [takattowo/OutlookDevicePolicyBypass](https://github.com/takattowo/OutlookDevicePolicyBypass)，在上游基础上的改动：

- 改用 libxposed API 101 构建，可在较新的 LSPosed 上加载
- 关闭 R8，避免模块入口类被裁剪
- Hook 安装在 `onPackageLoaded`，并使用 Outlook 的 ClassLoader
- compileSdk 36，versionCode 3 / versionName 1.1.1

同步上游：

```bash
git remote add upstream https://github.com/takattowo/OutlookDevicePolicyBypass.git
git fetch upstream
git merge upstream/main
```

也可以直接在 GitHub 页面点 “Sync fork”。

## 说明

模块只修改 Outlook 客户端的本地判断，不改变服务器端或组织的 MDM 策略；是否允许这样使用取决于你所在组织的规定。

## 许可

[MIT](LICENSE)，原始版权归 takattowo 所有。
