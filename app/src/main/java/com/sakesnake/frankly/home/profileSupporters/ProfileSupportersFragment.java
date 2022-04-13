package com.sakesnake.frankly.home.profileSupporters;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseCloud;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// This fragment will show profile supporters users and also current user supporting
public class ProfileSupportersFragment extends Fragment implements SupporterCallback{

    private View rootView;

    private Toolbar fragmentToolbar;

    private ProgressBar progressBar;

    private RecyclerView supportersRecyclerView;

    private ProfileSupportersAdapter profileSupportersAdapter;

    private LinearLayoutManager linearLayoutManager;

    private Context mContext;

    private FeedsArgsViewModel feedsArgsViewModel;

    public static final int QUERY_WATCHERS = 101;

    public static final int QUERY_WATCHING = 102;

    public static final int QUERY_LIKED = 103;

    private int queryConstant;

    private ParseUser currentParseUser;

    private boolean grantDeletePermission = false;

    private NavController navController;

    private boolean loadingStarted = true;

    private boolean dataEnded = false;

    private int currentRecyclerViewPos = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_profile_supporters, container, false);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // init frag views
        initFragViews();

        // init fragment argument
        initFragArgs(feedsArgsViewModel.getLastSupportFragArg());

        return rootView;
    }

    // initializing fragment views
    private void initFragViews(){
        supportersRecyclerView = (RecyclerView) rootView.findViewById(R.id.supporters_recycler_view);
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.profile_supporters_toolbar);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);
    }

    // initializing fragment arguments
    private void initFragArgs(final Bundle bundle){
        if (bundle == null)
            return;

        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_USER)){
            currentParseUser = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_USER);
            queryConstant = bundle.getInt(Constants.BUNDLE_PROFILE_QUERY_CONSTANT);
            grantDeletePermission = bundle.getBoolean(Constants.BUNDLE_GRANT_DELETE_PERMISSION);
            ArrayList<ParseUser> parseUserArrayList = bundle.getParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST);
            if (parseUserArrayList == null)
                initDataQueryThenFetch();
            else{
                progressBar.setVisibility(View.GONE);
                setViews(parseUserArrayList);
            }

            setToolbarTitle();
        }
    }

    private void setToolbarTitle(){
        switch (queryConstant){
            case QUERY_WATCHERS:
                fragmentToolbar.setTitle(getString(R.string.my_watchers));
                break;

            case QUERY_WATCHING:
                fragmentToolbar.setTitle(getString(R.string.my_watch_list));
                break;

            case QUERY_LIKED:
                fragmentToolbar.setTitle(getString(R.string.my_liked_profile));
                break;
        }
    }

    // Preliminary work before fetching the data
    private void initDataQueryThenFetch(){
        if (currentParseUser == null)
            return;

        if (queryConstant == QUERY_WATCHERS){
            final ParseQuery<ParseUser> parseQuery = generateParseUserQuery(currentParseUser,ParseConstants.PROFILE_WATCHERS);
            fetchDataFromNetwork(parseQuery);
        }
        else if (queryConstant == QUERY_WATCHING){
            final ParseQuery<ParseUser> parseQuery = generateParseUserQuery(currentParseUser,ParseConstants.PROFILE_WATCHING);
            fetchDataFromNetwork(parseQuery);
        }
        else if (queryConstant == QUERY_LIKED)
            getLikedProfiles();
    }

    // Get current user liked profiles (Cloud function)
    private void getLikedProfiles(){
        final HashMap<String,Integer> hashMap = new HashMap<>();
        if (profileSupportersAdapter == null)
            hashMap.put(HashMapKeys.SKIP_LIKED_PROFILE_OBJECTS,0);
        else
            hashMap.put(HashMapKeys.SKIP_LIKED_PROFILE_OBJECTS,profileSupportersAdapter.getItemCount());

        ParseCloud.callFunctionInBackground(ParseConstants.CURRENT_USER_LIKED_PROFILE,hashMap, (objects,e) -> {
            if (e == null){
                List<ParseUser> parseUsers = (List<ParseUser>) objects;
                if (parseUsers != null && parseUsers.size() > 0)
                    setViews(parseUsers);
            }
            else
                Log.e(Constants.TAG, "Cannot fetch liked profiles: "+e.getMessage());
        });
    }


    // Generating parse query to to get relation of watchers and watching
    private ParseQuery<ParseUser> generateParseUserQuery(final ParseUser parseUser,final String relationKey){

        ParseRelation<ParseUser> parseUserRelation = parseUser.getRelation(relationKey);

        List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.PROFILE_PIC);
        keys.add(ParseConstants.PROFILE_USERNAME);
        keys.add(ParseConstants.PROFILE_ORIGINAL_NAME);

        ParseQuery<ParseUser> parseQuery = parseUserRelation.getQuery();
        parseQuery.selectKeys(keys);
        int FETCH_LIMIT = 10;
        parseQuery.setLimit(FETCH_LIMIT);
        if (profileSupportersAdapter == null)
            parseQuery.setSkip(0);
        else
            parseQuery.setSkip(profileSupportersAdapter.getItemCount());

        return parseQuery;
    }

    // fetching the parse user from network
    private void fetchDataFromNetwork(@NonNull final ParseQuery<ParseUser> parseQuery){
        parseQuery.findInBackground((parseUsers, e) -> {
            loadingStarted = false;
            if (e == null){
                if (parseUsers.size() > 0)
                    setViews(parseUsers);
                else {
                    dataEnded = true;
                    if (profileSupportersAdapter != null)
                        profileSupportersAdapter.hideProgressBar();
                }
            }
            else
                Log.e(Constants.TAG, "Cannot fetch parse users " + queryConstant+" Error ----------> "+e.getMessage());
        });
    }

    // Setting the fragment views
    private void setViews(final List<ParseUser> parseUserList){
        progressBar.setVisibility(View.GONE);

        if (profileSupportersAdapter == null){
            profileSupportersAdapter = new ProfileSupportersAdapter(parseUserList,mContext,grantDeletePermission,this);
            linearLayoutManager = new LinearLayoutManager(mContext);
            supportersRecyclerView.setAdapter(profileSupportersAdapter);
            supportersRecyclerView.setLayoutManager(linearLayoutManager);
            supportersRecyclerView.setScrollY(currentRecyclerViewPos);
            return;
        }

        profileSupportersAdapter.appendTheData(parseUserList);
    }

    @Override
    public void callbackWithDeleteOption(ParseUser parseUser, boolean deleteUser) {
        if (deleteUser){
            if (queryConstant == ProfileSupportersFragment.QUERY_LIKED)
                removeUserFromLikedProfile(parseUser.getObjectId());
            else if (queryConstant == ProfileSupportersFragment.QUERY_WATCHING)
                removeUserFromWatchlist(parseUser.getObjectId());
            return;
        }

        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername()))
            navController.navigate(R.id.action_profileSupportersFragment_to_bottomProfileFragment);
        else{
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
            feedsArgsViewModel.addUserProfileArgs(bundle);
            navController.navigate(R.id.action_profileSupportersFragment_to_usersProfileFragment);
        }

    }

    // Remove user from liked profile list
    private void removeUserFromLikedProfile(final String userObjectId){
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.USER_OBJECT_ID_KEY,userObjectId);
        ParseCloud.callFunctionInBackground(ParseConstants.LIKE_OR_DISLIKE_USER,hashMap,(object, e)->{
            if (e != null)
                Log.e(Constants.TAG, "likeOrDislikeUser failed to call cloud: ERROR---> "+e.getMessage());
            else
                Log.d(Constants.TAG, "likeOrDislikeUser cloud code called successfully");
        });
    }

    // Cloud function to remove user from watchlist
    private void removeUserFromWatchlist(final String userObjectId){
        final HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.USER_OBJECT_ID_KEY,userObjectId);
        ParseCloud.callFunctionInBackground(ParseConstants.ADD_OR_REMOVE_WATCHLIST,hashMap,(object, e) -> {
            if (e != null)
                Log.e(Constants.TAG, "Failed to remove user "+userObjectId+" from watchlist");
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigating up
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Observing recycler view scrolling
        supportersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0){
                    if (dataEnded || linearLayoutManager == null)
                        return;

                    int refreshThreshHold = 4;
                    int totalCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loadingStarted && (totalCount <= (lastVisibleItem + refreshThreshHold))){
                        loadingStarted = true;
                        profileSupportersAdapter.showProgressBar();
                        initDataQueryThenFetch();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (profileSupportersAdapter != null) {
            feedsArgsViewModel.modifySupportFragLastArg(profileSupportersAdapter.getCurrentList());
            currentRecyclerViewPos = supportersRecyclerView.getScrollY();
        }

        super.onDestroyView();
        rootView = null;
        fragmentToolbar = null;
        supportersRecyclerView = null;
        profileSupportersAdapter = null;
        linearLayoutManager = null;
        progressBar = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastSupportFragArg();
        super.onDestroy();
    }

}