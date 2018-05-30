package com.beichen.hookwxx5.data;

import android.text.TextUtils;

import org.json.JSONObject;

public class InjectItem extends BaseItem{
    private String displayName;  // 显示名字,供自己分别脚本,在小游戏注入窗口显示
    private String node;         // 对脚本的备注
    private String jsCode;       // 待注入的jsCode


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

    public String getJsCode() {
        return jsCode;
    }

    public void setJsCode(String jsCode) {
        this.jsCode = jsCode;
    }

    private static final String JSON_INJECT_GAME_NAME = "game_name";             // 对应json的key
    private static final String JSON_INJECT_DISPLAY_NAME = "display_name";       // 对应json的key
    private static final String JSON_INJECT_NODE = "node";                       // 对应json的key
    private static final String JSON_INJECT_APPID = "appId";                     // 对应json的key
    private static final String JSON_INJECT_JS_CODE = "javascripty_code";        // 对应json的key
    private static final String JSON_INJECT_AVAILABLE = "available";             // 对应json的key


    public InjectItem(){
        setProfile("beichen_inject");
    }

    public InjectItem(String gameName, String displayName, String node, String appId, String jsCode, boolean available){
        this.gameName = gameName;
        this.displayName = displayName;
        this.node = node;
        this.appId = appId;
        this.jsCode = jsCode;
        this.available = available;
        setProfile("beichen_inject");
    }

    @Override
    public JSONObject item2Json() {
        JSONObject jsonObject = new JSONObject();
        try {
            String tmp;
            jsonObject.put(JSON_INJECT_GAME_NAME, this.getGameName());
            jsonObject.put(JSON_INJECT_DISPLAY_NAME, this.getDisplayName());
            tmp = TextUtils.isEmpty(this.getNode()) ? "" : this.getNode();
            jsonObject.put(JSON_INJECT_NODE, tmp);
            tmp = TextUtils.isEmpty(this.getAppId()) ? "" : this.getAppId();
            jsonObject.put(JSON_INJECT_APPID, tmp);
            jsonObject.put(JSON_INJECT_JS_CODE, this.getJsCode());
            jsonObject.put(JSON_INJECT_AVAILABLE, this.isAvailable());
        }catch (Exception e){
            return null;
        }
        return jsonObject;
    }

    @Override
    public BaseItem json2Item(JSONObject json) {
        InjectItem injectItem = new InjectItem();
        try {
            injectItem.setGameName(json.optString(JSON_INJECT_GAME_NAME));
            injectItem.setDisplayName(json.optString(JSON_INJECT_DISPLAY_NAME));
            injectItem.setNode(json.optString(JSON_INJECT_NODE));
            injectItem.setAppId(json.optString(JSON_INJECT_APPID));
            injectItem.setJsCode(json.optString(JSON_INJECT_JS_CODE));
            injectItem.setAvailable(json.optBoolean(JSON_INJECT_AVAILABLE));
        }catch (Exception e){
            return null;
        }
        return injectItem;
    }

    @Override
    public BaseItem copy() {
        return new InjectItem(this.getGameName(), this.getDisplayName(), this.getNode(), this.getAppId(), this.getJsCode(), this.available);
    }
}
