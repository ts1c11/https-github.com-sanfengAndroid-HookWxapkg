package com.beichen.hookwxx5.plugin;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.beichen.hookwxx5.Item;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    public static final String JS_NAME = "js_name";
    public static final String JS_RULE = "js_rule";
    public static final String JS_ORI = "js_ori";
    public static final String JS_MOD = "js_mod";
    public static final String FILE_SETTING_NAME = "beichen_hookwxapkg";

    public static Item JSON2Item(JSONObject object) {
        Item item = new Item();
        try {
            item.fileName = object.getString(Utils.JS_NAME);
            item.rule = object.getString(Utils.JS_RULE);
            item.ori = object.getString(Utils.JS_ORI);
            item.mod = object.getString(Utils.JS_MOD);
        }catch (Exception e){
            return null;
        }
        return item;
    }
    public static JSONObject Item2JSON(Item item){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JS_NAME, item.fileName);
            String tmp = TextUtils.isEmpty(item.rule) ? "" : item.rule;
            jsonObject.put(JS_RULE, item.rule);
            jsonObject.put(JS_ORI, item.ori);
            tmp = TextUtils.isEmpty(item.mod) ? "": item.mod;
            jsonObject.put(JS_MOD, item.mod);
        }catch (Exception e){
            return null;
        }
        return jsonObject;
    }

    public static boolean saveSettings(List<Item> list){
        boolean ret = false;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "未正确挂载SD卡,无法写入数据");
            return ret;
        }
        File file = new File(Environment.getExternalStorageDirectory(), FILE_SETTING_NAME);
        FileWriter fw = null;
        try {
            if (file.isDirectory()) file.delete();
            if (!file.exists()) file.createNewFile();
            if (list.size() == 0){
                file.delete();
                return true;
            }
            fw = new FileWriter(file);      // 这里每次覆盖写入即可
            for (Item item : list){
                JSONObject obj = Item2JSON(item);
                if (obj != null){
                    fw.write(obj.toString());
                    fw.write("\n");
                }
            }
            ret = true;
        }catch (Exception e){
            Log.e(TAG, "写入数据出错", e);
        }finally {
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                Log.e(TAG, "IO error", e);
            }
        }
        return ret;
    }

    public static List<Item> readSettings(){
        List<Item> list = new ArrayList<>();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读取数据");
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory(), FILE_SETTING_NAME);
        if (!file.exists()) {
            Log.d(TAG, FILE_SETTING_NAME + " 文件不存在没有数据");
            return null;
        }
        if (!file.isFile()) {
            Log.d(TAG, FILE_SETTING_NAME + " 不是文件,无法读取数据");
            return null;
        }
        BufferedReader br = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            while ((line = br.readLine()) != null){
                if (!TextUtils.isEmpty(line)){
                    JSONObject jsonObject = new JSONObject(line);
                    list.add(JSON2Item(jsonObject));
                }
            }
        }catch (Exception e){
            Log.e(TAG, "读取配置文件出错", e);

        }finally {
            try {
                if (br != null) br.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.e(TAG, "读取配置文件出错", e);
            }
        }
        return list;
    }

}
