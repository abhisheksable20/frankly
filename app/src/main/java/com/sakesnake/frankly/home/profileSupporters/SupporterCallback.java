package com.sakesnake.frankly.home.profileSupporters;

import com.parse.ParseUser;

@FunctionalInterface
public interface SupporterCallback {
    void callbackWithDeleteOption(ParseUser parseUser, boolean deleteUser);
}
