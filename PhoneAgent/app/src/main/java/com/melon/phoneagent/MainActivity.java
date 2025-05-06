package com.melon.phoneagent;

import java.nio.Buffer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.melon.phoneagent.AgentApi.Callback;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    TextView textView; // 文本视图对象
    EditText inputText; // 输入框对象
    Button sendButton; // 发送按钮对象
    Button listenButton; // 监听按钮对象

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        inputText = findViewById(R.id.inputText);
        sendButton = findViewById(R.id.sendButton);
        listenButton = findViewById(R.id.listenButton);

        // 为发送按钮和监听按钮设置点击事件监听器
        sendButton.setOnClickListener(this::onSendButtonClick);
        listenButton.setOnLongClickListener(this::onListenButtonLongClick);
        listenButton.setOnTouchListener(this::onListenButtonTouch);

        AgentApi.getInstance().init(new Callback() {
            @Override
            public void doInitFinished(boolean ret) {
            }
        });

    }

    /**
     * 当发送按钮被点击时调用此方法。
     * 从输入框中获取文本内容，并将其设置到文本视图中显示。
     *
     * @param view 被点击的视图对象
     */
    void onSendButtonClick(View view) {
        String text = inputText.getText().toString();
        textView.setText(text);

        AgentApi.getInstance().chat(text, new Callback() {
            @Override
            public void doChatFinished(String ret) {
                runOnUiThread(() -> textView.setText(ret));
            }
        });
    }

    /**
     * 当长按监听按钮时触发的回调方法。
     *
     * @param view 被长按的视图对象
     * @return 返回false表示未消费事件，允许继续传递事件。
     */
    boolean onListenButtonLongClick(View view) {
        textView.setText("Long press started");
        return false;
    }

    /**
     * 处理监听按钮的触摸事件。
     *
     * @param view 触发事件的视图。
     * @param event 触摸事件对象，包含事件的详细信息。
     * @return 返回布尔值，指示是否消费了该事件。此处始终返回 false。
     *
     * 当触摸事件的动作为抬起（ACTION_UP）时，更新 TextView 的文本为 "Long press ended"。
     */
    boolean onListenButtonTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            textView.setText("Long press ended");
        }
        return false;
    }
}
