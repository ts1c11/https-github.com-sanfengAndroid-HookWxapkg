package com.beichen.hookwxx5.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beichen.hookwxx5.R;
import com.beichen.hookwxx5.data.InjectItem;
import com.beichen.hookwxx5.plugin.Utils;
import com.beichen.hookwxx5.widget.InjectActivity;
import com.beichen.hookwxx5.widget.JSReplaceActivity;

import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView tv_replace_help, tv_inject_help;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        findViewById(R.id.btn_start_replace).setOnClickListener(this);
        findViewById(R.id.btn_start_inject).setOnClickListener(this);
        findViewById(R.id.btn_test_dialog).setOnClickListener(this);
        tv_replace_help = findViewById(R.id.tv_replace_help);
        tv_inject_help = findViewById(R.id.tv_inject_help);
        initHelp();
    }

    private void initHelp() {
        String replace = "脚本替换帮助如下:\n" +
                         " 1. 脚本替换规则保存在sd卡" + Utils.JS_REPLACE_FILE_NAME + "文件中,每行为一个JSON字符串,可自己按规则添加替换规则\n" +
                         " 2. 由于是在读脚本为流时执行的替换,此时还并未开始小程序进程,目前只拿到了文件名无法判断属于具体的某个小游戏,后续可能会添加\n" +
                         " 3. 小程序脚本提取可以直接到微信私有目录获取,网上教程很多,也可以打开日志查看会在服务器中下载脚本\n" +
                         " 4. 微信小程序包是被混淆和代码缩减的,替换时规则要唯一,如有多个匹配项则只会替换第一个\n";
        tv_replace_help.setText(replace);
        String inject = "脚本注入帮助如下:\n" +
                        " 1. 目前小游戏菜单开放了调试等功能,替换了原有的转发功能为注入脚本功能,待小游戏加载完成点击菜单即可发现\n" +
                        " 2. 因为本程序未考虑Android5.0以下设备,所以注入时使用的是evaluateJavascript方法注入,其内置回调函数只接受String类型\n" +
                        " 3. 回调结果可日志过滤 \"beichen.Inject\" 和 \"注入结果\" 查看\n" +
                        " 4. 注入时可使用的API应参考微信小游戏开放的API";
        tv_inject_help.setText(inject);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_replace:
                startActivity(new Intent(this, JSReplaceActivity.class));
                break;
            case R.id.btn_start_inject:
                startActivity(new Intent(this, InjectActivity.class));
                break;
            case R.id.btn_test_dialog:
                LinkedHashMap<String, List<InjectItem>> map = Utils.readInjectSettings();
                if (map == null || map.size() < 1){
                    Toast.makeText(this, "当前没有注入数据", Toast.LENGTH_SHORT).show();
                    break;
                }
                for (String key : map.keySet()){
                    ChoiceDialog.getInstance().show(this, map.get(key), null);
                    break;
                }
                break;
        }
    }
}
