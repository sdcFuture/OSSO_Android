package com.futureelectronics.osso.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Created by Kyle Harman on 12/4/2018.
 */
@Database(entities = {Osso.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract OssoDao ossoDao();

    private static volatile AppDatabase INSTANCE;

    static AppDatabase getDatabase(final Context context){
        if(INSTANCE == null) {
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
