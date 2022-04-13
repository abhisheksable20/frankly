package com.sakesnake.frankly.home.postfeeds.allPostFeeds;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sakesnake.frankly.Constants;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
    This class works in LIFO structure last in first out
    main purpose of class is supply an arguments to fragment that are showing
    posts and also this class is used in UserProfileFragment, PostCommentFragment And PostLikesFragment to provide them arguments
    .............
    This class overcome the problem with saveInstanceState that we cannot save the state of
    fragment manually but with this class will store Arguments even if the same fragment is called multiple times.
 **/
public class FeedsArgsViewModel extends ViewModel {

    private MutableLiveData<List<Bundle>> fragArgs;

    private MutableLiveData<List<Bundle>> userProfileArgs;

    private MutableLiveData<List<Bundle>> fullContentViewModel;

    private MutableLiveData<List<Bundle>> postCommentsLiveData;

    private MutableLiveData<List<Bundle>> postLikesLiveData;

    private MutableLiveData<List<Bundle>> profileSupportersArgs;


    //--------------------------- User Profile Fragments Work -----------------------//
    // Adding user profile argument
    public void addUserProfileArgs(final Bundle bundle){
        if (userProfileArgs == null)
            userProfileArgs = new MutableLiveData<>(new ArrayList<>());

        if (userProfileArgs.getValue() != null)
            userProfileArgs.getValue().add(bundle);
    }

    // Getting last element of userProfileArgs
    public Bundle getUserProfArg(){
        if (userProfileArgs == null)
            return null;

        int lastPosition;
        if (userProfileArgs.getValue() != null  &&  (lastPosition = userProfileArgs.getValue().size()) > 0)
            return userProfileArgs.getValue().get(lastPosition - 1);

        return null;
    }

    // Delete the last element
    public void deleteUserProfArg(){
        if (userProfileArgs == null)
            return;

        int lastPosition;
        if (userProfileArgs.getValue() != null  &&  (lastPosition = userProfileArgs.getValue().size()) >  0)
            userProfileArgs.getValue().remove(lastPosition - 1);
    }

    // Add extra data to inside last bundle object
    public void addExtraInLastElement(final ParseUser parseUser,final boolean isLikedByCurrent, final boolean isInWatchlist){
        if (userProfileArgs == null)
            return;

        int lastPosition;
        if (userProfileArgs.getValue() != null  &&  (lastPosition = userProfileArgs.getValue().size()) > 0){
            Bundle bundle = userProfileArgs.getValue().get(lastPosition - 1);
            bundle.putParcelable(Constants.BUNDLE_SAVED_PARSE_USER,parseUser);
            bundle.putBoolean(Constants.BUNDLE_IS_WATCHING,isInWatchlist);
            bundle.putBoolean(Constants.BUNDLE_IS_LIKED_USER,isLikedByCurrent);
            userProfileArgs.getValue().set(lastPosition - 1,bundle);
        }
    }



    // --------------------------- Post Feeds Fragments Work ------------------------//
    // Adding fragment argument
    public void addFragArg(final Bundle bundle){
        if (fragArgs == null)
            fragArgs = new MutableLiveData<>(new ArrayList<>());

        if (fragArgs.getValue() != null)
            fragArgs.getValue().add(bundle);
    }

    // Getting last element of fragArgs
    public Bundle getArg(){
        if (fragArgs == null)
            return null;

        int lastPosition;
        if (fragArgs.getValue() != null  &&  (lastPosition = fragArgs.getValue().size()) > 0)
            return fragArgs.getValue().get(lastPosition - 1);

        return null;
    }

    // Delete the last element
    public void deleteLastArg(){
        if (fragArgs == null)
            return;

        int lastPosition;
        if (fragArgs.getValue() != null  &&  (lastPosition = fragArgs.getValue().size()) > 0) {
            fragArgs.getValue().remove(lastPosition - 1);
        }
    }


