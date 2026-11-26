# OutlookBypass

LSPosed module that skips the MDM / device-administrator enrollment screen in **Microsoft Outlook for Android** (`com.microsoft.office.outlook`), so you can sign in with an Exchange account without enrolling the device.

## How it works

Outlook decides whether device management is needed in `com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy`:

- `requiresDeviceManagement()` is forced to return `false`
- `isPolicyApplied()` is forced to return `true`

Both hooks live inside the Outlook process only (static scope), nothing else is touched.

## Tested with

- Android 17 (SDK 37), HyperOS 4.0.6
- KernelSU 32680 (LKM) + Zygisk Next 1.5.2
- LSPosed 2.2.1 (libxposed API 102)
- Outlook 5.2645.1

Android 15 / 16 and Outlook 5.2+ also work. The module is built against libxposed API 101, so it needs an LSPosed build with the new API (legacy Xposed API 82 modules are not supported).

## Install

1. Download the APK from [Releases](../../releases) or from an Actions artifact and install it (`minSdk 26`).
2. Enable the module in LSPosed and make sure Outlook is in scope.
3. Force-stop Outlook, then open it.

If a build with the same package name but a different signature is already installed, uninstall it first.

### KernelSU + Zygisk Next

When Zygisk Next runs in allowlist mode (`enforce_denylist=1`), only apps present in KernelSU's allowlist get injected:

1. Add Outlook to the KernelSU superuser list (the root toggle can stay off).
2. Reboot, because the kernel reads `/data/adb/ksu/.allowlist` at boot.

Without this step LSPosed never injects into Outlook and the module log stays empty.

## Build

Pushing to `main` or opening a PR runs `.github/workflows/build.yml`, which builds a release APK and uploads it as an artifact. Pushing a `v*` tag also publishes a GitHub Release.

Local build (JDK 21, Android SDK 36):

```bash
./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`.

The release build is debug-signed by default and installs as-is. To use your own key, provide `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` (environment variables locally, repository secrets in CI).

## Fork and upstream

Forked from [takattowo/OutlookDevicePolicyBypass](https://github.com/takattowo/OutlookDevicePolicyBypass). Changes on top of upstream:

- build against libxposed API 101 so it loads on newer LSPosed builds
- R8 disabled, so the module entry class is not stripped
- hooks installed in `onPackageLoaded` using the Outlook class loader
- compileSdk 36, versionCode 3 / versionName 1.1.1

Sync with upstream:

```bash
git remote add upstream https://github.com/takattowo/OutlookDevicePolicyBypass.git
git fetch upstream
git merge upstream/main
```

The "Sync fork" button on GitHub works as well.

## Notes

The module only changes Outlook's local client-side check; server-side and organizational MDM policy are untouched. Whether this use is acceptable is up to your organization's rules.

## License

[MIT](LICENSE). Original copyright belongs to takattowo.

---

## 中文说明

适用于 Android 的 LSPosed 模块，跳过 Outlook 登录 Exchange 账号时弹出的 MDM / 设备管理员激活页，不用把手机注册为受管设备。

- 作用域固定为 Outlook，Hook `DevicePolicy.requiresDeviceManagement()`（返回 `false`）和 `isPolicyApplied()`（返回 `true`）
- 已验证：Android 17（SDK 37）/ HyperOS 4.0.6、KernelSU 32680 + Zygisk Next 1.5.2、LSPosed 2.2.1（libxposed API 102）、Outlook 5.2645.1
- 安装：装 APK → LSPosed 启用模块并勾选 Outlook → 强行停止 Outlook 再打开
- 若 Zygisk Next 处于白名单模式（`enforce_denylist=1`）：需在 KernelSU 超级用户列表里加入 Outlook（root 开关可保持关闭），然后重启，否则模块不会被注入
- 构建：推送或打 `v*` 标签会自动出包；本地执行 `./gradlew :app:assembleRelease`（JDK 21、SDK 36），默认 debug 签名
- 上游：[takattowo/OutlookDevicePolicyBypass](https://github.com/takattowo/OutlookDevicePolicyBypass)，MIT 许可
