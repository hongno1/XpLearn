package com.example.xplearn;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;

public class HookMain implements IXposedHookLoadPackage {
    private static final String TAG = "XpTest";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 输出当前的包名
        Log.i(TAG, "packageName: " + lpparam.packageName);

        //native包
        String nativeLibraryDir = lpparam.appInfo.nativeLibraryDir;
        //加载类
        ClassLoader classLoader = lpparam.classLoader;
        // 进程名称
        String processName = lpparam.processName;
        //是否是第一个app
        boolean isFirstApplication = lpparam.isFirstApplication;

        Log.i(TAG, "handleLoadPackage: nativeLibraryDir=" + nativeLibraryDir);
        Log.i(TAG, "handleLoadPackage: classLoader=" + classLoader);
        Log.i(TAG, "handleLoadPackage: processName=" + processName);
        Log.i(TAG, "handleLoadPackage: isFirstApplication=" + isFirstApplication);

        Class<?> People = XposedHelpers.findClassIfExists("com.hexl.lessontest.logic.People", lpparam.classLoader);
        Field hello = XposedHelpers.findField(People, "hello");
        // 如果是私有的属性需要设置成true
        hello.setAccessible(true);
        //看是否是字符串 如果是字符串则传null   否则传入对象
        String world = (String) hello.get(null);
        Log.i(TAG, "handleLoadPackage: hello=" + world);

        Method run = XposedHelpers.findMethodExact(People, "run", String.class);
        Boolean runResult = (Boolean) run.invoke(XposedHelpers.newInstance(People, 1), "---dddddd");

        Log.i(TAG, "handleLoadPackage: runResult=" + runResult);

        XposedHelpers.findAndHookMethod(People, "run", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                String args0 = (String) param.args[0];
                Object obj = param.thisObject;
                Log.i(TAG, "beforeHookedMethod: args0=" + args0 + "-----obj=" + obj);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Boolean result = (Boolean) param.getResult();
                Log.i(TAG, "afterHookedMethod: result=" + result);

                param.setResult(false);
                Log.i(TAG, "afterHookedMethod: xiugaiResut=" + param.getResult());

            }
        });


    }
}
