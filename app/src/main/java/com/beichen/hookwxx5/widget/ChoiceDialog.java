package com.beichen.hookwxx5.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.plugin.ChoicesCallback;

import java.util.List;

public class ChoiceDialog {
    private static final ChoiceDialog ourInstance = new ChoiceDialog();
    private static List<InjectItem> list = null;
    private int position = 0;
    public static ChoiceDialog getInstance() {
        return ourInstance;
    }

    private ChoiceDialog() {
    }

    public void setData(List<InjectItem> list){
        this.list = list;
    }
    public void show(Context context, ChoicesCallback callback){
        this.show(context, list, callback);
    }

    /**
     * @param context   上下文环境
     * @param list      这里传入的list是都显示的,请在调用处过滤不显示数据
     * @param callback  选择注入时的回调
     */
    public void show(final Context context, final List<InjectItem> list, final ChoicesCallback callback){
        position = 0;
        if (list == null || list.size() < 1){
            Toast.makeText(context, "没有可选择脚本,请在主程序中添加", Toast.LENGTH_SHORT).show();
            Log.e("beichen.Inject", "当前没有可用注入脚本");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 3);
        final String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++){
            items[i] = list.get(i).getDisplayName();
        }
        builder.setTitle("请选择脚本")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callback != null && callback instanceof ChoicesCallback){
                            callback.success(context, list.get(position).getJsCode());
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create().show();
    }
}
