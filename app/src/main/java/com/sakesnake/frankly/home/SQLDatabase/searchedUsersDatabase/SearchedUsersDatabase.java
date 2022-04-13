package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SearchedUsers.class}, exportSchema = false, version = 1)
@TypeConverters(ParseUserTypeConverter.class)
public abstract class SearchedUsersDatabase extends RoomDatabase{

    private static final Object LOCK = new Object();

    private static final String DATABASE_NAME = "searchedParseUsers";

    private static volatile SearchedUsersDatabase searchedUsersDatabase;

    public static SearchedUsersDatabase getInstance(Context appContext){
        if (searchedUsersDatabase == null){
            synchronized (LOCK){
                searchedUsersDatabase = Room
                        .databaseBuilder(appContext,SearchedUsersDatabase.class,DATABASE_NAME)
                        .build();
            }
        }
        return searchedUsersDatabase;
    }

    public abstract SearchedUsersDao searchedUsersDao();
}
