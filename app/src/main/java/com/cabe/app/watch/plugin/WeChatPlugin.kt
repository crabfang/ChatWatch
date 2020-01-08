package com.cabe.app.watch.plugin

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.cabe.app.watch.utils.isCollectionEmpty

class WeChatPlugin : IPlugin {
    private var currentPkgName = ""
    private var currentActivityName =
        WECHAT_LUCKMONEY_GENERAL_ACTIVITY
    private var service: AccessibilityService? = null
    private fun generateID(id: String): String {
        return "${currentPkgName}:id/$id"
    }
    override fun setService(service: AccessibilityService) {
        this.service = service
    }

    override fun setCurrentPkg(pkgName: String) {
        currentPkgName = pkgName
    }
    override fun setCurrentActivity(activityName: String) {
        currentActivityName = activityName
    }

    override fun watchChat(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByViewId(generateID("fdg"))?.let { nodes ->
            nodes.forEach{ item ->
                item.findAccessibilityNodeInfosByViewId(generateID("lt"))?.let { names ->
                    Log.w("AppDebug", "chat name detail : ${names[0].text}")
                }
                item.findAccessibilityNodeInfosByViewId(generateID("ag"))?.let { listView ->
                    listView.forEach { item ->
                        item.findAccessibilityNodeInfosByViewId(generateID("pq"))?.let { contents ->
                            contents.forEachIndexed { index, nodeInfo ->
                                Log.w("AppDebug", "chat content: $index ${nodeInfo.extras}")
                            }
                        }
                    }
                }
            }
        }
        watchList(event)
    }

    override fun watchList(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByViewId(generateID("bah"))?.let { nodes ->
            nodes.forEach{ item ->
                val chatName = item.findAccessibilityNodeInfosByViewId(generateID("baj"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                val chatTime = item.findAccessibilityNodeInfosByViewId(generateID("bak"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                val chatContent = item.findAccessibilityNodeInfosByViewId(generateID("bal"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                Log.w("AppDebug", "chat info: $chatName @ $chatTime say # $chatContent")
            }
        }
    }

    override fun handleEvent(event: AccessibilityEvent) {
        when {
            currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY) -> watchList(event)
            currentActivityName.contains(WECHAT_LUCKMONEY_CHATTING_ACTIVITY) -> watchChat(event)
        }
    }

    companion object {
        const val PLUGIN_PACKAGE_NAME = "com.tencent.mm"
        private const val WECHAT_LUCKMONEY_GENERAL_ACTIVITY = "LauncherUI"
        private const val WECHAT_LUCKMONEY_CHATTING_ACTIVITY = "ChattingUI"
    }
}