package com.cabe.app.watch.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cabe.app.watch.MyApp;

/*
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */
@Database(entities = {
        WatchChatInfo.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WatchChatWXDao watchChatDao();

    public static AppDatabase getInstance() {
        return Holder.sInstance;
    }

    private static class Holder {
        private static AppDatabase sInstance;

        static {
            sInstance = Room.databaseBuilder(MyApp.Companion.getInstances(), AppDatabase.class, "watch_chat_list.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
    }
}

