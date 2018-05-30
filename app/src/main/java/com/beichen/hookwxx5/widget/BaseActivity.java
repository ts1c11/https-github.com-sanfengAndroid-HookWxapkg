package com.beichen.hookwxx5.widget;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.adapter.ExAdapter;
import com.beichen.hookwxx5.data.BaseItem;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.plugin.Utils;

import java.util.LinkedHashMap;
import java.util.List;

public class BaseActivity<T extends BaseItem> extends Activity implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener, AdapterView.OnItemLongClickListener, ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {
    protected ExpandableListView expandableListView;
    protected ExAdapter adapter;
    protected LinkedHashMap<String, List<T>> hashMap = null;
    protected static final int INIT_DATA_SUCCESS = 0x100;
    protected static final int DATA_CHANGE_NOTIFY = 0x101;
    protected static final int EXLIST_GROUP_CHANGE = 0x102;
    protected static final int EXLIST_CHILD_CHANGE = 0x103;
    private final String TAG = BaseActivity.class.getSimpleName();



    protected Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case INIT_DATA_SUCCESS:
                    Toast.makeText(BaseActivity.this, "初始化数据成功", Toast.LENGTH_SHORT).show();
                    adapter = new ExAdapter(BaseActivity.this, R.layout.common_group_layout, msg.arg1, hashMap);
                    expandableListView.setAdapter(adapter);
                    expandableListView.setOnChildClickListener(BaseActivity.this);
                    expandableListView.setOnGroupClickListener(BaseActivity.this);
                    expandableListView.setOnItemLongClickListener(BaseActivity.this);
                    expandableListView.setOnGroupExpandListener(BaseActivity.this);
                    expandableListView.setOnGroupCollapseListener(BaseActivity.this);
                    break;
                case DATA_CHANGE_NOTIFY:
                    if (msg.arg1 == 0) {
                        Toast.makeText(BaseActivity.this, "更新数据到文件失败,请查看日志", Toast.LENGTH_SHORT).show();
                    } else if (msg.arg1 == 1) {
                        Toast.makeText(BaseActivity.this, "更新数据到文件成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case EXLIST_GROUP_CHANGE:
                    Log.d(TAG, "通知 adapter group 改变");
                    expandableListView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    expandableListView.setVisibility(View.VISIBLE);
//                    for (int i = 0; i < adapter.getGroupCount(); i++) {
//                        if (expandableListView.isGroupExpanded(i)) {
//                            expandableListView.collapseGroup(i);
//                            expandableListView.expandGroup(i);
//                        } else {
//                            expandableListView.expandGroup(i);
//                            expandableListView.collapseGroup(i);
//                        }
//                    }
                    saveThread.run();
                    // 在通知完显示过后应该将数据写入到文件中
                    break;
                case EXLIST_CHILD_CHANGE:             // 子项删除时调用
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "通知 adapter child 改变");
                    for (int i = 0; i < adapter.getGroupCount(); i++) {       // 首先先展开和收拢刷新子项数据
                        if (expandableListView.isGroupExpanded(i)) {
                            expandableListView.collapseGroup(i);
                            expandableListView.expandGroup(i);
                        } else {
                            expandableListView.expandGroup(i);
                            expandableListView.collapseGroup(i);
                        }
                    }
                    handler.sendEmptyMessage(EXLIST_GROUP_CHANGE);
                    saveThread.run();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onGroupExpand(int groupPosition) {

    }

    @Override
    public void onGroupCollapse(int groupPosition) {

    }

    private Thread saveThread = new Thread(){
        @Override
        public void run() {
            Message message = new Message();
            message.what = DATA_CHANGE_NOTIFY;
            message.arg1 = Utils.saveSettings(hashMap) ? 1 : 0;
            handler.sendMessage(message);
        }
    };
}
