package com.cabe.app.watch.plugin

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.ui.CHAT_TYPE_WX
import com.cabe.app.watch.utils.isCollectionEmpty

class WeChatPlugin : IPlugin {
    private var currentPkgName = ""
    private var currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY
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
            if(nodes.size > 0) curChatName = null
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
        event.source?.findAccessibilityNodeInfosByViewId(generateID("lt"))?.let { nodes ->
            if(nodes.size > 0) {
                val chatName = nodes[0].text
                val editText = event.source?.findAccessibilityNodeInfosByViewId(generateID("aqe"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                curChatName = chatName.toString()
                Log.w("AppDebug", "chat detail: $chatName @ $editText | $curInputStr")
            }
        }
    }

    private fun watchInput(event: AccessibilityEvent) {
        event.source?.apply {
            if(className == "android.widget.EditText") {
                if(text != null) {
                    curInputStr = text.toString()
                    Log.w("AppDebug", "chat input change: $curInputStr")
                }
            }
        }
    }

    private fun recordInput() {
        Log.w("AppDebug", "chat record input: $curChatName $curInputStr")
        if(!TextUtils.isEmpty(curChatName) && !TextUtils.isEmpty(curInputStr)) {
            DBHelper.insertChat(
                WatchChatInfo(
                    CHAT_TYPE_WX,
                    System.currentTimeMillis(),
                    curChatName?:"",
                    "我：$curInputStr"
                )
            )
        }
        curInputStr = null
    }

    override fun handleEvent(event: AccessibilityEvent) {
        when {
            currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY) -> watchList(event)
            currentActivityName.contains(WECHAT_LUCKMONEY_CHATTING_ACTIVITY) -> watchChat(event)
        }
        when(event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED, AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> watchInput(event)
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> recordInput()
        }
    }

    companion object {
        var curChatName:String? = null
        var curInputStr:String? = null
        const val PLUGIN_PACKAGE_NAME = "com.tencent.mm"
        private const val WECHAT_LUCKMONEY_GENERAL_ACTIVITY = "LauncherUI"
        private const val WECHAT_LUCKMONEY_CHATTING_ACTIVITY = "ChattingUI"
    }
}