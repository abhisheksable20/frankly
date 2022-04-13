package com.sakesnake.frankly.home.bottomnavsearch.searchprofiles;

import com.parse.ParseUser;

@FunctionalInterface
public interface SearchedProfileCallback {
    void profileSelected(ParseUser user);
}
