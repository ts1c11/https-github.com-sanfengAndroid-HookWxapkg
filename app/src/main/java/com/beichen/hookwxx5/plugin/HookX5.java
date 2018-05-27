package com.beichen.hookwxx5.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.data.ReplaceItem;
import com.beichen.hookwxx5.widget.ChoiceDialog;
import com.beichen.hookwxx5.widget.MyDialog;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookX5 implements IXposedHookLoadPackage, ChoicesCallback {
    private final static String TAG = "beichen";
    private final static String INJECT_TAG = "beichen.Inject";
    private final static String JS_TAG = "beichen.jsLog";
    private final static String WX_TAG = "beichen.wxLog";
    public static Object ibvObj = null;                             // 注入脚本时需要的对象
    private XC_LoadPackage.LoadPackageParam mLoadPackageParam;
    private static List<ReplaceItem> modifyList;                           // 保存所有脚本替换数据
    private static List<InjectItem> injectItemList = null;
    private static String gameName;
    private static String appId;


    private XC_MethodHook x5WebViewCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
            switch (name){
                case "com.tencent.smtt.sdk.WebView.loadUrl":
                    if (param.args.length == 1){
                        Log.e(TAG, "X5 Hook loadUrl(String)方法之前", new Exception());
                        String url = (String) param.args[0];
                        Log.e(TAG, "loadUrl: " + url);
                    } else if (param.args.length == 2){
                        Log.e(TAG, "X5 loadUrl(String, Map): " + (String) param.args[0]);
                    }
                    break;
                case "com.tencent.smtt.sdk.WebView.evaluateJavascript":
                    Log.e(TAG, "evaluateJavascript 堆栈", new Exception());
                    String js = (String) param.args[0];
                    ValueCallback callback = (ValueCallback) param.args[1];
                    Log.e(TAG, "evaluateJavascript JS : " + js);
                    break;
                case "com.tencent.smtt.sdk.WebView.loadData":
                    Log.e(TAG, "X5 Hook loadData(String, String, String) 方法之前", new Exception());
                    String str1 = (String) param.args[0];
                    String str2 = (String) param.args[1];
                    String str3 = (String) param.args[2];
                    Log.e(TAG, "data=" + str1 + " mineType=" + str2 + " encoding=" + str3);
                    break;
                case "com.tencent.smtt.sdk.WebView.loadDataWithBaseURL":
                    //String baseUrl, String data, String mimeType, String encoding, String historyUrl
                    String baseUrl = (String) param.args[0];
                    String data = (String) param.args[1];
                    String mimeType = (String) param.args[2];
                    String encoding = (String) param.args[3];
                    String historyUrl = (String) param.args[4];
                    Log.e(TAG, "loadDataWithBaseURL 堆栈: ", new Exception());
                    Log.e(TAG, "loadDataWithBaseURL: baseUrl=" + baseUrl + " data=" + data + " mimeType=" + mimeType + " encoding=" + encoding + " historyUrl=" + historyUrl);
                    break;
            }
        }
    };

    private XC_MethodHook appbrandCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

            switch (param.method.getDeclaringClass().getName() + "." + param.method.getName()){
                case "com.tencent.mm.plugin.appbrand.appcache.ap.a":
                    String name = (String) param.args[1];
                    //Log.e(TAG, "ap.a 堆栈:  " + name, new Exception());
                    break;
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            Log.e(TAG, "afterHookedMethod: " + param.method.getDeclaringClass().getName() + "." + param.method.getName());
            String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
            switch (name){
                case "com.tencent.mm.plugin.appbrand.appcache.ap.a":
                    String name1 = (String) param.args[1];
                    String s = (String) param.getResult();
                    Log.e(TAG, "读取js脚本, 脚本名: " + name1 + " 脚本内容: " + s);
                    int i = 0;
                    if (modifyList != null && modifyList.size() > 0){
                        for (ReplaceItem replaceItem : modifyList){
                            if (replaceItem.fileName.equals(name1)){
                                if (!TextUtils.isEmpty(replaceItem.rule)){
                                    if (!s.contains(replaceItem.rule)){    // 这里的规则是包含关系,需要确保在文件中的唯一性
                                        i++;
                                        continue;
                                    }
                                }
                                // 接下来是替换
                                if (s.contains(replaceItem.ori)){
                                    s = s.replace(replaceItem.ori, replaceItem.mod);
                                    Log.d(TAG + " 脚本替换", "replace loaction " + i + " success!");
                                }else {
                                    Log.e(TAG + " 脚本替换", "replace loaction " + i + " fail, please confirm!");
                                }
                            }
                            i++;
                        }
                    }

//                     attach 100%
//                     for(var d=i.boxes,h=a+.5*InitMark.stageOffHeight,u=0;8>u;u++)if(d[u+1]>0&&t["hitImg"+u].hitTestPoint(o,h)){this._mark=u+1;break}
//                     for(var d=i.boxes,u=0;u<8;u++)if(d[u+1]>0){this._mark=u+1;break}
//
//                     guess 100%
//                     e["island"+n].initIslandView(i)}
//                     e["island"+n].initIslandView(i)}var tar=0;if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[0].crowns){tar=1;}else if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[1].crowns){tar=2;}else{tar=3;}console.log("猜金币,选择: "+tar);new TextPop("选择: "+tar);
//
//                     video ad
//                     var t=wx.createRewardedVideoAd({adUnitId:e});t.load().then(function(){return t.show()}).catch(function(e){return console.log(e.errMsg)}),t.onClose(function(e){console.log("onClose:",e),n&&n(),t.offClose()}),t.onError(function(e){console.log("error:",e),o&&o(),t.offError()})
//                     console.log("onClose:", e);n&&n();
//
//                     hurt 100/200
//                     e.attackTitan=function(t,n,i,a,o,r){
//                     e.attackTitan=function(t,n,i,a,o,r){if(a){i=200;}else{i=100;}
                    param.setResult(s);
                    break;
            }

        }
    };


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")){
            return;
        }
        mLoadPackageParam = loadPackageParam;
        // 这里直接在宿主线程中读取数据,数据多的情况下可能会造成ANR
        modifyList = Utils.readReplaceSettings();
        Log.d(TAG, "共获取修改项: " + modifyList.size() + "项");
        ClassLoader loader = loadPackageParam.classLoader;
        Log.e(TAG, "开始Hook微信, 当前进程名: " + loadPackageParam.processName);

        /*************** Hook x5内核框架层 *******************/
        // 因Android使用的是x5内核,因此最终都会执行到这个地方
        Class<?> webView = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class,x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class, Map.class, x5WebViewCallback);
        Class<?> valueCallBackClass = loader.loadClass("com.tencent.smtt.sdk.ab");
        XposedHelpers.findAndHookMethod(webView, "evaluateJavascript", String.class, valueCallBackClass, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadData", String.class, String.class, String.class, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, x5WebViewCallback);

        // 这个类是在读取JS文件为字符串
        Class<?> appbrandEClass = loader.loadClass("com.tencent.mm.plugin.appbrand.e");
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.appcache.ap", loader, "a", appbrandEClass, String.class, appbrandCallback);

        // hook 微信log和小程序log
        hookLog(loadPackageParam.classLoader);
        // hook 微信小游戏添加注入功能
        hookInject(loadPackageParam);
    }




    private XC_MethodHook injectCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
            switch (name){
                case "com.tencent.mm.plugin.appbrand.e$15.b":            // 只要在这里修改掉传入的AppBrandSysConfig即可打开调试功能
                    Class<?> appBrandSysConfigClass = param.method.getDeclaringClass().getClassLoader().loadClass("com.tencent.mm.plugin.appbrand.config.AppBrandSysConfig");
                    Class<?> wxaPkgWrappingInfoClass = param.method.getDeclaringClass().getClassLoader().loadClass("com.tencent.mm.plugin.appbrand.appcache.WxaPkgWrappingInfo");
                    Object arg0 = param.args[0];
                    if (arg0 == null){
                        Log.e(INJECT_TAG, "传入的AppBrandSysConfig为空");     // 正常情况下是不为空的
                        break;
                    }
                    Field iyIField = appBrandSysConfigClass.getDeclaredField("iyI");
                    iyIField.setAccessible(true);
                    boolean iyI = iyIField.getBoolean(arg0);
                    Field izvField = appBrandSysConfigClass.getField("izv");
                    izvField.setAccessible(true);
                    Object izvObj = izvField.get(arg0);
                    Field appIdField = appBrandSysConfigClass.getDeclaredField("appId");
                    appIdField.setAccessible(true);
                    appId = (String) appIdField.get(arg0);
                    Field etuField = appBrandSysConfigClass.getDeclaredField("etu");
                    etuField.setAccessible(true);
                    gameName = (String) etuField.get(arg0);
                    Field iqsField = wxaPkgWrappingInfoClass.getDeclaredField("iqs");
                    iqsField.setAccessible(true);
                    int iqs = iqsField.getInt(izvObj);
                    Log.d(INJECT_TAG, "原始AppBrandSysConfig.iyI=" + Boolean.toString(iyI) + " 原始WxaPkgWrappingInfo.iqs=" + iqs + " 游戏名字: " + gameName + " appId: " + appId);
                    // 读取之前需要获取小游戏名字和appId
                    readInjectSetting();
                    break;
                case "com.tencent.mm.plugin.appbrand.menu.MenuDelegate_EnableDebug.a":
                    hookAppBrandMenu(param, 3, "开启/关闭调试");
                    break;
                case "com.tencent.mm.sdk.a.b.chT":       // 这个函数在小游戏菜单中影响了 "appId: xxxxxx", "显示调试信息", "离开" 共三个菜单,而在其它地方调用也比较多,因此需要过滤下
                    Throwable throwable = new Throwable();
                    StackTraceElement[] elements = throwable.getStackTrace();
                    if (elements.length > 4){
                        String s = elements[3].getClassName() + "." + elements[3].getMethodName();
                        switch (s){
                            case "com.tencent.mm.plugin.appbrand.menu.j.a":        // 显示调试信息
                            case "com.tencent.mm.plugin.appbrand.menu.c.a":        // 显示 appId
                                // case "com.tencent.mm.plugin.appbrand.menu.g.a":        // 离开菜单
                            case "com.tencent.mm.plugin.appbrand.page.p.a":
                                Log.e(INJECT_TAG, "开启小游戏 appId 菜单项");
                                param.setResult(true);
                                break;
                        }
                    }
                    break;
                case "com.tencent.mm.plugin.appbrand.menu.h.a":
                    hookAppBrandMenu(param, 1, "注入脚本");
                    break;
                default:
                    break;
            }

        }
    };

    private void hookAppBrandMenu(XC_MethodHook.MethodHookParam param, int id, String name) throws Throwable{
        Log.d(INJECT_TAG, "Hook 小程序菜单");
        Class<?> nClass = param.method.getDeclaringClass().getClassLoader().loadClass("com.tencent.mm.ui.base.n");
        Method fMethod = nClass.getDeclaredMethod("f", int.class, CharSequence.class);
        fMethod.setAccessible(true);
        Object arg2 = param.args[2];
        fMethod.invoke(arg2, id, name);
        param.setResult(null);
    }

    /**
     *  修改微信小游戏的功能菜单,主要将"转发"功能替换为脚本注入,开启小游戏调试功能
     *
     * @param loadPackageParam
     * @throws ClassNotFoundException
     */
    private void hookInject(XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
//        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.page.p$33", loadPackageParam.classLoader, "onClick", View.class, injectCallback);
        Class<?> appBrandSysConfigClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.config.AppBrandSysConfig");
//         直接修改会影响到游戏的分享功能
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.e$15", loadPackageParam.classLoader, "b", appBrandSysConfigClass, injectCallback);
        // Hook菜单开放appid显示
        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.a.b", loadPackageParam.classLoader, "chT", injectCallback);
        // Hook小游戏修改 "转发" 菜单
        Class<?> pClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.page.p");
        Class<?> lClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.menu.l");
        Class<?> nClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.ui.base.n");
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.menu.MenuDelegate_EnableDebug", loadPackageParam.classLoader, "a", Context.class, pClass, nClass, String.class, injectCallback);

        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.menu.h", loadPackageParam.classLoader, "a", Context.class, pClass, nClass, String.class, injectCallback);
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.menu.h", loadPackageParam.classLoader, "a", Context.class, pClass, String.class, lClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(INJECT_TAG, "修改原始 \"转发\" 菜单点击事件");
                Context context = (Context) param.args[0];
                param.setResult(null);
                ChoiceDialog.getInstance().show(context, HookX5.this);

            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.game.d", loadPackageParam.classLoader, "aaK", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e(INJECT_TAG, "d.aaK() 方法已执行,保存ibv对象, 便于后面js注入");
                ibvObj = param.getResult();
            }
        });
    }

    private XC_MethodHook logCallback = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            boolean log = false;
            Throwable ex = new Throwable();
            StackTraceElement[] elements = ex.getStackTrace();
            for (StackTraceElement element : elements){
                if (element.getClassName().contains("com.tencent.mm.plugin.appbrand")){
                    log = true;
                    break;
                }
            }
            if (!log){
                return;
            }
            int level = 0;
            String name = param.method.getName();
            String arg0 = (String) param.args[0];
            String arg1 = (String) param.args[1];
            Object[] arg2 = (Object[]) param.args[2];
            String format = arg2 == null ? arg1 : String.format(arg1, arg2);
            if (TextUtils.isEmpty(format)){
                format = "null";
            }
            switch (name){
                case "f":
                case "i":
                    level = 0;
                    break;
                case "d":
                case "v":
                case "k":
                case "l":
                    level = 1;
                    break;
                case "e":
                case "w":
                    level = 2;
                    break;
            }
            switch (level){
                case 0:
                    Log.i(WX_TAG + " " + arg0, format);
                    break;
                case 1:
                    Log.d(WX_TAG + " " + arg0, format);
                    break;
                case 2:
                    Log.e(WX_TAG + " " + arg0, format);
                    break;
            }
        }
    };


    public void hookLog(ClassLoader loader) throws Exception{
        Class<?> logClass = loader.loadClass("com.tencent.mm.sdk.platformtools.w");
        XposedHelpers.findAndHookMethod(logClass, "f", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "e", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "w", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "i", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "d", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "v", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "k", String.class, String.class, Object[].class, logCallback);
        XposedHelpers.findAndHookMethod(logClass, "l", String.class, String.class, Object[].class, logCallback);

        // 将小程序日志自定义转发到java
        Class<?> arg0Class = loader.loadClass("com.tencent.mm.plugin.appbrand.j");
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.jsapi.al", loader, "a", arg0Class, JSONObject.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                JSONObject jsonObjectArg1 = (JSONObject) param.args[1];
                int l = jsonObjectArg1.getInt("level");
                String logs = jsonObjectArg1.getString("logs");
                switch (l){
                    case 0:
                        Log.d(JS_TAG, logs);
                        break;
                    case 1:
                        Log.i(JS_TAG, logs);
                        break;
                    case 2:
                        Log.w(JS_TAG, logs);
                        break;
                    case 3:
                        Log.e(JS_TAG, logs);
                        break;
                }
            }
        });
    }

    @Override
    public void success(Context context, final String js) {
        // 这里注入js代码
        if (ibvObj == null){
            Log.e(INJECT_TAG, "当前环境错误,无法执行注入");
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(INJECT_TAG, "主进程开始注入代码: " + js);
                    Class<?> gClass = mLoadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.game.g");
                    Method evaluateJavascriptMethod = gClass.getDeclaredMethod("evaluateJavascript", String.class, ValueCallback.class);
                    evaluateJavascriptMethod.setAccessible(true);
                    evaluateJavascriptMethod.invoke(ibvObj, js, valueCallback);
                }catch (Exception e){
                    Log.e(INJECT_TAG, "注入错误", e);
                }
            }
        });
    }

    private ValueCallback<String> valueCallback = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            Log.e(INJECT_TAG, "注入结果: " + value);
        }
    };

    public void readInjectSetting(){
        new Thread(){
            @Override
            public void run() {
                injectItemList = new ArrayList<>();
                LinkedHashMap<String, List<InjectItem>> map = Utils.readInjectSettings();
                Log.d(INJECT_TAG, "开始读取 " + gameName + " 小游戏配置注入脚本");
                List<InjectItem> tmp = null;
                if (map != null && map.size() > 0){
                    // 我们优先考虑 appId 匹配再考虑游戏名字匹配
                    for (String key : map.keySet()){
                        List<InjectItem> list = map.get(key);
                        if (list != null && list.size() > 0){
                            if (list.get(0).getAppId().equals(appId)){
                                tmp = list;
                                break;
                            }
                        }
                    }
                    if (tmp == null){
                        if (map.containsKey(gameName)){
                            tmp = map.get(gameName);
                        }
                    }
                    if (tmp == null){
                        Log.e(INJECT_TAG, "当前游戏没有配置注入脚本");
                        return;
                    }
                    // 有数据还要踢出没有开启的数据, 这里复制 InjectItem 使其临时List被回收
                    for (InjectItem item : tmp){
                        if (item.isAvailable()){
                            injectItemList.add(InjectItem.copy(item));
                        }
                    }
                    if (injectItemList.size() > 0){ // 有数据则先设置数据,后面直接调用Dialog即可
                        ChoiceDialog.getInstance().setData(injectItemList);
                        Log.e(INJECT_TAG, "已将本小游戏: " + gameName + " appId: " + appId + " 注入脚本添加至Dialog中");
                    }
                }else {
                    Log.e(INJECT_TAG, "当前游戏没有配置注入脚本");
                }
            }
        }.run();
    }
}
