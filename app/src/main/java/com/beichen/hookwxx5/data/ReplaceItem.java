package com.beichen.hookwxx5.data;

import android.text.TextUtils;

import org.json.JSONObject;

public class ReplaceItem {
    public String fileName;
    public String rule;
    public String ori;
    public String mod;

    public static final String JS_NAME = "js_name";
    public static final String JS_RULE = "js_rule";
    public static final String JS_ORI = "js_ori";
    public static final String JS_MOD = "js_mod";

    public ReplaceItem(){}
    public ReplaceItem(String fileName, String rule, String ori, String mod){
        this.fileName = fileName;
        this.rule = rule;
        this.ori = ori;
        this.mod = mod;
    }

    public static ReplaceItem JSON2ReplaceItem(JSONObject object) {
        ReplaceItem replaceItem = new ReplaceItem();
        try {
            replaceItem.fileName = object.getString(JS_NAME);
            replaceItem.rule = object.getString(JS_RULE);
            replaceItem.ori = object.getString(JS_ORI);
            replaceItem.mod = object.getString(JS_MOD);
        }catch (Exception e){
            return null;
        }
        return replaceItem;
    }
    public static JSONObject ReplaceItem2JSON(ReplaceItem replaceItem){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JS_NAME, replaceItem.fileName);
            String tmp = TextUtils.isEmpty(replaceItem.rule) ? "" : replaceItem.rule;
            jsonObject.put(JS_RULE, replaceItem.rule);
            jsonObject.put(JS_ORI, replaceItem.ori);
            tmp = TextUtils.isEmpty(replaceItem.mod) ? "": replaceItem.mod;
            jsonObject.put(JS_MOD, replaceItem.mod);
        }catch (Exception e){
            return null;
        }
        return jsonObject;
    }

}
