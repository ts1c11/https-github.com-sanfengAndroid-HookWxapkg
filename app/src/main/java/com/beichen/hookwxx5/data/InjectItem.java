package com.beichen.hookwxx5.data;

import android.text.TextUtils;

import org.json.JSONObject;

public class InjectItem {
    private String gameName;     // 小游戏名字
    private String displayName;  // 显示名字,供自己分别脚本,在小游戏注入窗口显示
    private String node;         // 对脚本的备注
    private String appId;        // 小游戏appId,可根据它来判断游戏,可以为空
    private String jsCode;       // 待注入的jsCode
    private boolean available;   // 当前是否可用,在小游戏注入窗口是否显示,避免过多显示影响UI

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getJsCode() {
        return jsCode;
    }

    public void setJsCode(String jsCode) {
        this.jsCode = jsCode;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }


    public static final String JSON_INJECT_GAME_NAME = "game_name";             // 对应json的key
    public static final String JSON_INJECT_DISPLAY_NAME = "display_name";       // 对应json的key
    public static final String JSON_INJECT_NODE = "node";                       // 对应json的key
    public static final String JSON_INJECT_APPID = "appId";                     // 对应json的key
    public static final String JSON_INJECT_JS_CODE = "javascripty_code";        // 对应json的key
    public static final String JSON_INJECT_AVAILABLE = "available";             // 对应json的key
    public static final String JSON_INJECT_KEY_NAME = "game";                   // 每个游戏保存的json格式对应键的名字
    public static final String JSON_INJECT_VALUE_NAME = "data";                 // 每个游戏保存的json格式对应值得名字

    public InjectItem(){}

    public InjectItem(String gameName, String displayName, String node, String appId, String jsCode, boolean available){
        this.gameName = gameName;
        this.displayName = displayName;
        this.node = node;
        this.appId = appId;
        this.jsCode = jsCode;
        this.available = available;
    }

    public static JSONObject InjectItem2JSON(InjectItem injectItem){
        JSONObject jsonObject = new JSONObject();
        try {
            String tmp;
            jsonObject.put(JSON_INJECT_GAME_NAME, injectItem.gameName);
            jsonObject.put(JSON_INJECT_DISPLAY_NAME, injectItem.displayName);
            tmp = TextUtils.isEmpty(injectItem.node) ? "" : injectItem.node;
            jsonObject.put(JSON_INJECT_NODE, tmp);
            tmp = TextUtils.isEmpty(injectItem.appId) ? "" : injectItem.appId;
            jsonObject.put(JSON_INJECT_APPID, tmp);
            jsonObject.put(JSON_INJECT_JS_CODE, injectItem.jsCode);
            jsonObject.put(JSON_INJECT_AVAILABLE, injectItem.available);
        }catch (Exception e){
            return null;
        }
        return jsonObject;
    }

    public static InjectItem JSON2InjectItem(JSONObject jsonObject){
        InjectItem injectItem = new InjectItem();
        try {
            injectItem.gameName = jsonObject.getString(JSON_INJECT_GAME_NAME);
            injectItem.displayName = jsonObject.getString(JSON_INJECT_DISPLAY_NAME);
            injectItem.node = jsonObject.getString(JSON_INJECT_NODE);
            injectItem.appId = jsonObject.getString(JSON_INJECT_APPID);
            injectItem.jsCode = jsonObject.getString(JSON_INJECT_JS_CODE);
            injectItem.available = jsonObject.getBoolean(JSON_INJECT_AVAILABLE);
        }catch (Exception e){
            return null;
        }
        return injectItem;
    }

    public static InjectItem copy(InjectItem item){
        InjectItem item1 = new InjectItem(item.getGameName(), item.getDisplayName(), item.getNode(), item.getAppId(), item.getJsCode(), item.available);
        return item1;
    }
}
