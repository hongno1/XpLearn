package com.example.xplearn;

import cn.iinti.sekiro3.business.api.fastjson.JSONObject;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class Store {
    // 实例化 People的构造函数实例
    static Object peopleConstructorInstance;

    // 实例化 People的方法实例
    static Object peopleMethodInstance;

//    public static JSONObject callRun(String str) {
//        return null;
//    }

    public static boolean callRun(String str){
        if (peopleMethodInstance != null){
            return (Boolean) XposedHelpers.callMethod(peopleMethodInstance, "run", str);
        }else{
            return false;
        }

    }


    public static boolean hookLog(ClassLoader classLoader){
        XposedHelpers.findAndHookMethod("com.hexl.lessontest.utils.LogUtils", classLoader, "info", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        return true;
    }


}
