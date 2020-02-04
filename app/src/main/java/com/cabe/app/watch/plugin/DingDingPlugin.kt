package com.cabe.app.watch.plugin

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.ui.CHAT_TYPE_DD
import com.cabe.app.watch.utils.isCollectionEmpty
import java.text.SimpleDateFormat
import java.util.*

class DingDingPlugin : IPlugin {
    private var currentPkgName = ""
    private var currentActivityName = ""
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

    private fun recordChat(chatInfo: WatchChatInfo) {
        if(!TextUtils.isEmpty(chatInfo.chatName) && !TextUtils.isEmpty(chatInfo.content)) {
            DBHelper.insertChat(chatInfo)
        }
    }

    override fun watchChat(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByViewId(generateID("tv_title"))?.let { titleNodes ->
            if(titleNodes.size > 0) curChatName = titleNodes[0].text.toString()
        }

        event.source?.findAccessibilityNodeInfosByViewId(generateID("list_view"))?.let { listNodes ->
            if(listNodes.size > 0) {
                val listView = listNodes[0]
                var contentStr = ""
                for(i in 0 until listView.childCount) {
                    listView.getChild(i).let { itemNode ->
                        var desc = itemNode.contentDescription
                        if(!desc.startsWith("我")) {
                            desc = "TA$desc"
                        }
                        contentStr += "$desc \n"
                    }
                }
                Log.i("AppDebug", contentStr)
                if(!TextUtils.isEmpty(curChatName) && contentStr != lastContent) {
                    lastContent = contentStr
                    val chatInfo = WatchChatInfo(
                        CHAT_TYPE_DD,
                        System.currentTimeMillis(),
                        curChatName?:"",
                        contentStr,
                        ""
                    )
                    recordChat(chatInfo)
                }
            }
        }
    }

    private val tmpFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS", Locale.getDefault())
    private val dateFormat= SimpleDateFormat("HH:mm", Locale.getDefault())
    private fun parseTime2Long(timeStr: String): Long? {
        return try {
            val date = if(timeStr=="刚刚") Date() else dateFormat.parse(timeStr)
            val tmpCalendar = Calendar.getInstance().apply { time = date }
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, tmpCalendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, tmpCalendar.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            Log.w("AppDebug", "parseTime2Long error $timeStr")
            null
        }
    }
    override fun watchList(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByViewId(generateID("session_item"))?.let { nodes ->
            if(nodes.size > 0) curChatName = null
            val list = mutableListOf<WatchChatInfo>()
            nodes.forEach{ item ->
                val chatName = item.findAccessibilityNodeInfosByViewId(generateID("session_title"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                val chatTime = item.findAccessibilityNodeInfosByViewId(generateID("session_gmt"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                val chatContent = item.findAccessibilityNodeInfosByViewId(generateID("session_content_tv"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                val chatTimestamp = parseTime2Long(chatTime.toString())
                if(chatTimestamp != null) {
                    list.add(
                        WatchChatInfo(
                            CHAT_TYPE_DD,
                            chatTimestamp,
                            chatName.toString(),
                            chatContent.toString(),
                            ""
                        )
                    )
                }
            }
            val curTime = System.currentTimeMillis()
            if(curTime - lastListRecordTime > 1000 * 5 && list.isNotEmpty()) {
                lastListRecordTime = curTime
                list.forEach { item ->
                    val newTimeStr = item.timestamp.let { tmpFormat.format(it) }
                    Log.i("AppDebug", "chat info: ${item.chatName} say # ${item.content} ---> $newTimeStr")
                    recordChat(item)
                }
            }
        }
    }

    override fun handleEvent(event: AccessibilityEvent) {
        when(event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                curChatName = ""
                watchList(event)
                watchChat(event)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> watchChat(event)
        }
    }

    companion object {
        var curChatName: String? = null
        var lastContent: String?= null
        var lastListRecordTime = 0L
        const val PLUGIN_PACKAGE_NAME = "com.alibaba.android.rimet"
    }
}