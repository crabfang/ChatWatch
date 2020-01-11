package com.cabe.app.watch.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */
@Dao
interface WatchChatWXDao {
    @Query("SELECT * FROM watch_chat_list WHERE chat_type=:chatType GROUP BY chat_name ORDER BY timestamp DESC")
    fun queryAllChatList(chatType: String) : LiveData<List<WatchChatInfo>>

    @Query("SELECT * FROM watch_chat_list WHERE chat_type=:chatType AND chat_name=:chatName ORDER BY timestamp DESC")
    fun queryChatListByName(chatType: String, chatName: String) : LiveData<List<WatchChatInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatInfo(info: WatchChatInfo)

    @Query("DELETE FROM watch_chat_list WHERE timestamp=:timestamp")
    fun deleteChatInfo(timestamp: Long)

    @Query("DELETE FROM watch_chat_list WHERE chat_type=:chatType AND chat_name=:chatName")
    fun deleteChatName(chatType: String, chatName: String)

    @Query("DELETE FROM watch_chat_list WHERE chat_type=:chatType AND chat_name=:chatName")
    fun removeChatByName(chatType: String, chatName: String)

    @Query("DELETE FROM watch_chat_list WHERE chat_type=:chatType")
    fun clearChat(chatType: String)
}