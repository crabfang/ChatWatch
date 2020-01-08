package com.cabe.app.watch.plugin;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public interface IPlugin {
    void setService(AccessibilityService service);
    void setCurrentPkg(String pkgName);
    void setCurrentActivity(String activityName);
    void watchChat(AccessibilityEvent event);
    void watchList(AccessibilityEvent event);
    void handleEvent(AccessibilityEvent event);
}