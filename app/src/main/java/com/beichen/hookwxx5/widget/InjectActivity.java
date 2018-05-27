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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.adapter.InjectExAdapter;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.plugin.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class InjectActivity extends Activity implements View.OnClickListener, ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener, AdapterView.OnItemLongClickListener, ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {
    private EditText etInjectGameName, etInjectDisplayName, etInjectNode, etInjectAppId, etInjectJscode;
    private CheckBox cbInjectAvailable;
    private Button btnInjectSubmit;
    private LinkedHashMap<String, List<InjectItem>> hashMap =null;
    private ExpandableListView expandableListView;
    private static final String TAG = InjectActivity.class.getSimpleName();
    private InjectExAdapter adapter;

    private static final int INIT_DATA_SUCCESS = 0x100;
    private static final int DATA_CHANGE_NOTIFY = 0x101;
    private static final int EXLIST_GROUP_CHANGE = 0x102;
    private static final int EXLIST_CHILD_CHANGE = 0x103;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INIT_DATA_SUCCESS:
                    Toast.makeText(InjectActivity.this, "初始化数据成功", Toast.LENGTH_SHORT).show();
                    adapter = new InjectExAdapter(InjectActivity.this, R.layout.inject_group_layout, R.layout.inject_children_layout, hashMap);
                    expandableListView.setAdapter(adapter);
                    expandableListView.setOnChildClickListener(InjectActivity.this);
                    expandableListView.setOnGroupClickListener(InjectActivity.this);
                    expandableListView.setOnItemLongClickListener(InjectActivity.this);
                    expandableListView.setOnGroupExpandListener(InjectActivity.this);
                    expandableListView.setOnGroupCollapseListener(InjectActivity.this);
                    break;
                case DATA_CHANGE_NOTIFY:
                    if (msg.arg1 == 0){
                        Toast.makeText(InjectActivity.this, "更新数据到文件失败,请查看日志", Toast.LENGTH_SHORT).show();
                    }else if (msg.arg1 == 1){
                        Toast.makeText(InjectActivity.this, "更新数据到文件成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case EXLIST_GROUP_CHANGE:
                    Log.e(TAG, "通知 adapter group 改变");
                    expandableListView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    expandableListView.setVisibility(View.VISIBLE);
                    for (int i = 0; i <adapter.getGroupCount(); i++){
                        if (expandableListView.isGroupExpanded(i)){
                            expandableListView.collapseGroup(i);
                            expandableListView.expandGroup(i);
                        }else {
                            expandableListView.expandGroup(i);
                            expandableListView.collapseGroup(i);
                        }
                    }
                    // 在通知完显示过后应该将数据写入到文件中
                    saveThread.run();
                    break;
                case EXLIST_CHILD_CHANGE:             // 子项删除时调用
                    Log.e(TAG, "通知 adapter child 改变");
                    for (int i = 0; i <adapter.getGroupCount(); i++){       // 首先先展开和收拢刷新子项数据
                        if (expandableListView.isGroupExpanded(i)){
                            expandableListView.collapseGroup(i);
                            expandableListView.expandGroup(i);
                        }else {
                            expandableListView.expandGroup(i);
                            expandableListView.collapseGroup(i);
                        }
                    }
                    boolean z = false;
                    for (String key: hashMap.keySet()){
                        if (hashMap.get(key).size() == 0){
                            hashMap.remove(key);
                            z = true;
                            break;
                        }
                    }
                    if (z){
                        handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                    }else {
                      saveThread.run();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inject_activity);
        initView();
        initData();
    }

    private void initView() {
        etInjectGameName = findViewById(R.id.et_inject_game_name);
        etInjectDisplayName = findViewById(R.id.et_inject_display_name);
        etInjectNode = findViewById(R.id.et_inject_node);
        etInjectAppId = findViewById(R.id.et_inject_appId);
        etInjectJscode = findViewById(R.id.et_inject_jscode);
        cbInjectAvailable = findViewById(R.id.cb_inject_available);
        btnInjectSubmit = findViewById(R.id.btn_inject_submit);
        btnInjectSubmit.setOnClickListener(this);
        expandableListView = findViewById(R.id.exlist_inject);
    }

    private void initData(){
        new Thread(){
            @Override
            public void run() {
                hashMap = Utils.readInjectSettings();
                if (hashMap == null){
                    hashMap = new LinkedHashMap<>();
                }
                handler.sendEmptyMessage(INIT_DATA_SUCCESS);
            }
        }.run();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_inject_submit:
                String gameName = etInjectGameName.getText().toString();
                if (TextUtils.isEmpty(gameName)){
                    Toast.makeText(this, "游戏名字不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String displayName = etInjectDisplayName.getText().toString();
                if (TextUtils.isEmpty(displayName)){
                    Toast.makeText(this, "显示名字不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String node = TextUtils.isEmpty(etInjectNode.getText().toString()) ? "" : etInjectNode.getText().toString();
                String appId = TextUtils.isEmpty(etInjectAppId.getText().toString()) ? "" : etInjectAppId.getText().toString();
                String jsCode = etInjectJscode.getText().toString();
                if (TextUtils.isEmpty(jsCode)){
                    Toast.makeText(this, "注入脚本不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                boolean avaliable = cbInjectAvailable.isChecked();
                InjectItem injectItem = new InjectItem(gameName, displayName, node, appId, jsCode, avaliable);
                if (hashMap.containsKey(gameName)){
                    hashMap.get(gameName).add(injectItem);
                    handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                }else {
                    List<InjectItem> list = new ArrayList<>();
                    list.add(injectItem);
                    hashMap.put(gameName, list);
                    handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                }
                break;
        }
    }
    private void testData(){
        LinkedHashMap<String, List<InjectItem>> map = new LinkedHashMap<>();
        List<InjectItem> list = new ArrayList<>();
        for (int i = 0; i < 6; i++){
            InjectItem item = new InjectItem("海盗来了", "海盗", "", "", "jscode " + i, true);
            list.add(item);
        }

        map.put("海盗1", list);
        InjectItem[] data = list.toArray(new InjectItem[0]);
        InjectItem[] data2 = new InjectItem[data.length];
        System.arraycopy(data, 0, data2, 0, data.length);
        List<InjectItem> list2 = Arrays.asList(data2);
        map.put("海盗2", list2);
        Utils.saveInjectSettings(map);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.e(TAG, "当前点击为, 父位置: " + groupPosition + " 子位置: " + childPosition);
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Log.e(TAG, "当前点击为, 父位置: " + groupPosition);
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final long packedPosition = expandableListView.getExpandableListPosition(position);
        final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
        final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
        Log.e(TAG, "当前长按的位置为, 父: " + groupPosition + " 子: " + childPosition);
        if (childPosition == -1){                                   // 长按父容器时 childPostion = -1
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
        }else {                                                     // 长按的是某个子项
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告")
                    .setMessage("您正在试图删除或修改该项数据, 确定删除或修改吗?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).remove(childPosition);
                            handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .setNeutralButton("重新编辑", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            InjectItem item = hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).get(childPosition);
                            etInjectGameName.setText(item.getGameName());
                            etInjectDisplayName.setText(item.getDisplayName());
                            etInjectNode.setText(item.getNode());
                            etInjectAppId.setText(item.getAppId());
                            etInjectJscode.setText(item.getJsCode());
                            cbInjectAvailable.setChecked(item.isAvailable());
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
        Log.e(TAG, "当前组: " + groupPosition + "被展开");

    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        Log.e(TAG, "当前组: " + groupPosition + "被收拢");

    }

    Thread saveThread = new Thread(){
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = DATA_CHANGE_NOTIFY;
            if (Utils.saveInjectSettings(hashMap)){
                msg.arg1 = 1;
            }else {
                msg.arg1 = 0;
            }
            handler.sendMessage(msg);
        }
    };
}
