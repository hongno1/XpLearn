package com.example.xplearn;

import cn.iinti.sekiro3.business.api.fastjson.JSONObject;
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


}
