package com.cabe.app.watch.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import com.cabe.app.watch.R
import com.cabe.app.watch.service.WatchService
import kotlinx.android.synthetic.main.activity_main.*

const val CHAT_TYPE_WX = "chatTypeWX"
const val CHAT_TYPE_QQ = "chatTypeQQ"
class MainActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        canBack = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity_main_btn_service.setOnClickListener {
            openServiceSetting()
        }
        activity_main_btn_notification.setOnClickListener {
            openNotificationListenSettings()
        }
        activity_main_btn_chat_wx.setOnClickListener {
            openChatList(CHAT_TYPE_WX)
        }
        activity_main_btn_chat_qq.setOnClickListener {
            openChatList(CHAT_TYPE_QQ)
        }
    }

    private fun openChatList(chatType: String) {
        startActivity(Intent(this, ChatListActivity::class.java).apply {
            putExtra(EXTRA_KEY_CHAT_TYPE, chatType)
        })
    }

    /** 判断当前服务是否正在运行 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun isRunning(): Boolean {
        if (WatchService.service == null) {
            return false
        }
        val accessibilityManager =
            WatchService.service?.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val info: AccessibilityServiceInfo = WatchService.service?.serviceInfo ?: return false
        val list =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val iterator: Iterator<AccessibilityServiceInfo> = list.iterator()
        var isConnect = false
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i.id == info.id) {
                isConnect = true
                break
            }
        }
        return isConnect
    }

    fun openServiceSetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    /** 检测通知监听服务是否被授权 */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageNames =
            NotificationManagerCompat.getEnabledListenerPackages(this)
        return packageNames.contains(context.packageName)
    }

    /** 打开通知监听设置页面 */
    fun openNotificationListenSettings() {
        try {
            val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}