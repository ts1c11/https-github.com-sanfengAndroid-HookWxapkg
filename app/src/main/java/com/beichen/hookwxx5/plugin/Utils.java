package com.beichen.hookwxx5.plugin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.beichen.hookwxx5.data.BaseItem;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.data.ReplaceItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static <T extends BaseItem> boolean saveSettings(LinkedHashMap<String, List<T>> map){
        boolean ret = false;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读写数据");
            return ret;
        }
        // 如果 map 没有元素我们则无法知道其具体的类型
        if (map == null){
            Log.e(TAG, "写入的数据不能为空,这将导致无法知道具体类型");
            return ret;
        }
        File file = null;
        if (map.size() == 0){   // 没有数据的情况下可以尝试添加一个,看是否报错
            List<InjectItem> item = new ArrayList<>();
            try {
                map.put("test", (List<T>) item);
                map.clear();
                file = new File(Environment.getExternalStorageDirectory(), new InjectItem().getProfile());
                Log.d(TAG, "删除配置文件: " + file.getAbsolutePath());
                file.delete();
                return true;
            }catch (Exception e){
                // 报错则说明类型不匹配
                file = new File(Environment.getExternalStorageDirectory(), new ReplaceItem().getProfile());
                Log.d(TAG, "删除配置文件: " + file.getAbsolutePath());
                file.delete();
                return true;
            }
        }
        try {
            T tmp = map.get(Utils.LinkedHashMapIndex2Key(map, 0)).get(0);
            file = new File(Environment.getExternalStorageDirectory(), tmp.getClass().newInstance().getProfile());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, List<T>> entry : map.entrySet()){
                String game = entry.getKey();
                List<T> list = entry.getValue();
                JSONArray arr = new JSONArray();
                for (int i = 0; i < list.size(); i++){
                    arr.put(list.get(i).item2Json());
                }
                JSONObject object = new JSONObject();
                object.put(BaseItem.JSON_KEY_NAME, game);
                object.put(BaseItem.JSON_VALUE_NAME, arr);
                jsonArray.put(object);
            }
            fw.write(jsonArray.toString());
            fw.flush();
            fw.close();
            ret = true;
            Log.d(TAG, "保存数据成功");
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

    public static <T extends BaseItem> LinkedHashMap<String, List<T>> readSettings(Class<T> cls){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读取数据");
            return null;
        }
        LinkedHashMap<String, List<T>> map = new LinkedHashMap<>();
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory(), cls.newInstance().getProfile());
        } catch (Exception e) {
           return null;
        }
        if (!file.exists()){
            Log.d(TAG, "当前还没有数据");
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
                List<T> list = new ArrayList<>();
                String game = jsonArray.optJSONObject(i).optString(BaseItem.JSON_KEY_NAME);
                JSONArray arr = jsonArray.optJSONObject(i).optJSONArray(BaseItem.JSON_VALUE_NAME);
                T obj = cls.newInstance();
                for (int j = 0; j < arr.length(); j++){
                    T item = (T) obj.json2Item(arr.optJSONObject(j));
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

    public static <T extends BaseItem> String LinkedHashMapIndex2Key(LinkedHashMap<String, List<T>> map, int index){
        int i = 0;
        for (String key : map.keySet()){
            if (index == i){
                return key;
            }
            i++;
        }
        return null;
    }

    public static <T extends BaseItem> List<T> LinkedHashMapIndex2Value(LinkedHashMap<String, List<T>> map, int index){
        int i = 0;
        for (String key : map.keySet()){
            if (index == i){
                return map.get(key);
            }
            i++;
        }
        return null;
    }

    /** 获取微信版本号,用于多版本匹配
     * @param context
     * @return
     */
    public static String getWXVerName(Context context){
        try {
            return context.getPackageManager().getPackageInfo("com.tencent.mm", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveWXSettings(String name, String value){
        // 保存信息在 sd卡下beichen_settings文件中,为一个json字符串
        JSONObject jsonObject = readWxSettings();
        if (jsonObject == null){    // 为空可能没有配置文件或读取出错,都可以尝试写入
            jsonObject = new JSONObject();
        }
        try {
            jsonObject.put(name, value);
            saveWxSettings(jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "保存微信版本出错", e);
        }
    }

    public static String readWxSettings(String key){
        JSONObject jsonObject = readWxSettings();
        if (jsonObject == null){    // 为空可能没有配置文件或读取出错,都可以尝试写入
            jsonObject = new JSONObject();
        }
        return jsonObject.optString(key);
    }


    public static JSONObject readWxSettings(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读写数据");
            return null;
        }
        File file = new File(Environment.getExternalStorageDirectory(), "beichen_settings");
        if (!file.exists()){
            return new JSONObject();
        }
        if (file.exists() && !file.isFile()){
            file.delete();
            return new JSONObject();
        }
        BufferedReader br = null;
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            String line = "";
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            br.close();
        }catch (Exception e) {
            try {
                if (fis != null) fis.close();
                if (br != null) br.close();
            } catch (Exception e2) {
                Log.e(TAG, "读取配置文件 beichen_settings 失败", e2);
            }
        }

        if (sb.toString().isEmpty()){
            return null;
        }else {
            try {
                return new JSONObject(sb.toString());
            } catch (JSONException e) {
                Log.e(TAG, "读取配置文件 beichen_settings 失败", e);
            }
        }
        return null;
    }

    /** 写入配置文件,这里写入时覆盖的,因此json必须是完整的
     * @param object
     */
    public static void saveWxSettings(JSONObject object){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e(TAG, "没有正确挂载SD卡,无法读写数据");
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), "beichen_settings");
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(object.toString());
            fw.close();
        } catch (IOException e) {
            Log.e(TAG, "写入配置文件 beichen_settings 失败", e);
        }
    }
}
