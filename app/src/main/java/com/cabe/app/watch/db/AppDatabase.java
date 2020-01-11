package com.cabe.app.watch.db;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cabe.app.watch.MyApp;

/*
 * 业务说明
 *
 * @author cf
 * @since v1.0
 */
@Database(entities = {
        WatchChatInfo.class
}, version = 2)
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
                    .addMigrations(
                            migration_1_2
                    )
                    .build();
        }
    }
    private static Migration migration_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE watch_chat_list ADD COLUMN b_mob_id TEXT NOT NULL default ''");
        }
    };
}

