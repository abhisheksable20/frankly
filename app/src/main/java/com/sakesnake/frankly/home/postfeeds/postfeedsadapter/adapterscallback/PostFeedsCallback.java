package com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback;

import com.parse.ParseObject;
import com.parse.ParseUser;

@FunctionalInterface
public interface PostFeedsCallback {
    void selectedPost(final int currentPos,final ParseObject object, final ParseUser user);
}
