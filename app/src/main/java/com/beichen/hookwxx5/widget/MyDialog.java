package com.beichen.hookwxx5.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beichen.hookwxx5.plugin.ChoicesCallback;

public class MyDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView tv_title;
    private EditText et_input;
    private Button btn_confirm, btn_cancel;
    private LinearLayout pLayout;
    private RelativeLayout cLayout;
    private ChoicesCallback callback;

    public MyDialog(Context context, ChoicesCallback callback) {
        super(context);
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pLayout = new LinearLayout(context);
        pLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pLayout.setOrientation(LinearLayout.VERTICAL);
        tv_title = new TextView(context);
        tv_title.setText("请输入脚本:");
        et_input = new EditText(context);

        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btn_confirm = new Button(context);
        btn_confirm.setText("确定");
        btn_confirm.setLayoutParams(params1);
        btn_confirm.setTag(0x10000);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        btn_cancel = new Button(context);
        btn_cancel.setText("取消");
        btn_cancel.setTag(0x10001);
        btn_cancel.setLayoutParams(params2);
        cLayout = new RelativeLayout(context);
        cLayout.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        cLayout.addView(btn_cancel);
        cLayout.addView(btn_confirm);
        pLayout.addView(tv_title);
        pLayout.addView(et_input);
        pLayout.addView(cLayout);
        setContentView(pLayout);
        this.setTitle("脚本注入");
        initListener();
    }
    private void initListener(){
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch ((int)v.getTag()){
            case 0x10000:
                String js = et_input.getText().toString();
                Log.e("beichen.Inject", "输入脚本为: " + et_input.getText().toString());
                if (TextUtils.isEmpty(js)){
                    Toast.makeText(context, "注入的脚本不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                callback.success(context, js);
                dismiss();
                break;
            case 0x10001:
                dismiss();
                break;
        }
    }
}
