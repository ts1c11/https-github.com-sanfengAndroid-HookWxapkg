package com.beichen.hookwxx5.data;

import android.text.TextUtils;

import org.json.JSONObject;

public class ReplaceItem extends BaseItem{
    private String fileName;         // 文件名
    private String ori;              // 替换前脚本
    private String mod;              // 替换后脚本

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOri() {
        return ori;
    }

    public void setOri(String ori) {
        this.ori = ori;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    private static final String JSON_GAME_NAME = "game_name";
    private static final String JSON_FILE_NAME = "file_name";
    private static final String JSON_APPID = "appId";
    private static final String JSON_AVALIABLE = "available";
    private static final String JSON_JS_ORI = "js_ori";
    private static final String JSON_JS_MOD = "js_mod";

    public ReplaceItem(){
        setProfile("beichen_replace");
    }
    public ReplaceItem(String gameName, String appId, String fileName, String ori, String mod, boolean available){
        this.gameName = gameName;
        this.fileName = fileName;
        this.appId = appId;
        this.ori = ori;
        this.mod = mod;
        this.available = available;
        setProfile("beichen_replace");
    }

    @Override
    public BaseItem copy() {
        return new ReplaceItem(this.getGameName(), this.getAppId(), this.getFileName(), this.getOri(), this.getMod(), this.isAvailable());
    }

    @Override
    public JSONObject item2Json() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_GAME_NAME, this.getGameName());
            jsonObject.put(JSON_FILE_NAME, this.getFileName());
            String tmp = TextUtils.isEmpty(this.getAppId()) ? "" : this.getAppId();
            jsonObject.put(JSON_APPID, tmp);
            jsonObject.put(JSON_JS_ORI, this.getOri());
            tmp = TextUtils.isEmpty(this.getMod()) ? "": this.getMod();
            jsonObject.put(JSON_JS_MOD, tmp);
            jsonObject.put(JSON_AVALIABLE, this.isAvailable());
        }catch (Exception e){
            return null;
        }
        return jsonObject;
    }

    @Override
    public BaseItem json2Item(JSONObject json) {
        ReplaceItem replaceItem = new ReplaceItem();
        try {
            replaceItem.setGameName(json.optString(JSON_GAME_NAME));
            replaceItem.setFileName(json.optString(JSON_FILE_NAME));
            replaceItem.setAppId(json.optString(JSON_APPID));
            replaceItem.setOri(json.optString(JSON_JS_ORI));
            replaceItem.setMod(json.optString(JSON_JS_MOD));
            replaceItem.setAvailable(json.optBoolean(JSON_AVALIABLE));
        }catch (Exception e){
            return null;
        }
        return replaceItem;
    }
}
