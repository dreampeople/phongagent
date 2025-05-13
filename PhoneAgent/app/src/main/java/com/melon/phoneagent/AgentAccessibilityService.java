package com.melon.phoneagent;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.melon.phoneagent.autil.AccessServiceUtil;

public class AgentAccessibilityService extends AccessibilityService {

    void init() {

    }

    @Override
    public void onServiceConnected() {
        AccessServiceUtil.init(this);

        init();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
        AccessServiceUtil.init(null);
    }
}
