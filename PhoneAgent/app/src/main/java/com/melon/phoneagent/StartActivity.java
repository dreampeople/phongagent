package com.melon.phoneagent;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.melon.util.APKUtil;
import com.melon.util.AndroidUtil;

public class StartActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button startBtn = findViewById(R.id.startButton);

        startBtn.setOnClickListener(this::startClick);
    }

    void startClick(View view) {
        //权限检查

        // 检查是否有悬浮窗权限
        if (!android.provider.Settings.canDrawOverlays(this)) {
            // 若没有权限，启动设置页面让用户授予权限
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
            return;
        }

        // 检查是否有模拟点击权限（辅助功能权限）
        if (!AndroidUtil.isAccessibilitySettingsOn(AgentAccessibilityService.class.getName())) {
            // 若没有权限，启动设置页面让用户授予权限
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
