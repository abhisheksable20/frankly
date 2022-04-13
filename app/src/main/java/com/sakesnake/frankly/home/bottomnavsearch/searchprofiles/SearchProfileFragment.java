package com.sakesnake.frankly.home.bottomnavsearch.searchprofiles;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchProfileFragment extends Fragment implements View.OnFocusChangeListener,TextWatcher,SearchedProfileCallback{

    private EditText getUsernameEditText;

    private ImageView navigateUpImageView;

    private ProgressBar searchingUsersProgressBar;

    private RecyclerView recentSearchesRecyclerView,networkSearchProfileRecyclerView;

    private TextView recentSearchTitleTextView;

    private View rootView;

    private Context mContext;

    private NavController navController;

    private SearchProfilesViewModel searchProfilesViewModel;

    private SearchedProfileAdapter networkSearchAdapter;

    private FeedsArgsViewModel feedsArgsViewModel;

    private boolean startSearching = false;

    private boolean showSoftInput = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_search_profile, container, false);

        // init view models
        searchProfilesViewModel = new ViewModelProvider(requireActivity()).get(SearchProfilesViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);


        // init fragment views
        getUsernameEditText = (EditText) rootView.findViewById(R.id.search_profile_fragment_get_username_edit_text);
        searchingUsersProgressBar = (ProgressBar) rootView.findViewById(R.id.search_profile_fragment_progress_bar);
        navigateUpImageView = (ImageView) rootView.findViewById(R.id.search_profile_fragment_back_navigation_image_view);
        recentSearchesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recent_searched_users_recycler_view);
        recentSearchTitleTextView = (TextView) rootView.findViewById(R.id.recent_searches_title_text_view);
        networkSearchProfileRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_profile_fragment_result_recycler_view);


        if (getUsernameEditText.getText().toString().trim().isEmpty())
            getUsersFromLocalStore();

        if (getArguments() != null  &&  getArguments().getBoolean(Constants.BUNDLE_SHOW_SOFT_INPUT)){
            getArguments().remove(Constants.BUNDLE_SHOW_SOFT_INPUT);
            showSoftInput = true;
            searchProfilesViewModel.setParseUsers(null);
        }

        // Observing searched users view model data (This data is retrieved from network)
        searchProfilesViewModel.getParseUsersFromNetwork().observe(getViewLifecycleOwner(),parseUsers -> {
            if (parseUsers != null  &&  parseUsers.size() > 0){
                recentSearchTitleTextView.setVisibility(View.GONE);
                networkSearchProfileRecyclerView.setVisibility(View.VISIBLE);

                setSearchedProfileAdapter(parseUsers,false);
            }
        });

        return rootView;
    }

    // Setting searched profile adapter
    private void setSearchedProfileAdapter(final List<ParseUser> parseUsers, final boolean grantDeletePermission){
        if (grantDeletePermission)
            recentSearchTitleTextView.setVisibility(View.VISIBLE);
        else
            recentSearchTitleTextView.setVisibility(View.GONE);

        if (networkSearchAdapter != null){
            networkSearchAdapter.updateNetworkData(parseUsers,grantDeletePermission);
            return;
        }

        networkSearchAdapter = new SearchedProfileAdapter(mContext,grantDeletePermission,parseUsers,this);
        networkSearchProfileRecyclerView.setAdapter(networkSearchAdapter);
        networkSearchProfileRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    // Profile adapter callback
    @Override
    public void profileSelected(ParseUser user) {
        getUsernameEditText.clearFocus();
        saveUserToLocalStore(user);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,user);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_searchProfileFragment_to_usersProfileFragment,bundle);
    }


    // Getting parse user from parse local datastore
    private void getUsersFromLocalStore(){
        final ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.fromLocalDatastore();
        parseQuery.whereNotEqualTo(ParseConstants.PROFILE_USERNAME,ParseUser.getCurrentUser().getUsername());
        parseQuery.findInBackground((parseUsers, e) -> {
            if (e == null && parseUsers.size() > 0)
                setSearchedProfileAdapter(parseUsers, true);
            else if (e != null)
                Log.d(Constants.TAG, "Failed to get localDataStore parse user: "+e.getMessage());
        });
    }

    // Saving parse user to local datastore (unique)
    private void saveUserToLocalStore(final ParseUser parseUser){
        final ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.fromLocalDatastore();
        parseQuery.getInBackground(parseUser.getObjectId(), (object, e) -> {
            if (object == null)
                parseUser.pinInBackground();
        });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigating up
        navigateUpImageView.setOnClickListener(view1 -> navController.navigateUp());

        // Listening on focus change listener
        getUsernameEditText.setOnFocusChangeListener(this);
        if (showSoftInput)
            getUsernameEditText.requestFocus();

        // Listening on text change listener
        getUsernameEditText.addTextChangedListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (b)
            manager.showSoftInput(view,InputMethodManager.SHOW_FORCED);
        else
            manager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        searchingUsersProgressBar.setVisibility(View.VISIBLE);
        startSearching = true;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (rootView == null)
            return;

        getParseUser(charSequence.toString().trim());
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.toString().trim().isEmpty()) {
            searchingUsersProgressBar.setVisibility(View.GONE);
            getUsersFromLocalStore();
        }
    }

    // Retrieving Parse user from network
    private void getParseUser(final String username){
        if (username.equals("")  ||  !startSearching)
            return;

        final ArrayList<String> keys = new ArrayList<>();
        keys.add(ParseConstants.PROFILE_USERNAME);
        keys.add(ParseConstants.PROFILE_ORIGINAL_NAME);
        keys.add(ParseConstants.PROFILE_PIC);

        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereStartsWith(ParseConstants.PROFILE_USERNAME,username);
        userParseQuery.whereEqualTo(ParseConstants.EMAIL_VERIFIED,true);
        userParseQuery.whereNotEqualTo(ParseConstants.PROFILE_USERNAME,ParseUser.getCurrentUser().getUsername());
        userParseQuery.selectKeys(keys);
        userParseQuery.setLimit(10);

        userParseQuery.findInBackground(((objects, e) -> {
            if (rootView == null)
                return;

            searchingUsersProgressBar.setVisibility(View.GONE);

            if (e == null)
                searchProfilesViewModel.setParseUsers(objects);
            else if (e.getCode() == ParseException.INVALID_SESSION_TOKEN){
                feedsArgsViewModel.clearAllArgs();
                SessionErrorViewModel sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
                sessionErrorViewModel.isSessionExpired(true);
            }
            else
                System.out.println(e.getMessage());

        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showSoftInput = false;
        startSearching = false;
        rootView = null;
        getUsernameEditText = null;
        searchingUsersProgressBar = null;
        navigateUpImageView = null;
        recentSearchesRecyclerView = null;
        networkSearchProfileRecyclerView = null;
        networkSearchAdapter = null;
        recentSearchTitleTextView = null;
    }
}