package com.beichen.hookwxx5.plugin;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.data.ReplaceItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static final String JS_REPLACE_FILE_NAME = "beichen_hookwxapkg";
    public static final String JS_INJECT_FILE_NAME = "beichen_inject";

    public static boolean saveReplaceSettings(List<ReplaceItem> list){
        boolean ret = false;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "未正确挂载SD卡,无法写入数据");
            return ret;
        }
        File file = new File(Environment.getExternalStorageDirectory(), JS_REPLACE_FILE_NAME);
        FileWriter fw = null;
        try {
            if (file.isDirectory()) file.delete();
            if (!file.exists()) file.createNewFile();
            if (list.size() == 0){
                file.delete();
                return true;
            }
            fw = new FileWriter(file);      // 这里每次覆盖写入即可
            for (ReplaceItem replaceItem : list){
                JSONObject obj = ReplaceItem.ReplaceItem2JSON(replaceItem);
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

    public static List<ReplaceItem> readReplaceSettings(){
        List<ReplaceItem> list = new ArrayList<>();
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读取数据");
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory(), JS_REPLACE_FILE_NAME);
        if (!file.exists()) {
            Log.d(TAG, JS_REPLACE_FILE_NAME + " 文件不存在没有数据");
            return null;
        }
        if (!file.isFile()) {
            Log.d(TAG, JS_REPLACE_FILE_NAME + " 不是文件,无法读取数据");
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
                    list.add(ReplaceItem.JSON2ReplaceItem(jsonObject));
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean saveInjectSettings(LinkedHashMap<String, List<InjectItem>> map){
        boolean ret = false;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读写数据");
            return ret;
        }
        File file = new File(Environment.getExternalStorageDirectory(), JS_INJECT_FILE_NAME);
        if (map.size() <= 0){
            Log.e(TAG, "删除所有数据");
            file.delete();
            return true;
        }

        FileWriter fw = null;
        String[] itemArr = new String[map.size()];
        try {
            fw = new FileWriter(file);
            int index = 0;
            for (Map.Entry<String, List<InjectItem>> entry : map.entrySet()){
                String game = entry.getKey();
                List<InjectItem> list = entry.getValue();
                JSONObject[] arr = new JSONObject[list.size()];
                for (int i = 0; i < list.size(); i++){
                    arr[i] = InjectItem.InjectItem2JSON(list.get(i));
                }
                JSONArray jsArr = new JSONArray(arr);
                JSONObject object = new JSONObject();
                object.put(InjectItem.JSON_INJECT_KEY_NAME, game);
                object.put(InjectItem.JSON_INJECT_VALUE_NAME, jsArr);
                itemArr[index] = object.toString();
                index++;
            }
            fw.write("[");
            for (int i = 0; i < itemArr.length; i++){       // 写入时构造成json数组格式,方便读取
                Log.e(TAG, itemArr[i]);
                if (i == itemArr.length - 1){
                    fw.write(itemArr[i]);
                    break;
                }
                fw.write(itemArr[i]);
                fw.write(",");
            }
            fw.write("]");
            fw.flush();
            fw.close();
            ret = true;
            Log.e(TAG, "保存数据成功");
        }catch (Exception e){
            Log.e(TAG, "保存数据错误", e);
        }finally {
            try {
                if (fw != null) fw.close();
            }catch (Exception e){
                Log.e(TAG, "IO error", e);
            }
        }
        return ret;
    }

    public static LinkedHashMap<String, List<InjectItem>> readInjectSettings(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读取数据");
            return null;
        }
        LinkedHashMap<String, List<InjectItem>> map = new LinkedHashMap<>();
        File file = new File(Environment.getExternalStorageDirectory(), JS_INJECT_FILE_NAME);
        if (!file.exists()){
            Log.e(TAG, "当前还没有数据");
            return null;
        }
        BufferedReader br = null;
        FileInputStream fis = null;
        try {
            String line = "";
            StringBuilder sb = new StringBuilder();
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++){
                List<InjectItem> list = new ArrayList<>();
                JSONArray arr = jsonArray.getJSONObject(i).getJSONArray(InjectItem.JSON_INJECT_VALUE_NAME);
                String game = jsonArray.getJSONObject(i).getString(InjectItem.JSON_INJECT_KEY_NAME);
                for (int j = 0; j < arr.length(); j++){
                    InjectItem item = InjectItem.JSON2InjectItem(arr.getJSONObject(j));
                    list.add(item);
                }
                map.put(game, list);
            }
        }catch (Exception e){
            Log.e(TAG, "读取配置文件错误", e);
            return null;
        }finally {
            try {
                if (fis != null) fis.close();
                if (br != null) br.close();
            }catch (Exception e){
                Log.e(TAG, "读取配置文件IO错误", e);
            }
        }
        return map;
    }

    public static String LinkedHashMapIndex2Key(LinkedHashMap<String, List<InjectItem>> map, int index){
        int i = 0;
        for (String key : map.keySet()){
            if (index == i){
                return key;
            }
            i++;
        }
        return null;
    }

    public static List<InjectItem> LinkedHashMapIndex2Value(LinkedHashMap<String, List<InjectItem>> map, int index){
        int i = 0;
        for (String key : map.keySet()){
            if (index == i){
                return map.get(key);
            }
            i++;
        }
        return null;
    }
}
