package com.example.xplearn;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.iinti.sekiro3.business.api.fastjson.JSONObject;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class Store {
    // 实例化 People的构造函数实例
    static Object peopleConstructorInstance;

    // 实例化 People的方法实例
    static Object peopleMethodInstance;

    public static String getKeyUUID() {
        return keyUUID;
    }

    public static void setKeyUUID(String keyUUID) {
        Store.keyUUID = keyUUID;
    }

    // 定义一个变量
    static String keyUUID = null;

    //定义一个Map集合  线程安全
    static Map<String, Map<String, Object>> queryResult = Maps.newConcurrentMap();


    public static JSONObject callRun(String str) {
        JSONObject jsonObject = new JSONObject();

        if (peopleMethodInstance != null) {
            String uuid = UUID.randomUUID().toString().replace("-", "'");
            setKeyUUID(uuid);
            HashMap<String, Object> map = new HashMap<>();
            queryResult.put(uuid, map);

            XposedHelpers.callMethod(peopleMethodInstance, "run", str);

            //使用锁机制 处理请求耗时的操作
            try {
                synchronized (uuid) {
                    uuid.wait(1000);
                    Map<String, Object> remove = queryResult.remove(uuid);
                    jsonObject = new JSONObject(remove);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                jsonObject.put("msg", "timeOut");
            } finally {
                setKeyUUID(null);
            }

        } else {
            jsonObject.put("msg", "PeopleMethodInstance = null");
        }

        return jsonObject;
    }


    // hook People实例化后的 run 里面的info方法
    public static boolean hookLog(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod("com.hexl.lessontest.utils.LogUtils", classLoader, "info", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                String arg0 = (String) param.args[0];
                String uuid = getKeyUUID();
                if (uuid != null){
                    Map<String, Object> map = queryResult.get(uuid);
                    if (arg0.contains("People run by") && map != null){
                        map.put("run param", arg0);
                        synchronized (uuid){
                            uuid.notify();
                        }
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        return true;
    }


}
