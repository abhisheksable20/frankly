package com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback;

import com.parse.ParseUser;

@FunctionalInterface
public interface ImagesHomeAdapterCallback {
    // Here position means content type
    void imageSelected(final int position, final ParseUser parseUser);
}
