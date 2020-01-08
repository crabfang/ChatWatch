package com.cabe.app.watch.db

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*

class DBHelper {
    companion object {
        fun queryChatListAll(chatType: String): LiveData<List<WatchChatInfo>> {
            return AppDatabase.getInstance().watchChatDao().queryAllChatList(chatType)
        }

        fun queryChatListByName(chatType: String, name: String): LiveData<List<WatchChatInfo>> {
            return AppDatabase.getInstance().watchChatDao().queryChatListByName(chatType, name)
        }

        fun insertChat(chatInfo: WatchChatInfo) {
            val block: suspend CoroutineScope.() -> Unit = {
                AppDatabase.getInstance().watchChatDao().insertChatInfo(chatInfo)
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