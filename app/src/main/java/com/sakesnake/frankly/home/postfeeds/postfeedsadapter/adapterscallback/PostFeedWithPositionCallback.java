package com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback;

import com.sakesnake.frankly.home.postfeeds.allPostFeeds.CustomParseObject;

@FunctionalInterface
public interface PostFeedWithPositionCallback {
    void feedCallbackWithPosition (CustomParseObject customParseObject, int position, int work);
}
