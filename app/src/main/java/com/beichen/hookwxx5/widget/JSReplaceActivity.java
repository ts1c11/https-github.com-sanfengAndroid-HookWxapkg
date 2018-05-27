package com.beichen.hookwxx5.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.data.ReplaceItem;
import com.beichen.hookwxx5.adapter.ReplaceItemAdapter;
import com.beichen.hookwxx5.plugin.Utils;

import java.util.ArrayList;
import java.util.List;

public class JSReplaceActivity extends Activity implements View.OnClickListener {
    private final String TAG = "beichen";
    private EditText etJSName, etJSRule, etJSOri, etJSMod;
    private ListView listView;
    private ReplaceItemAdapter replaceItemAdapter;
    private List<ReplaceItem> list;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if (msg.arg1 == 1){
                        Toast.makeText(JSReplaceActivity.this, "更新数据成功", Toast.LENGTH_SHORT).show();
                    }else if (msg.arg1 == 0){
                        Toast.makeText(JSReplaceActivity.this, "更新数据错误,请查看日志", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    replaceItemAdapter = new ReplaceItemAdapter(JSReplaceActivity.this, R.layout.rule_item, list, callBack);
                    listView.setAdapter(replaceItemAdapter);
                    listView.setOnItemLongClickListener(listener);
                    listView.setItemsCanFocus(true);
                    Toast.makeText(JSReplaceActivity.this, "初始化数据成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private DataCallBack callBack = new DataCallBack() {
        @Override
        public void dataChange() {
            // 数据改变后覆盖写入配置文件
            new Thread(){
                @Override
                public void run() {
                    boolean ret = Utils.saveReplaceSettings(list);
                    Message msg = new Message();
                    if (ret){
                        msg.arg1 = 1;
                    }else {
                        msg.arg1 = 0;
                    }
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                }
            }.run();
        }
    };

    private AdapterView.OnItemLongClickListener listener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            Log.e("beichen", "长按 " + position);
            new AlertDialog.Builder(JSReplaceActivity.this)
                    .setTitle("编辑")
                    .setMessage("是否删除本项数据或删除后重新编辑")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            list.remove(position);
                            replaceItemAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNeutralButton("重新编辑", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ReplaceItem replaceItem = list.get(position);
                            list.remove(position);
                            etJSName.setText(replaceItem.fileName);
                            etJSRule.setText(replaceItem.rule);
                            etJSOri.setText(replaceItem.ori);
                            etJSMod.setText(replaceItem.mod);
                            replaceItemAdapter.notifyDataSetChanged();
                        }
                    })
                    .create().show();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replace_activity);
        etJSName = findViewById(R.id.et_js_name);
        etJSRule = findViewById(R.id.et_js_rule);
        etJSOri = findViewById(R.id.et_js_ori);
        etJSMod = findViewById(R.id.et_js_mod);
        findViewById(R.id.btn_add_rule).setOnClickListener(this);
        listView = findViewById(R.id.list_view);
        //initData();
        initList();

    }

    public void initData(){
        ReplaceItem replaceItem0 = new ReplaceItem("game.js", "", "1e4==InitMark.uid", "1e4!=InitMark.uid");
        ReplaceItem replaceItem1 = new ReplaceItem("game.js", "", "for(var d=i.boxes,h=a+.5*InitMark.stageOffHeight,u=0;8>u;u++)if(d[u+1]>0&&t[\"hitImg\"+u].hitTestPoint(o,h)){this._mark=u+1;break}", "for(var d=i.boxes,u=0;u<8;u++)if(d[u+1]>0){this._mark=u+1;break}");
        ReplaceItem replaceItem2 = new ReplaceItem("game.js", "", "e[\"island\"+n].initIslandView(i)}", "e[\"island\"+n].initIslandView(i)}var tar=0;if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[0].crowns){tar=1;}else if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[1].crowns){tar=2;}else{tar=3;}console.log(\"猜金币,选择: \"+tar);new TextPop(\"选择: \"+tar);");
        ReplaceItem replaceItem3 = new ReplaceItem("game.js", "", "var t=wx.createRewardedVideoAd({adUnitId:e});t.load().then(function(){return t.show()}).catch(function(e){return console.log(e.errMsg)}),t.onClose(function(e){console.log(\"onClose:\",e),n&&n(),t.offClose()}),t.onError(function(e){console.log(\"error:\",e),o&&o(),t.offError()})", "console.log(\"onClose:\", e);n&&n();");
        ReplaceItem replaceItem4 = new ReplaceItem("game.js", "", "e.attackTitan=function(t,n,i,a,o,r){", "e.attackTitan=function(t,n,i,a,o,r){if(a){i=200;}else{i=100;}");
        List<ReplaceItem> list = new ArrayList<>();
        list.add(replaceItem0);
        list.add(replaceItem1);
        list.add(replaceItem2);
        list.add(replaceItem3);
        list.add(replaceItem4);
        Utils.saveReplaceSettings(list);
    }

    private void initList() {
        new Thread(){
            @Override
            public void run() {
                list = Utils.readReplaceSettings();
                if (list == null){
                    list = new ArrayList<>();
                }
                mHandler.sendEmptyMessage(2);
            }
        }.run();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_rule:
                if (TextUtils.isEmpty(etJSName.getText().toString()) || TextUtils.isEmpty(etJSOri.getText().toString())) {
                    Toast.makeText(this, "脚本名称和被替换字符串不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                ReplaceItem replaceItem = new ReplaceItem();
                replaceItem.fileName = etJSName.getText().toString();
                String tmp = TextUtils.isEmpty(etJSRule.getText().toString()) ? "" : etJSRule.getText().toString();
                replaceItem.rule = tmp;
                replaceItem.ori = etJSOri.getText().toString();
                tmp = TextUtils.isEmpty(etJSMod.getText().toString()) ? "" : etJSMod.getText().toString();
                replaceItem.mod = tmp;
                list.add(replaceItem);
                replaceItemAdapter.notifyDataSetChanged();
                break;
        }
    }
    public interface DataCallBack{
        void dataChange();
    }
}
