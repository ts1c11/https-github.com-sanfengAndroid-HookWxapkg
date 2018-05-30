package com.beichen.hookwxx5.widget;

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
import com.beichen.hookwxx5.adapter.ExAdapter;
import com.beichen.hookwxx5.data.BaseItem;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.plugin.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class InjectActivity extends BaseActivity<InjectItem> implements View.OnClickListener {
    private EditText etInjectGameName, etInjectDisplayName, etInjectNode, etInjectAppId, etInjectJscode;
    private CheckBox cbInjectAvailable;
    private Button btnInjectSubmit;
    private static final String TAG = InjectActivity.class.getSimpleName();

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

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                hashMap = Utils.readSettings(InjectItem.class);
                if (hashMap == null) {
                    hashMap = new LinkedHashMap<>();
                }
                Message message = new Message();
                message.arg1 = R.layout.inject_children_layout;
                message.what = INIT_DATA_SUCCESS;
                handler.sendMessage(message);
            }
        }.run();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_inject_submit:
                String gameName = etInjectGameName.getText().toString();
                if (TextUtils.isEmpty(gameName)) {
                    Toast.makeText(this, "游戏名字不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String displayName = etInjectDisplayName.getText().toString();
                if (TextUtils.isEmpty(displayName)) {
                    Toast.makeText(this, "显示名字不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                String node = TextUtils.isEmpty(etInjectNode.getText().toString()) ? "" : etInjectNode.getText().toString();
                String appId = TextUtils.isEmpty(etInjectAppId.getText().toString()) ? "" : etInjectAppId.getText().toString();
                String jsCode = etInjectJscode.getText().toString();
                if (TextUtils.isEmpty(jsCode)) {
                    Toast.makeText(this, "注入脚本不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                boolean available = cbInjectAvailable.isChecked();
                InjectItem injectItem = new InjectItem(gameName, displayName, node, appId, jsCode, available);
                if (hashMap.containsKey(gameName)) {
                    hashMap.get(gameName).add(injectItem);
                    handler.sendEmptyMessage(EXLIST_CHILD_CHANGE);
                } else {
                    List<InjectItem> list = new ArrayList<>();
                    list.add(injectItem);
                    hashMap.put(gameName, list);
                    handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                }
                break;
        }
    }

    private void testData() {
        LinkedHashMap<String, List<BaseItem>> map = new LinkedHashMap<>();
        List<BaseItem> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            InjectItem item = new InjectItem("海盗来了", "海盗", "", "", "jscode " + i, true);
            list.add(item);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.d(TAG, this.getClass().getSimpleName() + " onChildClick: " + groupPosition + " " + childPosition);
        return false;
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
                            InjectItem item = (InjectItem) hashMap.get(Utils.LinkedHashMapIndex2Key(hashMap, groupPosition)).get(childPosition);
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
        Log.d(TAG, "当前组: " + groupPosition + "被展开");

    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        Log.d(TAG, "当前组: " + groupPosition + "被收拢");

    }

}
