package com.cabe.app.watch.plugin

import android.accessibilityservice.AccessibilityService
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.ui.CHAT_TYPE_WX
import com.cabe.app.watch.utils.isCollectionEmpty
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class WeChatPlugin : IPlugin {
    private val wechatContentFromOther = 1
    private val wechatContentFromMine = 2

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

    private fun recordChat(chatInfo: WatchChatInfo) {
        if(!TextUtils.isEmpty(chatInfo.chatName) && !TextUtils.isEmpty(chatInfo.content)) {
            DBHelper.insertChat(chatInfo)
        }
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
            Log.i("AppDebug", "parseTime2Long error $timeStr")
            null
        }
    }
    override fun watchList(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByViewId(generateID("bah"))?.let { nodes ->
            if(nodes.size > 0) curChatName = null
            val list = mutableListOf<WatchChatInfo>()
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
                val chatTimestamp = parseTime2Long(chatTime.toString())
                if(chatTimestamp != null) {
                    list.add(
                        WatchChatInfo(
                            CHAT_TYPE_WX,
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
                    Log.w("AppDebug", "chat info: ${item.chatName} say # ${item.content} ---> $newTimeStr")
                    recordChat(item)
                }
            }
        }
        event.source?.findAccessibilityNodeInfosByViewId(generateID("lt"))?.let { nodes ->
            if(nodes.size > 0) {
                val chatName = nodes[0].text
                val editText = event.source?.findAccessibilityNodeInfosByViewId(generateID("aqe"))?.let { names ->
                    if(isCollectionEmpty(names)) null
                    else names[0].text
                }
                if(curChatName == null) {
                    curChatName = chatName.toString()
                }
                Log.i("AppDebug", "chat detail: $chatName @ $editText | $curInputStr")
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

    private fun recordInput(event: AccessibilityEvent) {
        val nodeList = event.source?.findAccessibilityNodeInfosByViewId(generateID("ag"))
        if(nodeList == null || nodeList.isEmpty()) return

        nodeList.first()?.let { listView ->
            if(listView.childCount > 0) {
                val contentNodes = listView.getChild(listView.childCount - 1).findAccessibilityNodeInfosByViewId(generateID("ab"))
                if(contentNodes?.size ?: 0 > 0) {
                    contentNodes.first().let { itemView ->
                        val firstClass = itemView.getChild(0).className
                        val secondClass = itemView.getChild(1).className
                        Log.d("AppDebugChat", "chat item: $firstClass $secondClass")
                        val fromType = if(firstClass == "android.widget.ImageView") wechatContentFromOther else wechatContentFromMine
                        findFirstNode(itemView,"pq")?.let {
                            val content = if(fromType == wechatContentFromOther) "对方发送文本" else "我：$curInputStr"
                            recordChat(
                                WatchChatInfo(
                                    CHAT_TYPE_WX,
                                    System.currentTimeMillis(),
                                    curChatName?:"",
                                    content,
                                    ""
                                )
                            )
                        }
                        findFirstNode(itemView,"atb")?.let {
                            val content = if(fromType == wechatContentFromOther) "对方发送图片" else "我发送图片"
                            recordChat(
                                WatchChatInfo(
                                    CHAT_TYPE_WX,
                                    System.currentTimeMillis(),
                                    curChatName?:"",
                                    content,
                                    ""
                                )
                            )
                        }
                        findFirstNode(itemView,"atb")?.let { web ->
                            findFirstNode(web, "atq")?.let { title ->
                                val titleStr = title.text
                                val content = if(fromType == wechatContentFromOther) "对方分享网页:$titleStr" else "我分享网页:$titleStr"
                                recordChat(
                                    WatchChatInfo(
                                        CHAT_TYPE_WX,
                                        System.currentTimeMillis(),
                                        curChatName?:"",
                                        content,
                                        ""
                                    )
                                )
                            }
                        }
                        findFirstNode(itemView,"atc")?.let { program ->
                            val titleStr = findFirstNode(program, "atf")?.text
                            val content = if(fromType == wechatContentFromOther) "对方分享小程序:$titleStr" else "我分享小程序:$titleStr"
                            recordChat(
                                WatchChatInfo(
                                    CHAT_TYPE_WX,
                                    System.currentTimeMillis(),
                                    curChatName?:"",
                                    content,
                                    ""
                                )
                            )
                        }
                    }
                }
            }
        }
        curInputStr = null
    }

    private fun findFirstNode(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        return node.parent.findAccessibilityNodeInfosByViewId(generateID(id))?.let {
            if(it.size > 0) it.first()
            else null
        }
    }

    override fun handleEvent(event: AccessibilityEvent) {
        when {
            currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY) -> watchList(event)
            currentActivityName.contains(WECHAT_LUCKMONEY_CHATTING_ACTIVITY) -> watchChat(event)
        }
        when(event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED, AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> watchInput(event)
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> recordInput(event)
        }
    }

    companion object {
        var curChatName:String? = null
        var curInputStr:String? = null
        var lastListRecordTime = 0L
        const val PLUGIN_PACKAGE_NAME = "com.tencent.mm"
        private const val WECHAT_LUCKMONEY_GENERAL_ACTIVITY = "LauncherUI"
        private const val WECHAT_LUCKMONEY_CHATTING_ACTIVITY = "ChattingUI"
    }
}