package com.sakesnake.frankly.home.postfeeds.allPostFeeds;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavhome.HomeFragmentViewModel;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.BlogOrNewsAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.DreamsOrIdeasAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.QuestionsAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.StoryOrPoemsAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.sakesnake.frankly.parsedatabase.UserDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// This fragment will show BlogOrNews, DreamsOrIdeas, Question, StoryOrPoem contents
// Remember here Quotes content is not included it will be available in ImagesFeedFragment
public class WritingFeedsFragment extends Fragment implements UserDataCallback, PostFeedsCallback {

    private Toolbar fragmentToolbar;

    private RecyclerView feedRecyclerView;

    private View rootView,noDataView;

    private ViewStub noDataViewStub;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Context mContext;

    private int skipData = 0;

    private final int limit = 10;

    private NavController navController;

    // Recycler view adapters, will be in use only one
    private BlogOrNewsAdapter blogOrNewsAdapter;

    private DreamsOrIdeasAdapter dreamsOrIdeasAdapter;

    private QuestionsAdapter questionsAdapter;

    private StoryOrPoemsAdapter storyOrPoemsAdapter;

    private GridLayoutManager layoutManager;

    private HomeFragmentViewModel homeFragmentViewModel;

    private SessionErrorViewModel sessionErrorViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private ParseUser fetchParseUser;

    private int contentType = -1;

    private int recyclerViewPosition = 0;

    // Recycler view data
    private int totalCount,lastVisibleItem;

    private boolean listenScroll = true;

    // Flag from where this fragment was called
    // 100 Home Fragment
    // 101 Search Fragment
    // 102 User Fragment
    private int calledFrom = 0;

    // First time this flag will be set to true, to trigger loading, then it will set according to it.
    private boolean loadData = false;

    private int refreshThreshold = 4;

