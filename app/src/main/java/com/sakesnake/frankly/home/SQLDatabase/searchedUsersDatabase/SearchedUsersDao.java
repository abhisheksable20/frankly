package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SearchedUsersDao {
    @Query("SELECT * FROM searchedUsers")
    List<SearchedUsers> getSavedUsers();

    @Delete
    void deleteUser(SearchedUsers user);

    @Insert
    void insertUser(SearchedUsers user);
}
