package com.sakesnake.frankly.parsedatabase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Contains all static methods
public class FetchLatestData {

    // Fetching latest parse user data
    public static void fetchLatestUserData(@NonNull final ParseUser parseUser, UserDataCallback userDataCallback){
        parseUser.fetchInBackground((object, e) -> {
            if (e == null){
                if (userDataCallback != null)
                    userDataCallback.latestUserData((ParseUser) object,null);
            }
            else{
                if (userDataCallback != null)
                    userDataCallback.latestUserData(null,e);
            }
        });
    }

    public static void fetchCommentRelation(final String objectId,final ParseCommentRelationCallback commentRelationCallback){
        final List<String> keys =  new ArrayList<>();
        keys.add(ParseConstants.POST_COMMENTS_LIST);
        final ParseQuery<ParseObject> postQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        postQuery.whereEqualTo(ParseConstants.POST_OBJECT_ID,objectId);
        postQuery.selectKeys(keys);
        postQuery.getFirstInBackground((object, e) -> {
            if (e == null){
                if (commentRelationCallback != null)
                    commentRelationCallback.getCommentRelationObject(object);
            }
            else
                Log.e(Constants.TAG, "Cannot fetch the post to get comments: " + objectId+" error-->: "+e.getMessage());
        });
    }

    // Fetching the post comments
    public static void fetchPostComments(final ParseObject parseObject, int limit,int skip, ParseObjectUserListCallback parseObjectUserListCallback){
        final ParseQuery<ParseObject> commentQuery = parseObject.getRelation(ParseConstants.POST_COMMENTS_LIST).getQuery();
        commentQuery.include(ParseConstants.COMMENT_OWNER);
        commentQuery.setLimit(limit);
        commentQuery.setSkip(skip);
        commentQuery.findInBackground((objects, e) -> {
            if (e == null){
                if (parseObjectUserListCallback != null)
                    parseObjectUserListCallback.postObjectUserLists(objects,null);
            }
            else
                Log.e(Constants.TAG, "Cannot fetch post comments: "+e.getMessage());
        });
    }

    public static void fetchPostLikes(final ParseObject parseObject, int limit, int skip, ParseObjectUserListCallback parseObjectUserListCallback){
        List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.PROFILE_USERNAME);
        keys.add(ParseConstants.PROFILE_PIC);
        keys.add(ParseConstants.PROFILE_ORIGINAL_NAME);
        final ParseRelation<ParseUser> userParseRelation = parseObject.getRelation(ParseConstants.POST_LIKES_LIST);
        final ParseQuery<ParseUser> userParseQuery = userParseRelation.getQuery();
        userParseQuery.selectKeys(keys);
        userParseQuery.setLimit(limit);
        userParseQuery.setSkip(skip);
        userParseQuery.findInBackground((objects, e) -> {
            if (e == null)
                parseObjectUserListCallback.postObjectUserLists(null,objects);
        });
    }




    // Retrieving parse object remaining fields from network
    public static void fetchRemainingParseObjectFields(@NonNull final ParseObject parseObject, ParseObjectCallback parseObjectCallback){
        parseObject.fetchIfNeededInBackground((object, e) -> {
            if (parseObjectCallback != null) {
                if (e == null)
                    parseObjectCallback.parseObjectCallback(object, null);
                else
                    parseObjectCallback.parseObjectCallback(null, e);
            }
        });
    }

    // Is i liked this current post
    public static void isILikedThisPost(final String postObjectId,BooleanParseCallback booleanParseCallback){
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.POST_OBJECT_ID_KEY,postObjectId);
        ParseCloud.callFunctionInBackground(ParseConstants.IS_I_LIKED_THIS_POST,hashMap,(isILiked,e) ->{
            if (booleanParseCallback != null && e == null)
                booleanParseCallback.isTrueFromServer((boolean) isILiked, e);
        });
    }

    // Like or Dislike the post according to the situation
    public static void likeOrDislikePost(final String postObjectId, BooleanParseCallback booleanParseCallback){
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.POST_OBJECT_ID_KEY,postObjectId);
        ParseCloud.callFunctionInBackground(ParseConstants.LIKE_DISLIKE_POST,hashMap,(postLiked,e) -> {
           if (booleanParseCallback != null  &&  e == null)
               booleanParseCallback.isTrueFromServer((boolean) postLiked,e);
        });
    }



}
