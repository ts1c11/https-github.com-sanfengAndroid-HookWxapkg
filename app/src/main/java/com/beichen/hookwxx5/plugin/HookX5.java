package com.beichen.hookwxx5.plugin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import com.beichen.hookwxx5.data.BaseItem;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.data.ReplaceItem;
import com.beichen.hookwxx5.widget.ChoiceDialog;

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
    private static List<ReplaceItem> replaceList = new ArrayList<>();                           // 保存所有脚本替换数据
    private static List<InjectItem> injectList = new ArrayList<>();
    private static String gameName;
    private static String appId;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")){
            return;
        }
        mLoadPackageParam = loadPackageParam;
        // 这里直接在宿主线程中读取数据,数据多的情况下可能会造成ANR
        ClassLoader loader = loadPackageParam.classLoader;
        Log.e(TAG, "开始Hook微信, 当前进程名: " + loadPackageParam.processName);
        // hook 微信X5内核
        hookX5Kernel(loadPackageParam);
        // hook 微信log和小程序log
        hookLog(loadPackageParam.classLoader);
        // hook 微信小游戏添加注入功能
        hookInject(loadPackageParam);
    }



    private void hookX5Kernel(XC_LoadPackage.LoadPackageParam loadPackageParam){
        XC_MethodHook x5WebViewCallback = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String name = param.method.getDeclaringClass().getName() + "." + param.method.getName();
                switch (name){
                    case "com.tencent.smtt.sdk.WebView.loadUrl":
                        if (param.args.length == 1){
                            Log.d(TAG, "X5 Hook loadUrl(String)方法之前", new Exception());
                            String url = (String) param.args[0];
                            Log.d(TAG, "loadUrl: " + url);
                        } else if (param.args.length == 2){
                            Log.d(TAG, "X5 loadUrl(String, Map): " + (String) param.args[0]);
                        }
                        break;
                    case "com.tencent.smtt.sdk.WebView.evaluateJavascript":
                        Log.d(TAG, "evaluateJavascript 堆栈", new Exception());
                        String js = (String) param.args[0];
                        ValueCallback callback = (ValueCallback) param.args[1];
                        Log.d(TAG, "evaluateJavascript JS : " + js);
                        break;
                    case "com.tencent.smtt.sdk.WebView.loadData":
                        Log.d(TAG, "X5 Hook loadData(String, String, String) 方法之前", new Exception());
                        String str1 = (String) param.args[0];
                        String str2 = (String) param.args[1];
                        String str3 = (String) param.args[2];
                        Log.d(TAG, "data=" + str1 + " mineType=" + str2 + " encoding=" + str3);
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
        /*************** Hook x5内核框架层 *******************/
        // 因Android使用的是x5内核,因此最终都会执行到这个地方
        Class<?> webView = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class,x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadUrl", String.class, Map.class, x5WebViewCallback);
        Class<?> valueCallBackClass = XposedHelpers.findClass("com.tencent.smtt.sdk.ab", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(webView, "evaluateJavascript", String.class, valueCallBackClass, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadData", String.class, String.class, String.class, x5WebViewCallback);
        XposedHelpers.findAndHookMethod(webView, "loadDataWithBaseURL", String.class, String.class, String.class, String.class, String.class, x5WebViewCallback);
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
                    initGameData(ReplaceItem.class);        // 这里为了保证能够替换脚本因此得运行在主线程中
                    if (replaceList.size() > 0){
                        Log.d(INJECT_TAG, "当前游戏共有 " + replaceList.size() + " 项脚本需要替换");
                    }
                    // 读取之前需要获取小游戏名字和appId
                    initInjectSetting();                    // 注入配置的读取不用运行在主线程中
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
                                Log.d(INJECT_TAG, "开启小游戏 appId 菜单项");
                                param.setResult(true);
                                break;
                        }
                    }
                    break;
                case "com.tencent.mm.plugin.appbrand.menu.h.a":
                    hookAppBrandMenu(param, 1, "注入脚本");
                    break;
                case "com.tencent.mm.plugin.appbrand.q.i.a":                // 脚本替换的地方
                    String fileName = (String) param.args[1];           // 这个函数在挂载 game.js脚本,可以在这里替换
                    String js = (String) param.args[2];
                    int i = 0;
                    if (replaceList != null && replaceList.size() > 0){
                        for (ReplaceItem replaceItem : replaceList){
                            if (replaceItem.getFileName().equals(fileName)){
                                Log.d(INJECT_TAG, "正在替换 " + replaceItem.getFileName() + " 脚本");
                                if (!TextUtils.isEmpty(replaceItem.getAppId())){
                                    if (!js.contains(replaceItem.getAppId())){    // 这里的规则是包含关系,需要确保在文件中的唯一性
                                        i++;
                                        continue;
                                    }
                                }
                                // 接下来是替换
                                if (js.contains(replaceItem.getOri())){
                                    js = js.replace(replaceItem.getOri(), replaceItem.getMod());
                                    Log.d(INJECT_TAG, "脚本替换: replace loaction " + i + " success!");
                                }else {
                                    Log.e(INJECT_TAG, "脚本替换: replace loaction " + i + " fail, please confirm!");
                                }
                            }
                            i++;
                        }
                    }
                    param.args[2] = js;
                    break;
                case "com.tencent.mm.plugin.appbrand.game.d.aaK":           // 获取WebView对象
                    Log.d(INJECT_TAG, "d.aaK() 方法已执行,保存ibv对象, 便于后面js注入");
                    // ibvObj = param.getResult();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     *  在微信小游戏菜单中添加菜单项
     * @param param         方法有关的参赛
     * @param id            菜单项对应的id,应该跟排序有关
     * @param name          菜单显示的名字
     * @throws Throwable
     */
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

        // 目前获取WebView实例的方法有多种,似乎每种都不同,但可能都是继承了一个基类
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.game.d", loadPackageParam.classLoader, "aaK", injectCallback);
        final Class<?> jClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.j");
        XposedHelpers.findAndHookConstructor(jClass, new XC_MethodHook() {
                   @Override
                   protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                       Log.d(INJECT_TAG, "Hook j类构造方法得到对象,方法名:" + param.method.getName());
                       Object obj = param.thisObject;
                       Field ibvField = jClass.getDeclaredField("ibv");
                       ibvField.setAccessible(true);
                       ibvObj = ibvField.get(obj);
                   }
        });

       // hook 游戏脚本修改
        Class<?> bClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.g.b");
        Class<?> aClass = loadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.q.i$a");
        XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.appbrand.q.i", loadPackageParam.classLoader, "a", bClass, String.class, String.class, aClass, injectCallback);
    }



    public void hookLog(ClassLoader loader) throws Exception{

        XC_MethodHook logCallback = new XC_MethodHook() {
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


    /**
     * @param context   上下文环境
     * @param js        获取到待注入的脚本
     */
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
                    Log.d(INJECT_TAG, "主进程开始注入代码: " + js);
                    Class<?> gClass = mLoadPackageParam.classLoader.loadClass("com.tencent.mm.plugin.appbrand.game.g");
                    Method evaluateJavascriptMethod = gClass.getDeclaredMethod("evaluateJavascript", String.class, ValueCallback.class);
                    evaluateJavascriptMethod.setAccessible(true);
                    evaluateJavascriptMethod.invoke(ibvObj, js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d(INJECT_TAG, "注入结果: " + value);
                        }
                    });
                }catch (Exception e){
                    Log.e(INJECT_TAG, "注入错误", e);
                }
            }
        });
    }


    /**
     * 读取脚本注入配置文件
     */
    public void initInjectSetting(){
        new Thread(){
            @Override
            public void run() {
               initGameData(InjectItem.class);
            }
        }.run();
    }

    private <T extends BaseItem> void  initGameData(Class<T> cls) {
        LinkedHashMap<String, List<T>> map = null;
        map = Utils.readSettings(cls);
        if (cls.isAssignableFrom(InjectItem.class)){            // 由于单个小程序进程只会调用一次,这里可以不用清理
            injectList.clear();
        }else if (cls.isAssignableFrom(ReplaceItem.class)){
            replaceList.clear();
        }
        if (map != null && map.size() > 0){
            List<T> tmp = null;
            for (String key : map.keySet()){
                List<T> list = map.get(key);
                if (list != null && list.size() > 0){
                    if (list.get(0).getAppId().equals(appId)){
                        tmp = list;
                        break;
                    }
                }
                if (tmp == null && map.containsKey(gameName)){                                  // 其次判断游戏名称
                    tmp = map.get(gameName);
                }
                if (tmp == null){
                    Log.e(INJECT_TAG, "当前游戏 " + gameName + " 没有配置注入或替换脚本");
                    return;
                }
                for (BaseItem item : tmp){                                                      // 过滤没有开启的脚本
                    if (item.isAvailable()){
                        if (item instanceof InjectItem){
                            injectList.add((InjectItem) item.copy());
                        }else if (item instanceof ReplaceItem){
                            replaceList.add((ReplaceItem) item.copy());
                        }
                    }
                }
                if (injectList.size() > 0){             // 多次调用没什么影响
                    ChoiceDialog.getInstance().setData(injectList);
                    Log.d(INJECT_TAG, "已将本小游戏: " + gameName + " appId: " + appId + " 注入脚本添加至Dialog中");
                }
            }
        }
    }
}