    private boolean dataEnded = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_writing_feed, container, false);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init view model
        homeFragmentViewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        // init views
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.users_posts_toolbar);
        feedRecyclerView = (RecyclerView) rootView.findViewById(R.id.users_posts_recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.users_posts_swipe_to_refresh_layout);
        noDataViewStub = (ViewStub) rootView.findViewById(R.id.no_data_view_stub);


        // Getting arguments (Imp otherwise this fragment is useless)
        getFragmentArgs(feedsArgsViewModel.getArg());

        setToolbarTitle();

        // Observing Home feed blog or news data
        homeFragmentViewModel.getBlogOrNewsData().observe(getViewLifecycleOwner(), list -> {
            if (!(contentType == Constants.UPLOAD_BLOG  ||  contentType == Constants.UPLOAD_NEWS_AND_UPDATE))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL){
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setBlogOrNewsAdapter(parseObjectList,true);
            }
        });

        // Observing Home feed Dreams or Ideas data
        homeFragmentViewModel.getDreamsOrIdeasData().observe(getViewLifecycleOwner(), list ->{
            if (!(contentType == Constants.UPLOAD_DREAM  ||  contentType == Constants.UPLOAD_IDEAS))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL){
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setDreamsOrIdeasAdapter(parseObjectList,true);
            }
        });

        // Observing Home feed Question data
        homeFragmentViewModel.getQuotesOrQuestionData().observe(getViewLifecycleOwner(), list -> {
            if (!(contentType == Constants.UPLOAD_GIVE_QUESTION))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL) {
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setQuestionsAdapter(parseObjectList, true);
            }
        });

        // Observing Home feed Story or Poem data
        homeFragmentViewModel.getStoryOrPoemData().observe(getViewLifecycleOwner(), list ->{
            if (!(contentType == Constants.UPLOAD_STORY  ||  contentType == Constants.UPLOAD_POEM))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL) {
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setStoryOrPoemsAdapter(parseObjectList, true);
            }
        });

        return rootView;
    }

    // Filtering view model data list according to content type
    private List<ParseObject> filterModelLists(final List<ParseObject> parseObjectList){
        List<ParseObject> parseObjects = new ArrayList<>();
        for (int i = 0; i < parseObjectList.size(); i++){
            ParseObject object = parseObjectList.get(i);
            if (object.getInt(ParseConstants.CONTENT_TYPE) == contentType)
                parseObjects.add(object);
        }

        return parseObjects;
    }

    private void getFragmentArgs(final Bundle args){
        if (args != null){
            ArrayList<ParseObject> parseObjects = null;
            if (args.containsKey(Constants.BUNDLE_ADAPTER_LIST))
                parseObjects = args.getParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST);

            // This fragment is called from HomeFragment (100)
            if (args.containsKey(Constants.BUNDLE_HOME_CALL)  &&  args.containsKey(Constants.BUNDLE_CONTENT_TYPE)){
                contentType = args.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = args.getInt(Constants.BUNDLE_HOME_CALL);
                swipeRefreshLayout.setEnabled(false);
                listenScroll = false;
                fetchParseUser = ParseUser.getCurrentUser();
            }

            // This fragment is called from SearchFragment (101)
            else if (args.containsKey(Constants.BUNDLE_SEARCH_CALL)  &&  args.containsKey(Constants.BUNDLE_CONTENT_TYPE)){
                contentType = args.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = args.getInt(Constants.BUNDLE_SEARCH_CALL);

                if (parseObjects != null)
                    setContentAdapters(parseObjects, true);
                else
                    generateQueryThenFetch(null);
            }

            // This fragment is called from UserFragment (102)
            else if (args.containsKey(Constants.BUNDLE_USER_CALL)
                    &&  args.containsKey(Constants.BUNDLE_CONTENT_TYPE)
                    &&  args.containsKey(Constants.BUNDLE_PARSE_USER)){

                contentType = args.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = args.getInt(Constants.BUNDLE_USER_CALL);
                fetchParseUser = args.getParcelable(Constants.BUNDLE_PARSE_USER);

                if (parseObjects != null)
                    setContentAdapters(parseObjects, true);
                else{
                    swipeRefreshLayout.setRefreshing(true);
                    FetchLatestData.fetchLatestUserData(fetchParseUser,this);
                }
            }
        }
    }

    private void generateQueryThenFetch(final ParseUser user){
        final ParseQuery<ParseObject> parseQuery = generateParseQuery(user);
        if (parseQuery != null) {
            refreshAllFlags();
            swipeRefreshLayout.setRefreshing(true);
            fullNetworkFetch(parseQuery, true);
        }
    }


    private void setToolbarTitle(){
        switch (contentType){
            case Constants.UPLOAD_BLOG:{
                fragmentToolbar.setTitle(getString(R.string.my_blog_my_uploads));
                break;
            }
            case Constants.UPLOAD_NEWS_AND_UPDATE:{
                fragmentToolbar.setTitle(getString(R.string.my_news_and_updates_my_uploads));
                break;
            }
            case Constants.UPLOAD_DREAM:{
                fragmentToolbar.setTitle(getString(R.string.my_dreams_my_uploads));
                break;
            }
            case Constants.UPLOAD_IDEAS:{
                fragmentToolbar.setTitle(getString(R.string.my_ideas_my_uploads));
                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                fragmentToolbar.setTitle(getString(R.string.search_frag_answer_the_question));
                break;
            }
            case Constants.UPLOAD_STORY:{
                fragmentToolbar.setTitle(getString(R.string.stories_home_fragment));
                break;
            }
            case Constants.UPLOAD_POEM:{
                fragmentToolbar.setTitle(getString(R.string.poems_home_fragment));
                break;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating back
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());


        // Listening to swipe listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if ((calledFrom == Constants.HOME_CALL  ||  calledFrom == Constants.USER_CALL)  &&  fetchParseUser != null) {
                refreshAllFlags();
                FetchLatestData.fetchLatestUserData(fetchParseUser, this);
                return;
            }
            else if (calledFrom == Constants.SEARCH_CALL  &&  contentType != -1){
                ParseQuery<ParseObject> parseQuery = generateParseQuery(null);
                if (parseQuery != null) {
                    refreshAllFlags();
                    fullNetworkFetch(parseQuery, true);
                    return;
                }
            }
            swipeRefreshLayout.setRefreshing(false);
        });


        // Checking is user reached to bottom
        feedRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager == null  ||  !listenScroll  ||  dataEnded)
                    return;

                if (dy > 0) {
                    totalCount = layoutManager.getItemCount();
                    lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    if (loadData &&  (totalCount <= (lastVisibleItem + refreshThreshold))){
                        loadData = false;
                        skipData = totalCount;

                        showProgressBar(true);

                        ParseQuery<ParseObject> parseQuery = generateParseQuery(fetchParseUser);

                        if (parseQuery != null)
                            fullNetworkFetch(parseQuery,false);
                    }
                }

            }
        });
    }

    private void refreshAllFlags(){
        skipData = 0;
        recyclerViewPosition = 0;
    }


    // This method will generate parse query and then fetch it from network
    private ParseQuery<ParseObject> generateParseQuery(final ParseUser parseUser){
        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                return blogOrNewsQuery(parseUser);
            }
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_IDEAS:{
                return dreamsOrIdeasQuery(parseUser);
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                return questionQuery(parseUser);
            }
            case Constants.UPLOAD_STORY:
            case Constants.UPLOAD_POEM:{
                return storyOrPoemQuery(parseUser);
            }
            default:
                return null;
        }
    }

    private ParseQuery<ParseObject> blogOrNewsQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.MULTI_IMAGE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.CONTENT_PREVIEW);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);

        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }

    private ParseQuery<ParseObject> dreamsOrIdeasQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.CONTENT_PREVIEW);
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.SINGLE_IMAGE);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);

        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }

    private ParseQuery<ParseObject> questionQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);

        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }

    private ParseQuery<ParseObject> storyOrPoemQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.SMALL_TEXT_TWO);
        keys.add(ParseConstants.CONTENT_PREVIEW);
        keys.add(ParseConstants.SINGLE_IMAGE);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);

        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }


    private ParseQuery<ParseObject> queryAccordingToCall(final ParseUser user){
        ParseQuery<ParseObject> parseQuery = null;
        if (user != null){
            if (calledFrom == Constants.USER_CALL) {
                ParseRelation<ParseObject> parseRelation = user.getRelation(ParseConstants.USER_POSTS);
                parseQuery = parseRelation.getQuery();
            }
            else if (calledFrom == Constants.HOME_CALL){
                ParseRelation<ParseObject> parseRelation = user.getRelation(ParseConstants.USER_HOME_FEED_INCLUSIVE);
                parseQuery = parseRelation.getQuery();
            }
        }
        else
            parseQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);

        return parseQuery;
    }

    // Showing progress bar at the bottom of recycler view
    private void showProgressBar(final boolean showProgress){
        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                if (blogOrNewsAdapter != null) {
                    if (showProgress)
                        blogOrNewsAdapter.showProgressBar();
                    else
                        blogOrNewsAdapter.hideProgressBar();
                }
                break;
            }
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:{
                if (dreamsOrIdeasAdapter != null) {
                    if (showProgress)
                        dreamsOrIdeasAdapter.showProgressBar();
                    else
                        dreamsOrIdeasAdapter.hideProgressBar();
                }
                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                if (questionsAdapter != null) {
                    if (showProgress)
                        questionsAdapter.showProgressBar();
                    else
                        questionsAdapter.hideProgressBar();
                }
                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:{
                if (storyOrPoemsAdapter != null) {
                    if (showProgress)
                        storyOrPoemsAdapter.showProgressBar();
                    else
                        storyOrPoemsAdapter.hideProgressBar();
                }
                break;
            }
        }
    }


    @Override
    public void latestUserData(ParseUser parseUser, ParseException exception) {
        if (parseUser != null){
            fetchParseUser = parseUser;
            ParseQuery<ParseObject> parseQuery = generateParseQuery(fetchParseUser);
            if (parseQuery != null)
                fullNetworkFetch(parseQuery,true);
            else if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }
    }


    // Fetching from network this will fetch all the data
    private void fullNetworkFetch(@NonNull final ParseQuery<ParseObject> objectParseQuery, final boolean changeFullData){
        objectParseQuery.whereEqualTo(ParseConstants.CONTENT_TYPE,contentType);
        objectParseQuery.setLimit(limit);
        objectParseQuery.setSkip(skipData);
        objectParseQuery.include(ParseConstants.POST_OWNER);
        objectParseQuery.findInBackground(((objects, e) -> {
            if (e == null){
                if (objects.size() > 0) {
                    loadData = true;
                    setContentAdapters(objects,changeFullData);
                }
                else {
                    if (isAdapterNull())
                        setNoDataView();

                    dataEnded = true;
                    showProgressBar(false);
                }
            }
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isLoggedOut(true);
            }
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }));

    }

    private boolean isAdapterNull(){
        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                return blogOrNewsAdapter == null;
            }
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_IDEAS:{
                return dreamsOrIdeasAdapter == null;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                return questionsAdapter == null;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:{
                return storyOrPoemsAdapter == null;
            }
            default:
                return false;
        }
    }



    // Showing no data found message to user if no data retrieved from first call
    private void setNoDataView(){
        if (noDataView == null)
            noDataView = noDataViewStub.inflate();

        ImageView noDataImageView = (ImageView) noDataView.findViewById(R.id.no_data_image_view);
        TextView noDataTextView = (TextView) noDataView.findViewById(R.id.no_data_message_text_view);

        switch (contentType){
            case Constants.UPLOAD_BLOG: {
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext, R.drawable.no_blogs_found_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_blog_found_message));
                break;
            }

            case Constants.UPLOAD_NEWS_AND_UPDATE:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_news_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_news_found_message));
                break;
            }

            case Constants.UPLOAD_IDEAS:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_idea_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_ideas_found_message));
                break;
            }

            case Constants.UPLOAD_DREAM:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_dreams_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_dreams_found_message));
                break;
            }

            case Constants.UPLOAD_GIVE_QUESTION:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_questions_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_question_found_message));
                break;
            }

            case Constants.UPLOAD_POEM:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_poem_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_poem_found_message));
                break;
            }

            case Constants.UPLOAD_STORY:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_storys_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_story_found_message));
                break;
            }
        }
    }


    private void setContentAdapters(final List<ParseObject> parseObjectList, final boolean changeFullData){
        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                setBlogOrNewsAdapter(parseObjectList,changeFullData);
                break;
            }
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:{
                setDreamsOrIdeasAdapter(parseObjectList,changeFullData);
                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                setQuestionsAdapter(parseObjectList,changeFullData);
                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:{
                setStoryOrPoemsAdapter(parseObjectList,changeFullData);
                break;
            }
        }
    }

    // Setting blog or news adapter
    private void setBlogOrNewsAdapter(final List<ParseObject> parseObjects, final boolean changeFullData){
        if (blogOrNewsAdapter == null){
            blogOrNewsAdapter = new BlogOrNewsAdapter(parseObjects,
                    mContext,
                    R.layout.search_blog_or_news_item_list_recycler_view,
                    this,false);

            layoutManager = new GridLayoutManager(mContext,2);
            feedRecyclerView.setAdapter(blogOrNewsAdapter);
            feedRecyclerView.setLayoutManager(layoutManager);
            feedRecyclerView.scrollToPosition(recyclerViewPosition);
            return;
        }

        if (changeFullData)
            blogOrNewsAdapter.fullDataChanged(parseObjects);

        else
            blogOrNewsAdapter.appendData(parseObjects);
    }


    // Setting Dreams or ideas adapter
    private void setDreamsOrIdeasAdapter(final List<ParseObject> parseObjects, final boolean changeFullData){
        if (dreamsOrIdeasAdapter == null){
            dreamsOrIdeasAdapter = new DreamsOrIdeasAdapter(parseObjects,
                    R.layout.search_dreams_or_ideas_item_list_recycler_view,
                    mContext,
                    this,false);

            layoutManager = new GridLayoutManager(mContext,2);
            feedRecyclerView.setAdapter(dreamsOrIdeasAdapter);
            feedRecyclerView.setLayoutManager(layoutManager);
            feedRecyclerView.scrollToPosition(recyclerViewPosition);
            return;
        }
        if (changeFullData)
            dreamsOrIdeasAdapter.fullDataChanged(parseObjects);
        else
            dreamsOrIdeasAdapter.appendData(parseObjects);
    }

    // Setting Question Adapter
    private void setQuestionsAdapter(final List<ParseObject> parseObjects, final boolean changeFullData){
        if (questionsAdapter == null){
            questionsAdapter = new QuestionsAdapter(mContext,parseObjects,
                    R.layout.search_question_item_list_recycler_view,this);

            layoutManager = new GridLayoutManager(mContext,2);
            feedRecyclerView.setAdapter(questionsAdapter);
            feedRecyclerView.setLayoutManager(layoutManager);
            feedRecyclerView.scrollToPosition(recyclerViewPosition);
            return;
        }

        if (changeFullData)
            questionsAdapter.fullDataChanged(parseObjects);
        else
            questionsAdapter.appendData(parseObjects);
    }

    // Setting Story Or Poem Data
    private void setStoryOrPoemsAdapter(final List<ParseObject> parseObjects,final boolean changeFullData){
        if (storyOrPoemsAdapter == null){
            storyOrPoemsAdapter = new StoryOrPoemsAdapter(parseObjects,
                    R.layout.search_story_or_poem_item_list_recycler_view,
                    mContext,this,false);

            layoutManager = new GridLayoutManager(mContext,2);
            feedRecyclerView.setAdapter(storyOrPoemsAdapter);
            feedRecyclerView.setLayoutManager(layoutManager);
            feedRecyclerView.scrollToPosition(recyclerViewPosition);
            return;
        }

        if (changeFullData)
            storyOrPoemsAdapter.fullDataChanged(parseObjects);
        else
            storyOrPoemsAdapter.appendData(parseObjects);
    }

    @Override
    public void selectedPost(int currentPos,ParseObject object, ParseUser user) {
        recyclerViewPosition = currentPos;
        if (user != null){
            if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
                navController.navigate(R.id.action_writingFeedsFragment_to_bottomProfileFragment);
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,user);
            feedsArgsViewModel.addUserProfileArgs(bundle);
            navController.navigate(R.id.action_writingFeedsFragment_to_usersProfileFragment);
            return;
        }

        final int contentType = object.getInt(ParseConstants.CONTENT_TYPE);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT,object);
        feedsArgsViewModel.addFullFeedsArg(bundle);

        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                navController.navigate(R.id.action_writingFeedsFragment_to_blogOrNewsFullFeedFragment);
                break;
            }
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_IDEAS:{
                navController.navigate(R.id.action_writingFeedsFragment_to_dreamOrIdeasFullFragment);
                break;
            }
            case Constants.UPLOAD_STORY:{
                navController.navigate(R.id.action_writingFeedsFragment_to_storyFeedFullFragment);
                break;
            }
            case Constants.UPLOAD_POEM:{
                navController.navigate(R.id.action_writingFeedsFragment_to_poemFeedFullFragment);
                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                navController.navigate(R.id.action_writingFeedsFragment_to_questionsFeedFragment);
            }
        }
    }

    private ArrayList<ParseObject> getAdapterList(){
        ArrayList<ParseObject> parseObjects = null;
        switch (contentType){
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                if (blogOrNewsAdapter != null)
                    parseObjects = new ArrayList<>(blogOrNewsAdapter.getAdapterList());

                break;
            }
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:{
                if (dreamsOrIdeasAdapter != null)
                    parseObjects = new ArrayList<>(dreamsOrIdeasAdapter.getAdapterList());

                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                if (questionsAdapter != null)
                    parseObjects = new ArrayList<>(questionsAdapter.getAdapterList());

                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:{
                if (storyOrPoemsAdapter != null)
                    parseObjects = new ArrayList<>(storyOrPoemsAdapter.getAdapterList());

                break;
            }

        }

        return parseObjects;
    }


    @Override
    public void onDestroyView() {
        ArrayList<ParseObject> objectArrayList = getAdapterList();
        if (objectArrayList != null)
            feedsArgsViewModel.addParseObjectInLast(objectArrayList,0);
        super.onDestroyView();
        layoutManager = null;
        blogOrNewsAdapter = null;
        dreamsOrIdeasAdapter = null;
        questionsAdapter = null;
        storyOrPoemsAdapter = null;
        fragmentToolbar = null;
        feedRecyclerView = null;
        rootView = null;
        swipeRefreshLayout = null;
        noDataViewStub = null;
        noDataView = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastArg();
        super.onDestroy();
    }
}