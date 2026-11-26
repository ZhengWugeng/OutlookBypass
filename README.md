# OutlookBypass

适用于 Android 的 LSPosed 模块，绕过 **Microsoft Outlook for Android**（`com.microsoft.office.outlook`）登录 UCF 等 Exchange 邮箱时要求的 MDM 设备管理员激活流程，让你不用把手机注册为受管设备就能正常收邮件。

LSPosed module that skips the MDM / device-administrator enrollment screen in Microsoft Outlook for Android.

## 原理

Outlook 在 `com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy` 上判断是否需要设备管理：

- `requiresDeviceManagement()` 强制返回 `false`
- `isPolicyApplied()` 强制返回 `true`

模块只在自己的进程里 Hook 这两个方法，作用域固定为 Outlook（`staticScope=true`），不触碰其他应用。

## 已验证环境

- 小米 17 Pro Max，HyperOS 4.0.6，Android 17（SDK 37）
- KernelSU 32680（LKM）+ Zygisk Next 1.5.2
- LSPosed 2.2.1（libxposed API 102）
- Outlook 5.2645.1

Android 15 / 16 与 Outlook 5.2+ 同样适用。模块使用 libxposed API 101，需要支持新 API 的 LSPosed（旧版 Xposed API 82 不再支持）。

## 安装

1. 从 [Releases](../../releases) 或 Actions 构建产物下载 APK 并安装（`minSdk 26`）。
2. 打开 LSPosed 管理器 → 启用模块 → 确认作用域里有 Outlook。
3. 强行停止 Outlook 再打开。

如果之前装过其他签名的同包名模块，需要先卸载再安装。

### KernelSU + Zygisk Next 注意

若 Zygisk Next 处于白名单模式（`enforce_denylist=1`，即只有 KernelSU 白名单里的应用会被注入），还需要：

1. 在 KernelSU 管理器的超级用户列表里加入 Outlook（root 开关保持关闭即可）。
2. 重启手机（内核开机时读取 `/data/adb/ksu/.allowlist`）。

否则 LSPosed 不会注入 Outlook，模块日志也不会有任何记录。

## 构建

推送到 `main` 或提 PR 时，`.github/workflows/build.yml` 会自动构建 release APK 并上传为 artifact；打 `v*` 标签会额外发布 GitHub Release。

本地构建（JDK 21，SDK 36）：

```bash
./gradlew :app:assembleRelease
```

产物：`app/build/outputs/apk/release/app-release.apk`。

默认使用 debug 签名，可直接安装；如需自己的签名，设置 `KEYSTORE_FILE`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`（或仓库 Secrets 同名变量）。

## 上游与同步

本仓库 fork 自 [takattowo/OutlookDevicePolicyBypass](https://github.com/takattowo/OutlookDevicePolicyBypass)，在其基础上做了兼容性修复（Android 17 / LSPosed 2.2.1 可用的 libxposed 101 构建、关闭 R8 混淆避免模块入口被裁剪、Hook 挂在 `onPackageLoaded` 并用目标 ClassLoader 加载类）。

同步上游：

```bash
git remote add upstream https://github.com/takattowo/OutlookDevicePolicyBypass.git
git fetch upstream
git merge upstream/main
```

也可以直接在 GitHub 页面点 “Sync fork”。

## 说明

模块只修改 Outlook 客户端的本地判断，不改变服务器或组织的 MDM 策略；是否允许这样使用由你所在组织的规定决定。

## 许可

[MIT](LICENSE)，原始版权归 takattowo 所有。
