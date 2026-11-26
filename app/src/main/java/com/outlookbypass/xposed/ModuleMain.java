package com.outlookbypass.xposed;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;

public class ModuleMain extends XposedModule {

    private static final String TAG = "OutlookPolicyBypass";
    private static final String TARGET_PKG = "com.microsoft.office.outlook";
    private static final String DEVICE_POLICY_CLASS =
            "com.microsoft.office.outlook.olmcore.managers.mdm.DevicePolicy";

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        log(Log.INFO, TAG, "module loaded in " + param.getProcessName()
                + "; framework=" + getFrameworkName() + " " + getFrameworkVersion());
    }

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        log(Log.INFO, TAG, "package loaded: " + param.getPackageName());
        if (!TARGET_PKG.equals(param.getPackageName())) return;

        Class<?> devicePolicy;
        try {
            devicePolicy = Class.forName(DEVICE_POLICY_CLASS, false, param.getDefaultClassLoader());
        } catch (Throwable e) {
            log(Log.ERROR, TAG, "DevicePolicy class not found", e);
            return;
        }

        hookBoolean(devicePolicy, "requiresDeviceManagement", false);
        hookBoolean(devicePolicy, "isPolicyApplied", true);
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!TARGET_PKG.equals(param.getPackageName())) return;
        log(Log.INFO, TAG, "package ready for " + param.getPackageName());
    }

    private void hookBoolean(Class<?> owner, String methodName, boolean forced) {
        Method m;
        try {
            m = owner.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            log(Log.WARN, TAG, owner.getName() + "." + methodName + "() not found", e);
            return;
        }
        hook(m).intercept(chain -> {
            log(Log.INFO, TAG, methodName + "() called on " + chain.getThisObject() + " -> " + forced);
            return forced;
        });
        log(Log.INFO, TAG, "hooked " + owner.getSimpleName() + "." + methodName + " -> " + forced);
    }
}
