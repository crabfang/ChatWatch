package com.cabe.app.watch.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import cn.bmob.v3.BmobObject

/**
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */
@Entity(tableName = "watch_chat_list", primaryKeys = ["timestamp"])
@Keep
data class WatchChatInfo(
        @ColumnInfo(name = "chat_type") val type: String,
        @ColumnInfo(name = "timestamp") val timestamp: Long,
        @ColumnInfo(name = "chat_name") val chatName: String,
        @ColumnInfo(name = "content") val content: String,
        @ColumnInfo(name = "b_mob_id") var bMobId: String
)

@Keep data class BChatInfo(
        var type: String,
        var timestamp: Long,
        var chatName: String,
        var content: String
): BmobObject()