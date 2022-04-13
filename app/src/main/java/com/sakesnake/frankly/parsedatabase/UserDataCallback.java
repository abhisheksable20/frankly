package com.sakesnake.frankly.parsedatabase;

import com.parse.ParseException;
import com.parse.ParseUser;

@FunctionalInterface
public interface UserDataCallback {
    void latestUserData(ParseUser parseUser, ParseException exception);
}
