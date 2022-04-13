package com.sakesnake.frankly.home.usersprofiledisplay;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.MyUploads;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.MyUploadsAdapter;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.UploadsAdapterCallback;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.home.profileSupporters.ProfileSupportersFragment;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.appbar.AppBarLayout;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

/*
   This fragment will only runs on three arguments. But third is optional here
   If no arguments found this fragment will not show anything.

   1 arg:- ParseUser that is coming from another fragments as data
   2 arg:- ParseUser that is stored in this fragments onSavedInstanceState
   3 arg:- Is this parse user is liked by Parse.currentUser() then this argument will be set to true

   If there is no 1st argument then it is assumed that the user has just navigated back from another fragment,
   and then 2nd argument is used to display data in this fragment.

   Note:- Here 2nd argument is just a 1st argument which is stored in onSavedInstanceState.
*/

public class UsersProfileFragment extends Fragment implements
        AppBarLayout.OnOffsetChangedListener, UploadsAdapterCallback {

    private Context mContext;

    private View rootView;

    private final ArrayList<MyUploads> myUploadsArrayList = new ArrayList<>();

    private boolean likedByCurrentUser = false;

    private boolean isWatchingByCurrentUser = false;

    private ParseUser parseUser;

    private SessionErrorViewModel sessionErrorViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayout watchersLinearLayout,watchingLinearLayout;

    private AppBarLayout appBarLayout;

    private ImageView profileBannerImageView, profilePicImageView, likeTheProfileImageView;

    private Button addUserToWatchlistBtn;

    private TextView usernameTextView, originalNameTextView, profileDescriptionTextView, tWatchersTextView, tWatchingTextView,
            tLikesTextView, tLikedProfileTextView;

    private RecyclerView userUploadsRecyclerView;

    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Getting view models
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        rootView = inflater.inflate(R.layout.fragment_users_profile, container, false);

        // init views
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.user_profile_swipe_to_refresh_layout);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.fragment_app_bar_layout);
        userUploadsRecyclerView = (RecyclerView) rootView.findViewById(R.id.user_profile_uploads_recycler_view);
        profileBannerImageView = (ImageView) rootView.findViewById(R.id.user_profile_banner_image_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.user_profile_profile_pic_image_view);
        likeTheProfileImageView = (ImageView) rootView.findViewById(R.id.like_user_profile_image_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_text_view);
        originalNameTextView = (TextView) rootView.findViewById(R.id.profile_original_name_text_view);
        profileDescriptionTextView = (TextView) rootView.findViewById(R.id.profile_description_text_view);
        tWatchersTextView = (TextView) rootView.findViewById(R.id.total_watchers_text_view);
        tWatchingTextView = (TextView) rootView.findViewById(R.id.total_watching_text_view);
        tLikesTextView = (TextView) rootView.findViewById(R.id.total_likes_text_view);
        tLikedProfileTextView = (TextView) rootView.findViewById(R.id.total_liked_profile_text_view);
        watchersLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_watchers_linear_layout);
        watchingLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_watch_list_linear_layout);
        addUserToWatchlistBtn = (Button) rootView.findViewById(R.id.add_user_to_watchlist_button);


        // Do not change the placement of object in the below array list instead append the objects on the next position
        // setting the arraylist
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_images_my_uploads), mContext.getColor(R.color.yellow_ideas),0));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_blog_my_uploads), mContext.getColor(R.color.orange_memes_and_comedy),1));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_dreams_my_uploads), mContext.getColor(R.color.black),2));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_quotes_thoughts_my_uploads), mContext.getColor(R.color.blue_thoughts_and_quotes),3));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_news_and_updates_my_uploads),mContext.getColor(R.color.green_motivate_yourself),4));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_memes_and_comedy_my_uploads), mContext.getColor(R.color.orange_memes_and_comedy),5));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_story_my_uploads), mContext.getColor(R.color.voilet_story),6));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_poem_my_uploads), mContext.getColor(R.color.pink_poems),7));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_asked_question_my_uploads), mContext.getColor(R.color.brown_give_them_answer),9));
        myUploadsArrayList.add(new MyUploads(getString(R.string.my_ideas_my_uploads), mContext.getColor(R.color.yellow_ideas),10));

        // If there is Fresh_Parse_User arg then first update that ParseUser from the server.
        // If there is Saved_Parse_User arg then there is no need to update the parse user show as it is.
        initArguments(feedsArgsViewModel.getUserProfArg());

        // Setting up user upload recycler view
        MyUploadsAdapter myUploadsAdapter = new MyUploadsAdapter(mContext, myUploadsArrayList, this);
        userUploadsRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        userUploadsRecyclerView.setAdapter(myUploadsAdapter);


        return rootView;
    }

    private void initArguments(final Bundle bundle) {
        if (bundle == null)
            return;

        swipeRefreshLayout.setRefreshing(true);

        // Do not update this parse user
        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_USER)) {
            parseUser = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_USER);
            likedByCurrentUser = bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER);
            isWatchingByCurrentUser = bundle.getBoolean(Constants.BUNDLE_IS_WATCHING);
            animateLikeButton(likedByCurrentUser);
            animateWatchlistButton(isWatchingByCurrentUser);
            setViewWithData(parseUser);
        }
        // Update this parse user
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_USER)) {
            parseUser = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_USER);
            updateTheUserData(parseUser,false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appBarLayout.addOnOffsetChangedListener(this);

        // Adding or removing this user from our watchlist
        addUserToWatchlistBtn.setOnClickListener(view1 -> addOrRemoveFromWatchlist());

        // Navigating the user to supporter fragment
        watchersLinearLayout.setOnClickListener(view1 -> navigateProfileSupporterFrag(ProfileSupportersFragment.QUERY_WATCHERS));
        watchingLinearLayout.setOnClickListener(view1 -> navigateProfileSupporterFrag(ProfileSupportersFragment.QUERY_WATCHING));

        // Liking or disliking this user
        likeTheProfileImageView.setOnClickListener(view1 -> likeOrDislikeUser());

        swipeRefreshLayout.setOnRefreshListener(() -> updateTheUserData(parseUser,true));

    }

    private void navigateProfileSupporterFrag(final int queryConstant){
        if (parseUser == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_SAVED_PARSE_USER,parseUser);
        bundle.putInt(Constants.BUNDLE_PROFILE_QUERY_CONSTANT,queryConstant);
        feedsArgsViewModel.insertSupporterFragArg(bundle);
        navController.navigate(R.id.action_usersProfileFragment_to_profileSupportersFragment);
    }

    // Add or remove this user from watchlist
    private void addOrRemoveFromWatchlist(){
        if (parseUser == null)
            return;

        animateWatchlistButton(isWatchingByCurrentUser = !isWatchingByCurrentUser);

        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.USER_OBJECT_ID_KEY, parseUser.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.ADD_OR_REMOVE_WATCHLIST,hashMap,(object,cloudError)->{
             if (cloudError != null)
                 Log.e(Constants.TAG, "addOrRemoveFromWatchlist cloud error----->: "+cloudError.getMessage());
        });
    }

    // Like or dislike this user
    private void likeOrDislikeUser(){
        if (parseUser == null)
            return;

        animateLikeButton(likedByCurrentUser = !likedByCurrentUser);
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.USER_OBJECT_ID_KEY,parseUser.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.LIKE_OR_DISLIKE_USER,hashMap,(object, e)->{
            if (e != null)
                Log.e(Constants.TAG, "likeOrDislikeUser failed to call cloud: ERROR---> "+e.getMessage());
            else
                Log.d(Constants.TAG, "likeOrDislikeUser cloud code called successfully");
        });
    }


    // Animating the like button if it liked by the current user
    private void animateLikeButton(boolean isLiked) {
        AnimatedVectorDrawable vectorDrawable = (AnimatedVectorDrawable) likeTheProfileImageView.getDrawable();
        if (isLiked)
            vectorDrawable.start();
        else
            vectorDrawable.reset();
    }


    private void animateWatchlistButton(boolean isWatchedProfile){
        if (isWatchedProfile){
            addUserToWatchlistBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
            addUserToWatchlistBtn.setText(getString(R.string.my_watch_list));
        }
        else{
            addUserToWatchlistBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
            addUserToWatchlistBtn.setText(getString(R.string.add_user_to_watchlist));
        }
    }

    // Here position is content type
    @Override
    public void getContentType(int position) {
        if (parseUser == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_CONTENT_TYPE, position);
        bundle.putInt(Constants.BUNDLE_USER_CALL, Constants.USER_CALL);
        bundle.putParcelable(Constants.BUNDLE_PARSE_USER, parseUser);
        feedsArgsViewModel.addFragArg(bundle);

        switch (position) {
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE: {
                navController.navigate(R.id.action_usersProfileFragment_to_imagesFeedFragment);
                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:
            case Constants.UPLOAD_GIVE_QUESTION:
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG: {
                navController.navigate(R.id.action_usersProfileFragment_to_writingFeedsFragment);
                break;
            }
        }
    }


    // Updating the given user with his latest data
    private void updateTheUserData(final ParseUser user, final boolean refreshObj) {
        if (user == null)
            return;

        userProfileExtras(user.getObjectId());

        if (!refreshObj) {
            swipeRefreshLayout.setEnabled(false);
            user.fetchIfNeededInBackground((object, e) -> {
                swipeRefreshLayout.setEnabled(true);
                afterFetching((ParseUser) object,e);
            });
        }
        else
            user.fetchInBackground((object, e) -> afterFetching((ParseUser) object,e));
    }

    // Work after fetching object from parse
    private void afterFetching(final ParseUser parseUser, final ParseException e){
        if (e == null) {
            this.parseUser =  parseUser;
            setViewWithData(parseUser);
        } else if (e.getCode() == ParseException.INVALID_SESSION_TOKEN)
            sessionErrorViewModel.isSessionExpired(true);
    }


    // Checking is this user is liked by the current user or not
    // And also checking that do i watch this user or not
    // This is cloud code function
    private void userProfileExtras(final String objectId) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.USER_OBJECT_ID_KEY, objectId);
        ParseCloud.callFunctionInBackground(ParseConstants.USER_PROFILE_EXTRA_DETAILS, hashMap, (jsonObjInHashMap, e) -> {
            if (e == null) {
                try {
                    final HashMap<String, Boolean> booleanHashMap = (HashMap<String, Boolean>) jsonObjInHashMap;
                    isWatchingByCurrentUser = booleanHashMap.get(ParseConstants.IS_USER_IN_WATCHLIST);
                    likedByCurrentUser = booleanHashMap.get(ParseConstants.IS_LIKED_PROFILE);
                } catch (NullPointerException | ClassCastException nullPointerException) {
                    Log.e(Constants.TAG, "UserProfileExtras HASHMAP ERROR------>: "+nullPointerException.getMessage());
                }
            }
            else
                Log.e(Constants.TAG, "userProfileExtras cloud error------>: "+e.getMessage());

            animateLikeButton(likedByCurrentUser);
            animateWatchlistButton(isWatchingByCurrentUser);

        });
    }


    // Setting view with parse data
    private void setViewWithData(@NonNull final ParseUser user) {
        swipeRefreshLayout.setRefreshing(false);

        // Setting profile main details
        usernameTextView.setText(user.getUsername());
        originalNameTextView.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));
        tWatchersTextView.setText(getString(R.string.number_to_string, user.getInt(ParseConstants.PROFILE_TOTAL_WATCHERS)));
        tWatchingTextView.setText(getString(R.string.number_to_string, user.getInt(ParseConstants.PROFILE_TOTAL_WATCHING)));
        tLikesTextView.setText(getString(R.string.number_to_string, user.getInt(ParseConstants.PROFILE_TOTAL_LIKES)));
        tLikedProfileTextView.setText(getString(R.string.number_to_string, user.getInt(ParseConstants.PROFILE_TOTAL_LIKED_PROFILE)));

        // Getting and setting Profile description
        String profileDescription = user.getString(ParseConstants.PROFILE_DESCRIPTION);
        if (profileDescription != null && !profileDescription.equals("")) {
            profileDescriptionTextView.setVisibility(View.VISIBLE);
            profileDescriptionTextView.setText(DocNormalizer.htmlToNormal(profileDescription));
        } else {
            profileDescriptionTextView.setVisibility(View.GONE);
        }

        // Setting profilePic and profileBanner
        final ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null) {
            Glide.with(mContext).load(profilePic.getUrl()).centerCrop().into(profilePicImageView);
        }

        final ParseFile profileBanner = user.getParseFile(ParseConstants.PROFILE_BANNER);
        if (profileBanner != null) {
            Glide.with(mContext)
                    .load(profileBanner.getUrl())
                    .placeholder(ContextCompat.getDrawable(mContext, R.drawable.profile_default_banner))
                    .centerCrop()
                    .into(profileBannerImageView);
        }

    }

    // Setting just username, originalName, description
    private void setDefaultValue(@NonNull final ParseUser user) {
        swipeRefreshLayout.setRefreshing(false);

        usernameTextView.setText(user.getUsername());
        originalNameTextView.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));

        // Getting profile description
        String profileDescription = user.getString(ParseConstants.PROFILE_DESCRIPTION);
        if (profileDescription != null && !profileDescription.equals("")) {
            profileDescriptionTextView.setVisibility(View.VISIBLE);
            profileDescriptionTextView.setText(DocNormalizer.htmlToNormal(profileDescription));
        } else {
            profileDescriptionTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        // Disabling swipe to refresh when offset is not 0 or when app bar is not on its default position
        swipeRefreshLayout.setEnabled(verticalOffset == 0);
    }

    @Override
    public void onDestroyView() {
        if (parseUser != null)
            feedsArgsViewModel.addExtraInLastElement(parseUser, likedByCurrentUser,isWatchingByCurrentUser);

        super.onDestroyView();
        myUploadsArrayList.clear();
        rootView = null;
        swipeRefreshLayout = null;
        appBarLayout = null;
        profileBannerImageView = null;
        profilePicImageView = null;
        likeTheProfileImageView = null;
        usernameTextView = null;
        originalNameTextView = null;
        profileDescriptionTextView = null;
        tWatchersTextView = null;
        tWatchingTextView = null;
        tLikesTextView = null;
        tLikedProfileTextView = null;
        userUploadsRecyclerView = null;
        addUserToWatchlistBtn = null;
        watchersLinearLayout = null;
        watchingLinearLayout = null;

    }

    // Removing this fragment stack argument
    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteUserProfArg();
        super.onDestroy();
    }
}