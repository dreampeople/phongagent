package com.melon.phoneagent;

import android.app.Application;

import com.melon.util.StaticVarUtil;

public class AgentApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        StaticVarUtil.init(getApplicationContext());
    }
}
