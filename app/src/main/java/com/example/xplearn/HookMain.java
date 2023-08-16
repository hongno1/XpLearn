package com.example.xplearn;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.xplearn.handler.TestHandler;
import com.example.xplearn.handler.TwoHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import cn.iinti.sekiro3.business.api.SekiroClient;
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

        //lsposed 相关api
//        test_learn_api(classLoader);

//        testPre();
        if (lpparam.processName.equals(lpparam.packageName)){

            showToast("i am coming....");
            connectServer();


            Class<?> aClass = XposedHelpers.findClassIfExists("com.hexl.lessontest.logic.People", lpparam.classLoader);
            if (aClass != null){
                XposedBridge.hookAllConstructors(aClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Store.peopleMethodInstance = param.thisObject;
                    }
                });
            }

        }
    }


    // toast日志输出
    private static void showToast(String msg){

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        }, 3000);

    }

    // 判断文件是否可读
    private static XSharedPreferences getPref(String path){
        XSharedPreferences pref = new XSharedPreferences(BuildConfig.APPLICATION_ID, path);
        return pref.getFile().canRead() ? pref : null;
    }

    // hook相关 XSharedPreferences的公告配置文件
    private String testPre(){
        XSharedPreferences sharedPreferences = getPref("TestSetting");
        Log.i(TAG, "testPre: share: " + sharedPreferences);
        if (sharedPreferences == null){
            Log.i(TAG, "testPre: null");
            return null;
        }else{
            sharedPreferences.reload();
            String web = sharedPreferences.getString("web", "");
            Log.i(TAG, "testPre: web=" + web);

            return web;
        }
    }

    // connect sekiro 方式方法
    private boolean connectServer(){

        String clientId = UUID.randomUUID().toString().replace("-", "");
        SekiroClient sekiroClient = new SekiroClient("test002", clientId, "81.69.37.203", 5612);

        // 开启异步线程实现 处理任务
        new Thread(new Runnable() {
            @Override
            public void run() {
                sekiroClient.setupSekiroRequestInitializer((sekiroRequest, handlerRegistry) -> {
                    //注册一个接口
                    handlerRegistry.registerSekiroHandler(new TestHandler());
                    handlerRegistry.registerSekiroHandler(new TwoHandler());
                }).start();
            }
        }).start();
        return true;
    }

    private void test_learn_api(ClassLoader classLoader) {
        Class<?> People = XposedHelpers.findClassIfExists("com.hexl.lessontest.logic.People", classLoader);
        Field hello = XposedHelpers.findField(People, "hello");
        // 如果是私有的属性需要设置成true
        hello.setAccessible(true);
        //看是否是字符串 如果是字符串则传null   否则传入对象
//        String world = (String) hello.get(null);
//        Log.i(TAG, "handleLoadPackage: hello=" + world);

//        Method run = XposedHelpers.findMethodExact(People, "run", String.class);
//        Boolean runResult = (Boolean) run.invoke(XposedHelpers.newInstance(People, 1), "---dddddd");
//        Log.i(TAG, "handleLoadPackage: runResult=" + runResult);

        // hook run方法  修改方法对应的值
        XposedHelpers.findAndHookMethod(People, "run", String.class, new XC_MethodHook(XCallback.PRIORITY_DEFAULT) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                String args0 = (String) param.args[0];
                Object obj = param.thisObject;
                Log.i(TAG, "beforeHookedMethod: args0=" + args0 + "-----obj=" + obj);

                param.args[0] = "hongqino1";
                //打印堆栈的日志
                Log.i(TAG, Log.getStackTraceString(new Throwable()));


            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Boolean result = (Boolean) param.getResult();
                Log.i(TAG, "afterHookedMethod: result=" + result);

                param.setResult(false);
                Log.i(TAG, "afterHookedMethod: updateResult=" + param.getResult());

            }
        });

        // hook 构造方法
        XposedHelpers.findAndHookConstructor("com.hexl.lessontest.logic.People", classLoader, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(TAG, "beforeHookedMethod: findAndHookConstructor");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object object = param.thisObject;

                Log.i(TAG, "afterHookedMethod: findAndHookConstructor" + object);
                // 将hook的构造函数 保存到Store的实例中去
                Store.peopleConstructorInstance = object;
            }
        });

        // XposedBridge

        //修改原来的方法执行返回结果
        XposedBridge.hookAllMethods(People, "speak", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Log.i(TAG, "replaceHookedMethod: " + Arrays.toString(param.args));
                if (param.args.length == 1) {
                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }
                return null;
            }
        });


        // XposedHelper的 get set的api

        Object peopleObj = XposedHelpers.newInstance(People, "fffff");
        XposedHelpers.setObjectField(peopleObj, "name", "hhhhh");
        XposedHelpers.setStaticBooleanField(People, "run", false);

        Object name = XposedHelpers.getObjectField(peopleObj, "name");
        boolean booleanField = XposedHelpers.getStaticBooleanField(People, "run");

        Log.i(TAG, "handleLoadPackage: name---" + name);
        Log.i(TAG, "handleLoadPackage: booleanField---" + booleanField);

        XposedHelpers.setAdditionalInstanceField(peopleObj, "chatgpt", "kkkkk");
        XposedHelpers.setAdditionalStaticField(peopleObj, "chat", "gpt");

        Object chat = XposedHelpers.getAdditionalStaticField(peopleObj, "chat");
        Object chatgpt = XposedHelpers.getAdditionalInstanceField(peopleObj, "chatgpt");

        Log.i(TAG, "handleLoadPackage: chat=" + chat);
        Log.i(TAG, "handleLoadPackage: chatgpt=" + chatgpt);
    }
}
