package com.beichen.hookwxx5.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beichen.hookwxx5.widget.JSReplaceActivity;
import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.data.ReplaceItem;

import java.util.List;

public class ReplaceItemAdapter extends ArrayAdapter<ReplaceItem> {
    private final int resourceId;
    private Context context;
    public List<ReplaceItem> list;
    private JSReplaceActivity.DataCallBack callBack;

    public ReplaceItemAdapter(Context context, int resource, List<ReplaceItem> list, JSReplaceActivity.DataCallBack callBack) {
        super(context, resource);
        this.context = context;
        resourceId = resource;
        this.list = list;
        this.callBack = callBack;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ReplaceItem getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReplaceItem cur =  getItem(position);
        View view;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(resourceId, parent, false);
        }else {
            view = convertView;
        }
        TextView tvFileName = view.findViewById(R.id.tv_file_name);
        TextView tvRule = view.findViewById(R.id.tv_rule);
        TextView tvOri = view.findViewById(R.id.tv_ori);
        TextView tvMod = view.findViewById(R.id.tv_mod);
        tvFileName.setText(cur.fileName);
        tvRule.setText(cur.rule);
        tvOri.setText(cur.ori);
        tvMod.setText(cur.mod);
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.e("beichen", "list改变");
        super.notifyDataSetChanged();
        callBack.dataChange();
    }
}

