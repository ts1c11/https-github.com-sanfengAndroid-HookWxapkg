package com.beichen.hookwxx5;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {
    private final int resourceId;
    private Context context;
    public List<Item> list;
    private MainActivity.DataCallBack callBack;

    public ItemAdapter(Context context, int resource, List<Item> list, MainActivity.DataCallBack callBack) {
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
    public Item getItem(int position) {
        return list.get(position);
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        Item cur =  getItem(position);
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

