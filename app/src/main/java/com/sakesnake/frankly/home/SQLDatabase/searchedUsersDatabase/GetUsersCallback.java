package com.sakesnake.frankly.home.SQLDatabase.searchedUsersDatabase;

import java.util.List;

@FunctionalInterface
public interface GetUsersCallback {
    void getCallback(List<SearchedUsers> userList);
}
