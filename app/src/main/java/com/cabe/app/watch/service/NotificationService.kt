package com.cabe.app.watch.service

import android.annotation.SuppressLint
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.ui.CHAT_TYPE_QQ
import com.cabe.app.watch.ui.CHAT_TYPE_WX

@SuppressLint("OverrideAbstract")
class NotificationService: NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if ("com.tencent.mm" == sbn?.packageName) {
            handleWeChat(sbn)
        } else if("com.tencent.mobileqq" == sbn?.packageName) {
            handleQQ(sbn)
        }
    }

    private fun handleWeChat(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras
        if (extras != null) { // 获取通知标题
            val title = extras.getString(Notification.EXTRA_TITLE, "")
            // 获取通知内容
            var content = extras.getString(Notification.EXTRA_TEXT, "")
            val index = content.indexOf("]")
            if(index >= 0) {
                content = content.substring(index + 1)
            }
            Log.w("AppDebug", "Notification: $title _ $content")
            DBHelper.insertChat(
                WatchChatInfo(
                    CHAT_TYPE_WX,
                    System.currentTimeMillis(),
                    title,
                    content
                )
            )
        }
    }

    private fun handleQQ(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras
        if (extras != null) { // 获取通知标题
            val title = extras.getString(Notification.EXTRA_TITLE, "")
            // 获取通知内容
            val content = extras.getString(Notification.EXTRA_TEXT, "")
            Log.w("AppDebug", "Notification: $title _ $content")
            DBHelper.insertChat(
                WatchChatInfo(
                    CHAT_TYPE_QQ,
                    System.currentTimeMillis(),
                    title,
                    content
                )
            )
        }
    }
}