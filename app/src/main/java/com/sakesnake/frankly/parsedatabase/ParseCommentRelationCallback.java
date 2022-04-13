package com.sakesnake.frankly.parsedatabase;

import com.parse.ParseObject;

@FunctionalInterface
// This interface will the post with only comment relation
public interface ParseCommentRelationCallback {
    void getCommentRelationObject(ParseObject objectWithCommentRelation);
}
