package com.cabe.app.watch.service

import android.annotation.SuppressLint
import android.app.Notification
import android.os.Parcel
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.cabe.app.watch.db.DBHelper
import com.cabe.app.watch.db.WatchChatInfo
import com.cabe.app.watch.ui.CHAT_TYPE_QQ
import com.cabe.app.watch.ui.CHAT_TYPE_WX
import java.lang.reflect.Field

@SuppressLint("OverrideAbstract")
class NotificationService: NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if ("com.tencent.mm" == sbn?.packageName) {
            handleWeChat(sbn)
        } else if("com.tencent.mobileqq" == sbn?.packageName) {
            handleQQ(sbn)
        }
    }

    private fun getText(notification: Notification?): List<String>? {
        if (null == notification) {
            return null
        }
        var views = notification.bigContentView
        if (views == null) {
            views = notification.contentView
        }
        if (views == null) {
            return null
        }
        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        val text: MutableList<String> = ArrayList()
        try {
            val field: Field = views.javaClass.getDeclaredField("mActions")
            field.isAccessible = true
            val actions = field.get(views) as ArrayList<Parcelable>
            // Find the setText() and setTime() reflection actions
            for (p in actions) {
                val parcel = Parcel.obtain()
                p.writeToParcel(parcel, 0)
                parcel.setDataPosition(0)
                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                val tag = parcel.readInt()
                if (tag != 2) continue
                // View ID
                parcel.readInt()
                val methodName = parcel.readString()
                if (null == methodName) {
                    continue
                } else if (methodName == "setText") { // Parameter type (10 = Character Sequence)
                    parcel.readInt()
                    // Store the actual string
                    val t =
                        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString()
                            .trim { it <= ' ' }
                    text.add(t)
                }
                parcel.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return text
    }

    private fun handleWeChat(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras
        if (extras != null) { // 获取通知标题
            val title = extras.getString(Notification.EXTRA_TITLE, "")
            // 获取通知内容
            var content = extras.getString(Notification.EXTRA_TEXT, "")
            val index = content.indexOf(": ")
            if(index >= 0) {
                content = content.substring(index + 2)
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