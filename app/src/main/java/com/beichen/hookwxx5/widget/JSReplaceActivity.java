package com.beichen.hookwxx5.widget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.data.ReplaceItem;
import com.beichen.hookwxx5.plugin.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class JSReplaceActivity extends BaseActivity<ReplaceItem> implements View.OnClickListener{

    private EditText etGameName, etFileName, etAppId, etJSOri, etJSMod;
    private CheckBox cbAvailable;
    private static final String TAG = JSReplaceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replace_activity);
        etGameName = findViewById(R.id.et_replace_game_name);
        etAppId = findViewById(R.id.et_replace_game_appid);
        etFileName = findViewById(R.id.et_replace_name);
        etJSOri = findViewById(R.id.et_replace_js_ori);
        etJSMod = findViewById(R.id.et_replace_js_mod);
        cbAvailable = findViewById(R.id.cb_replace_available);
        findViewById(R.id.btn_add_rule).setOnClickListener(this);
        expandableListView = findViewById(R.id.exlist_replace);
        initData();
        initList();

    }

    public void initData(){
        ReplaceItem replaceItem0 = new ReplaceItem("海盗来了", "", "game.js", "1e4==InitMark.uid", "1e4!=InitMark.uid", true);
        ReplaceItem replaceItem1 = new ReplaceItem("海盗来了", "","game.js","for(var d=i.boxes,h=a+.5*InitMark.stageOffHeight,u=0;8>u;u++)if(d[u+1]>0&&t[\"hitImg\"+u].hitTestPoint(o,h)){this._mark=u+1;break}", "for(var d=i.boxes,u=0;u<8;u++)if(d[u+1]>0){this._mark=u+1;break}", true);
        ReplaceItem replaceItem2 = new ReplaceItem("海盗来了", "","game.js", "e[\"island\"+n].initIslandView(i)}", "e[\"island\"+n].initIslandView(i)}var tar=0;if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[0].crowns){tar=1;}else if(dataManager.data.stealTarget.crowns==dataManager.data.stealIslands[1].crowns){tar=2;}else{tar=3;}console.log(\"猜金币,选择: \"+tar);new TextPop(\"选择: \"+tar);", true);
        ReplaceItem replaceItem3 = new ReplaceItem("海盗来了", "","game.js", "var t=wx.createRewardedVideoAd({adUnitId:e});t.load().then(function(){return t.show()}).catch(function(e){return console.log(e.errMsg)}),t.onClose(function(e){console.log(\"onClose:\",e),n&&n(),t.offClose()}),t.onError(function(e){console.log(\"error:\",e),o&&o(),t.offError()})", "console.log(\"onClose:\", e);n&&n();", true);
        ReplaceItem replaceItem4 = new ReplaceItem("海盗来了", "","game.js", "e.attackTitan=function(t,n,i,a,o,r){", "e.attackTitan=function(t,n,i,a,o,r){if(a){i=200;}else{i=100;}", true);
        List<ReplaceItem> list = new ArrayList<>();
        list.add(replaceItem0);
        list.add(replaceItem1);
        list.add(replaceItem2);
        list.add(replaceItem3);
        list.add(replaceItem4);
        LinkedHashMap<String, List<ReplaceItem>> map1 = new LinkedHashMap<>();
        map1.put("海盗来了", list);
        Utils.saveSettings(map1);
    }

    private void initList() {
        new Thread(){
            @Override
            public void run() {
                hashMap = Utils.readSettings(ReplaceItem.class);
                if (hashMap == null){
                    hashMap = new LinkedHashMap<String, List<ReplaceItem>>();
                }
                Message message = new Message();
                message.what = INIT_DATA_SUCCESS;
                message.arg1 = R.layout.replace_children_layout;
                handler.sendMessage(message);
            }
        }.run();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_rule:
                String gameName = etGameName.getText().toString();
                if (TextUtils.isEmpty(gameName)){
                    Toast.makeText(this, "游戏名字不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String appId = TextUtils.isEmpty(etAppId.getText().toString()) ? "" : etAppId.getText().toString();
                String fileName = etFileName.getText().toString();
                if (TextUtils.isEmpty(fileName)){
                    Toast.makeText(this, "脚本文件名不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String jsOri = etJSOri.getText().toString();
                if (TextUtils.isEmpty(jsOri)){
                    Toast.makeText(this, "替换前脚本不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String jsMod = etJSMod.getText().toString();
                boolean available = cbAvailable.isChecked();
                ReplaceItem replaceItem = new ReplaceItem();
                replaceItem.setGameName(gameName);
                replaceItem.setAppId(appId);
                replaceItem.setFileName(fileName);
                replaceItem.setOri(jsOri);
                replaceItem.setMod(jsMod);
                replaceItem.setAvailable(available);
                if (hashMap.containsKey(gameName)){
                    hashMap.get(gameName).add(replaceItem);
                    handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                }else {
                    List<ReplaceItem> list = new ArrayList<>();
                    list.add(replaceItem);
                    hashMap.put(gameName, list);
                    handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                }
                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.d(TAG, this.getClass().getSimpleName() + " onChildClick: " + groupPosition + " " + childPosition);
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        Log.d(TAG, this.getClass().getSimpleName() + " onGroupClick: " + groupPosition);
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, this.getClass().getSimpleName() + " onItemLongClick: " + position);
        final long packedPosition = expandableListView.getExpandableListPosition(position);
        final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
        final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
        Log.d(TAG, "当前长按的位置为, 父: " + groupPosition + " 子: " + childPosition);
        if (childPosition == -1) {                                   // 长按父容器时 childPostion = -1
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告")
                    .setMessage("您正在试图删除该游戏所有数据, 确定删除吗?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hashMap.remove(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition));
                            handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                        }
                    })
                    .setNegativeButton("取消", null);
            builder.show();
        } else {                                                     // 长按的是某个子项
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告")
                    .setMessage("您正在试图删除或修改该项数据, 确定删除或修改吗?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).remove(childPosition);
                            if (hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).size() > 0){
                                handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                            }else {
                                hashMap.remove(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition));
                                handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setNeutralButton("重新编辑", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ReplaceItem item = (ReplaceItem) hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).get(childPosition);
                            etGameName.setText(item.getGameName());
                            etAppId.setText(item.getAppId());
                            etFileName.setText(item.getFileName());
                            etJSOri.setText(item.getOri());
                            etJSMod.setText(item.getMod());
                            cbAvailable.setChecked(item.isAvailable());
                            hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).remove(childPosition);
                            handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                        }
                    });
            builder.show();
        }
        return true;
    }

    @Override
    public void onGroupExpand(int groupPosition) {

    }

    @Override
    public void onGroupCollapse(int groupPosition) {

    }


}