    // Modifying the last element
    // Just adding recycler view list and current position
    public void addParseObjectInLast(ArrayList<ParseObject> parseObjectList, int scrollPosition){
        if (fragArgs == null)
            return;

        int lastPosition;
        if (fragArgs.getValue() != null  &&  (lastPosition = fragArgs.getValue().size()) > 0){
            Bundle bundle = fragArgs.getValue().get(lastPosition - 1);
            bundle.putParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST,parseObjectList);
            fragArgs.getValue().set(lastPosition - 1, bundle);
        }
    }

    public void addCustomObjectInLast(ArrayList<CustomParseObject> customParseObjects, int scrollPosition){
        if (fragArgs == null)
            return;

        int lastPosition;
        if (fragArgs.getValue() != null  &&  (lastPosition = fragArgs.getValue().size()) > 0){
            Bundle bundle = fragArgs.getValue().get(lastPosition - 1);
            bundle.putParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST,customParseObjects);
            fragArgs.getValue().set(lastPosition - 1,bundle);
        }
    }


    // -------------------- Full feeds fragments View model ------------- //
    // Adding ParseObject in view model
    public void addFullFeedsArg(final Bundle bundle){
        if (fullContentViewModel == null)
            fullContentViewModel = new MutableLiveData<>(new ArrayList<>());

        if (fullContentViewModel.getValue() != null)
            fullContentViewModel.getValue().add(bundle);
    }

    public Bundle getLastFullFeedArg(){
        if (fullContentViewModel == null)
            return null;

        int lastPosition;
        if (fullContentViewModel.getValue() != null  &&  (lastPosition = fullContentViewModel.getValue().size()) > 0)
            return fullContentViewModel.getValue().get(lastPosition - 1);

        return null;
    }

    // Delete last element
    public void deleteLastFullFeedArg(){
        if (fullContentViewModel == null)
            return;

        int lastPosition;
        if (fullContentViewModel.getValue() != null  &&  (lastPosition = fullContentViewModel.getValue().size()) > 0)
            fullContentViewModel.getValue().remove(lastPosition - 1);
    }

    // Replacing last parse object to fully fetched parse object
    public void modifyLastParseObj(final ParseObject newParseObj, final boolean isLiked){
        if (fullContentViewModel == null)
            return;

        int lastPosition;
        if (fullContentViewModel.getValue() != null  &&  (lastPosition = fullContentViewModel.getValue().size()) > 0){
            Bundle bundle = fullContentViewModel.getValue().get(lastPosition - 1);
            bundle.putParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT,newParseObj);
            bundle.putBoolean(Constants.BUNDLE_IS_LIKED_USER,isLiked);
            fullContentViewModel.getValue().set(lastPosition - 1,bundle);
        }
    }

    //---------- Working with post comments ------------//
    public void insertPostCommentStartingArg(final Bundle bundle){
        if (postCommentsLiveData == null)
            postCommentsLiveData = new MutableLiveData<>(new ArrayList<>());

        if (postCommentsLiveData.getValue() != null)
            postCommentsLiveData.getValue().add(bundle);
    }

    public Bundle getPostCommentLastArg(){
        if (postCommentsLiveData == null)
            return null;

        int lastPosition;
        if (postCommentsLiveData.getValue() != null  &&  (lastPosition = postCommentsLiveData.getValue().size()) > 0)
            return postCommentsLiveData.getValue().get(lastPosition - 1);

        return null;
    }

    public void deleteLastPostCommentArg(){
        if (postCommentsLiveData == null)
            return;

        int lastPosition;
        if (postCommentsLiveData.getValue() != null  && (lastPosition = postCommentsLiveData.getValue().size()) > 0)
            postCommentsLiveData.getValue().remove(lastPosition - 1);
    }

    public void modifyPostCommentArg(final ParseObject objectWithCommentRel, final ArrayList<ParseObject> postComments){
        if (postCommentsLiveData == null)
            return;

        int lastPosition;
        if (postCommentsLiveData.getValue() != null  &&  (lastPosition = postCommentsLiveData.getValue().size()) > 0){
            Bundle bundle = postCommentsLiveData.getValue().get(lastPosition - 1);
            bundle.putParcelable(Constants.BUNDLE_SAVED_POST_COMMENT,objectWithCommentRel);
            if (postComments != null)
                bundle.putParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST,postComments);
            postCommentsLiveData.getValue().set(lastPosition - 1,bundle);
        }
    }

    // --------------------------- Working with post likes view model ---------------------------------------//

    // Inserting starting argument for post likes fragment
    public void insertPostLikesArgStarting(final Bundle bundle){
        if (postLikesLiveData == null)
            postLikesLiveData = new MutableLiveData<>(new ArrayList<>());

        if (postLikesLiveData.getValue() != null)
            postLikesLiveData.getValue().add(bundle);
    }

    // Getting the last argument from post likes view model
    public Bundle getPostLikesLastArg(){
        if (postLikesLiveData == null)
            return null;

        if (postLikesLiveData.getValue() != null)
            return postLikesLiveData.getValue().get(postLikesLiveData.getValue().size() - 1);

        return null;
    }

    // Deleting the last argument from post likes view model
    public void deleteLastPostLikeArg(){
        if (postLikesLiveData == null)
            return;

        int lastPosition;
        if (postLikesLiveData.getValue() != null  &&  (lastPosition = postLikesLiveData.getValue().size()) > 0)
            postLikesLiveData.getValue().remove(lastPosition - 1);
    }

    // Modifying post likes last argument
    public void modifyPostLikesLastArgs(final ParseObject postWithLikeRel, final ArrayList<ParseUser> parseUsers){
        if (postWithLikeRel == null)
            return;

        int lastPosition;
        if (postLikesLiveData.getValue() != null && (lastPosition = postLikesLiveData.getValue().size()) > 0){
            final Bundle bundle = postLikesLiveData.getValue().get(lastPosition - 1);
            bundle.putParcelable(Constants.BUNDLE_SAVED_POST_LIKES,postWithLikeRel);
            if (parseUsers != null)
                bundle.putParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST,parseUsers);

            postLikesLiveData.getValue().set(lastPosition - 1,bundle);
        }
    }


    //---------------------------------- Profile supporters fragment arguments ------------------------------//

    // insert supporters fragment argument (Starting args)
    public void insertSupporterFragArg(final Bundle bundle){
        if (profileSupportersArgs == null)
            profileSupportersArgs = new MutableLiveData<>(new ArrayList<>());

        if (profileSupportersArgs.getValue() != null)
            profileSupportersArgs.getValue().add(bundle);
    }

    // Get last element from supporter fragment argument list
    public Bundle getLastSupportFragArg(){
        if (profileSupportersArgs == null)
            return null;

        if (profileSupportersArgs.getValue() != null)
            return profileSupportersArgs.getValue().get(profileSupportersArgs.getValue().size() - 1);

        return null;
    }

    // Delete last element from supporter fragment argument
    public void deleteLastSupportFragArg(){
        if (profileSupportersArgs == null)
            return;

        if (profileSupportersArgs.getValue() != null  &&  profileSupportersArgs.getValue().size() > 0)
            profileSupportersArgs.getValue().remove(profileSupportersArgs.getValue().size() - 1);
    }

    // Modifying last support fragment argument
    public void modifySupportFragLastArg(final ArrayList<ParseUser> parseUsers){
        if (profileSupportersArgs == null || parseUsers == null)
            return;

        int lastPosition;
        if (profileSupportersArgs.getValue() != null  &&  (lastPosition = profileSupportersArgs.getValue().size()) > 0){
            final Bundle bundle = profileSupportersArgs.getValue().get(lastPosition - 1);
            bundle.putParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST,parseUsers);
            profileSupportersArgs.getValue().set(lastPosition - 1,bundle);
        }
    }

    public void clearAllArgs(){
        fragArgs = null;
        userProfileArgs = null;
        fullContentViewModel = null;
        postCommentsLiveData = null;
        postLikesLiveData = null;
        profileSupportersArgs = null;

    }

}
