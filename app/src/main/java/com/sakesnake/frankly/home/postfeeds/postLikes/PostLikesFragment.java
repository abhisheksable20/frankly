package com.sakesnake.frankly.home.postfeeds.postLikes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavsearch.searchprofiles.SearchedProfileAdapter;
import com.sakesnake.frankly.home.bottomnavsearch.searchprofiles.SearchedProfileCallback;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseObjectCallback;
import com.sakesnake.frankly.parsedatabase.ParseObjectUserListCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class PostLikesFragment extends Fragment implements ParseObjectUserListCallback, ParseObjectCallback, SearchedProfileCallback {

    private View rootView;

    private Toolbar fragmentToolbar;

    private RecyclerView postLikesRecyclerView;

    private Context mContext;

    private SearchedProfileAdapter searchedProfileAdapter;

    private LinearLayoutManager linearLayoutManager;

    private FeedsArgsViewModel feedsArgsViewModel;

    private NavController navController;

    private ParseObject currentParseObject;

    private boolean dataEnded = false;

    private boolean loadData = true;

    private static final int FETCH_LIMIT = 10;

    private final int REFRESH_THRESHOLD = 5;

    private int recyclerCurrentPos = 0;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_post_likes, container, false);

        // Getting the nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init the view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // init fragment views
        initFragViews();

        // init fragment argument
        initFragArg(feedsArgsViewModel.getPostLikesLastArg());

        return rootView;
    }

    // init frag views
    private void initFragViews(){
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.post_likes_frag_toolbar);
        postLikesRecyclerView = (RecyclerView) rootView.findViewById(R.id.post_likes_recycler_view);
    }

    // init fragment arguments
    private void initFragArg(final Bundle bundle){
        if (bundle == null)
            return;

        if (bundle.containsKey(Constants.BUNDLE_SAVED_POST_LIKES)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_SAVED_POST_LIKES);
            if (bundle.containsKey(Constants.BUNDLE_ADAPTER_LIST))
                setViews(bundle.getParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST));
            else
                FetchLatestData.fetchPostLikes(currentParseObject,FETCH_LIMIT,0,this);
        }
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_POST_LIKES)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_FRESH_POST_LIKES);
            FetchLatestData.fetchRemainingParseObjectFields(currentParseObject,this);
        }
    }

    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null) {
            currentParseObject = object;
            FetchLatestData.fetchPostLikes(object, FETCH_LIMIT, 0, this);
        }
    }

    @Override
    public void postObjectUserLists(List<ParseObject> postComments, List<ParseUser> parseUsers) {
        if (parseUsers == null)
            return;

        if (parseUsers.size() > 0)
            setViews(parseUsers);

        else {
            dataEnded = true;
            if (searchedProfileAdapter != null)
                searchedProfileAdapter.hideProgressBar();
            Log.d("ScaleTag", "postObjectUserLists: No likes found");
        }
    }

    // Setting fragment views
    private void setViews(final List<ParseUser> parseUsers){
        if (searchedProfileAdapter == null){
            searchedProfileAdapter = new SearchedProfileAdapter(mContext,false,parseUsers,this);
            linearLayoutManager = new LinearLayoutManager(mContext);
            postLikesRecyclerView.setAdapter(searchedProfileAdapter);
            postLikesRecyclerView.setLayoutManager(linearLayoutManager);
            postLikesRecyclerView.setScrollY(recyclerCurrentPos);
            return;
        }

        searchedProfileAdapter.appendData(parseUsers);

    }

    @Override
    public void profileSelected(ParseUser user) {
        if (user != null){
            if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
                navController.navigate(R.id.action_postLikesFragment_to_bottomProfileFragment);
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,user);
            feedsArgsViewModel.addUserProfileArgs(bundle);
            navController.navigate(R.id.action_postLikesFragment_to_usersProfileFragment);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Listining recycler view scroll
        postLikesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (linearLayoutManager == null || dataEnded || currentParseObject == null)
                    return;

                if (dy > 0){
                    final int totalCount = linearLayoutManager.getItemCount();
                    final int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                    if (loadData  &&  (totalCount <= (lastVisiblePosition + REFRESH_THRESHOLD))){
                        if (searchedProfileAdapter != null)
                            searchedProfileAdapter.showProgressBar();

                        loadData = false;

                        FetchLatestData.fetchPostLikes(currentParseObject,FETCH_LIMIT,linearLayoutManager.getItemCount(),PostLikesFragment.this);
                    }
                }
            }
        });
    }


    private ArrayList<ParseUser> getParseUserList(){
        ArrayList<ParseUser> parseUsers;
        if (searchedProfileAdapter != null){
            parseUsers = new ArrayList<>(searchedProfileAdapter.getParseUsers());
            return parseUsers;
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        if (currentParseObject != null){
            final ArrayList<ParseUser> parseUsers = getParseUserList();
            feedsArgsViewModel.modifyPostLikesLastArgs(currentParseObject,parseUsers);
        }
        recyclerCurrentPos = postLikesRecyclerView.getScrollY();
        super.onDestroyView();
        rootView = null;
        fragmentToolbar = null;
        postLikesRecyclerView = null;
        searchedProfileAdapter = null;
        linearLayoutManager = null;

    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastPostLikeArg();
        super.onDestroy();
    }

}