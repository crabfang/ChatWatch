package com.cabe.app.watch.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.cabe.app.watch.plugin.DingDingPlugin
import com.cabe.app.watch.plugin.IPlugin
import com.cabe.app.watch.plugin.WeChatPlugin

class WatchService: AccessibilityService() {
    companion object {
        var service: WatchService? = null
    }
    private var currentPackageName = ""
    var currentActivityName = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.i("AppDebug", "event type: ${AccessibilityEvent.eventTypeToString(event?.eventType ?: AccessibilityEvent.TYPE_VIEW_CLICKED)}")
        setCurrentActivityName(event)

        var plugin: IPlugin? = null
        if (WeChatPlugin.PLUGIN_PACKAGE_NAME == currentPackageName) {
            plugin = WeChatPlugin()
        } else if (DingDingPlugin.PLUGIN_PACKAGE_NAME == currentPackageName) {
            plugin = DingDingPlugin()
        }

        if (plugin == null) return

        plugin.setService(this)
        plugin.setCurrentPkg(currentPackageName)
        plugin.setCurrentActivity(currentActivityName)
        plugin.handleEvent(event)
    }

    private fun setCurrentActivityName(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        try {
            currentPackageName = event.packageName.toString()
            val componentName = ComponentName(
                currentPackageName,
                event.className.toString()
            )
            packageManager.getActivityInfo(componentName, 0)
            currentActivityName = componentName.flattenToShortString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("AppDebug", "setCurrentActivityName error")
        }
    }
    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        service = this
    }

    override fun onDestroy() {
        super.onDestroy()
        service = null
    }
}