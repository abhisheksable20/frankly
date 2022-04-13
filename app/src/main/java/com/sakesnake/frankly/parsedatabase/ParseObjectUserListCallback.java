package com.sakesnake.frankly.parsedatabase;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public interface ParseObjectUserListCallback {
    void postObjectUserLists(List<ParseObject> postComments, List<ParseUser> parseUsers);
}
