package com.cabe.app.watch.db

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */
@Entity(tableName = "watch_chat_list", primaryKeys = ["timestamp"])
class WatchChatInfo(
        @ColumnInfo(name = "chat_type") val type: String,
        @ColumnInfo(name = "timestamp") val timestamp: Long,
        @ColumnInfo(name = "chat_name") val chatName: String,
        @ColumnInfo(name = "content") val content: String
        )