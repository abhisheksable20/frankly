package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.parse.ParseUser;

// Model for saved searched users
@Entity(tableName = "searchedUsers")
public class SearchedUsers {

    @PrimaryKey(autoGenerate = true)
    private int primaryKey;

    @ColumnInfo(name = "parseUser")
    private ParseUser parseUser;

    public SearchedUsers(int primaryKey, ParseUser parseUser) {
        this.primaryKey = primaryKey;
        this.parseUser = parseUser;
    }

    @Ignore
    public SearchedUsers(ParseUser parseUser) {
        this.parseUser = parseUser;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public ParseUser getParseUser() {
        return parseUser;
    }
}
