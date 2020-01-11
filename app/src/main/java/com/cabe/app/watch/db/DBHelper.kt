package com.cabe.app.watch.db

import android.text.TextUtils
import androidx.lifecycle.LiveData
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cabe.app.watch.ui.SP_KEY_REMOTE_TABLE
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.json.JSONArray

const val REMOTE_TABLE_DEFAULT = "Remote"
class DBHelper {
    companion object {
        private const val tableSuffix = "Table"
        private fun getRemoteTable(): String {
            val prefix = SPUtils.getInstance().getString(SP_KEY_REMOTE_TABLE, REMOTE_TABLE_DEFAULT)
            return "${prefix}$tableSuffix"
        }

        fun queryRemoteData(tableName: String, chatName: String?, remoteCallback: (dataList: MutableList<BChatInfo>?) -> Unit) {
            if(TextUtils.isEmpty(tableName)) return

            val query = BmobQuery<JSONArray>("${tableName}$tableSuffix")
                .order("timestamp")
            if(!TextUtils.isEmpty(chatName)) {
                query.addWhereEqualTo("chatName", chatName)
            }
            query.findObjectsByTable(object: QueryListener<JSONArray>() {
                override fun done(array: JSONArray?, e: BmobException?) {
                    if(e != null) {
                        e.printStackTrace()
                        ToastUtils.showShort(e.message)
                    }
                    if(array != null) {
                       val dataList = GsonUtils.fromJson<MutableList<BChatInfo>>(array.toString(), object: TypeToken<MutableList<BChatInfo>>(){}.type)
                        remoteCallback(dataList)
                    }
                }
            })
        }

        fun queryChatListAll(chatType: String): LiveData<List<WatchChatInfo>> {
            return AppDatabase.getInstance().watchChatDao().queryAllChatList(chatType)
        }

        fun queryChatListByName(chatType: String, name: String): LiveData<List<WatchChatInfo>> {
            return AppDatabase.getInstance().watchChatDao().queryChatListByName(chatType, name)
        }

        fun insertChat(chatInfo: WatchChatInfo) {
            val block: suspend CoroutineScope.() -> Unit = {
                val tableName = getRemoteTable()
                val objectId = try {
                    BChatInfo(
                        chatInfo.type,
                        chatInfo.timestamp,
                        chatInfo.chatName,
                        chatInfo.content
                    ).apply {
                        this.tableName = tableName
                    }.saveSync()
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                AppDatabase.getInstance().watchChatDao().insertChatInfo(chatInfo.apply { bMobId = objectId })
            }
            GlobalScope.launch {
                block()
            }
        }

        fun deleteChat(chatInfo: WatchChatInfo, callback: () -> Unit) {
            val block: suspend CoroutineScope.() -> Unit = {
                AppDatabase.getInstance().watchChatDao().deleteChatInfo(chatInfo.timestamp)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
            GlobalScope.launch {
                block()
            }
        }

        fun deleteChatByName(chatInfo: WatchChatInfo, callback: () -> Unit) {
            val block: suspend CoroutineScope.() -> Unit = {
                AppDatabase.getInstance().watchChatDao().deleteChatName(chatInfo.type, chatInfo.chatName)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
            GlobalScope.launch {
                block()
            }
        }

        fun deleteChatByName(chatType: String, name: String, callback: () -> Unit) {
            val block: suspend CoroutineScope.() -> Unit = {
                AppDatabase.getInstance().watchChatDao().removeChatByName(chatType, name)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
            GlobalScope.launch {
                block()
            }
        }

        fun clearChat(chatType: String, callback: () -> Unit) {
            val block: suspend CoroutineScope.() -> Unit = {
                AppDatabase.getInstance().watchChatDao().clearChat(chatType)
                withContext(Dispatchers.Main) {
                    callback()
                }
            }
            GlobalScope.launch {
                block()
            }
        }
    }
}