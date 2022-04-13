package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sakesnake.frankly.App;

import java.util.List;

public class AccessStoredUsersDB {

    // Getting stored searched users from SQL database
    public static void getStoredUsers(@NonNull Context appContext, GetUsersCallback getUsersCallback){
        App.getCachedExecutorService().execute(()->{
            List<SearchedUsers> userList = SearchedUsersDatabase.getInstance(appContext).searchedUsersDao().getSavedUsers();
            if (getUsersCallback != null)
                getUsersCallback.getCallback(userList);
        });
    }

    // Inserting parse user in SQL database
    public static void insertParseUser(@NonNull Context appContext,SearchedUsers searchedUsers){
        App.getCachedExecutorService().execute(()-> SearchedUsersDatabase.getInstance(appContext).searchedUsersDao().insertUser(searchedUsers));
    }

    // Deleting stored parse user from SQL database
    public static void deleteStoredUser(@NonNull Context appContext, SearchedUsers searchedUsers){
        App.getCachedExecutorService().execute(() -> SearchedUsersDatabase.getInstance(appContext).searchedUsersDao().deleteUser(searchedUsers));
    }

}
