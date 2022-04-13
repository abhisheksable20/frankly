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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.App;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.bottomnavhome.HomeFragmentViewModel;
import com.sakesnake.frankly.home.postfeeds.SharePost;
import com.sakesnake.frankly.home.postfeeds.postMoreDetails.PostDetailsViewModel;
import com.sakesnake.frankly.home.postfeeds.postMoreDetails.PostMoreInfoFragment;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.ImagesOrMemesFeedAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.QuotesFeedAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedWithPositionCallback;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.sakesnake.frankly.parsedatabase.UserDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// This fragment will show Image, Memes and Quotes contents feeds
// This fragment will only show only three types of content
public class ImagesFeedFragment extends Fragment implements PostFeedsCallback, UserDataCallback, PostFeedWithPositionCallback {

    private View rootView, noDataView;

    private ViewStub noDataViewStub;

    private Toolbar fragmentToolbar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView feedRecyclerView;

    private LinearLayoutManager linearLayoutManager;

    private NavController navController;

    private HomeFragmentViewModel homeFragmentViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private PostDetailsViewModel postDetailsViewModel;

    // Recycler view adapters (Use only one at a time)
    private ImagesOrMemesFeedAdapter imagesOrMemesFeedAdapter;

    private QuotesFeedAdapter quotesFeedAdapter;

    private Context mContext;

    private int skipData = 0;

    private final int limit = 10;

    private ParseUser fetchParseUser;

    private ParseObject selectedParseObject;

    private int contentType = -1;

    // Flag for For Loop to from (Check is post liked by current user)
    private int startFromFlag = 0;

    // Recycler view data
    private int totalCount,lastVisibleItem;

    private boolean listenScroll = true;

    public static final int WORK_WITH_LIKE = 101;

    public static final int WORK_WITH_TOTAL_LIKES = 102;

    public static final int WORK_WITH_COMMENT = 103;

    public static final int WORK_WITH_SHARE = 104;

    public static final int WORK_WITH_MORE = 105;

    // Flag from where this fragment was called
    // 100 Home Fragment
    // 101 Search Fragment
    // 102 User Fragment
    private int calledFrom = 0;

    // First time this flag will be set to true, to trigger loading, then it will set according to it.
    private boolean loadData = false;

    private final int refreshThreshold = 5;

    private boolean dataEnded = false;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_images_feed, container, false);

        // init view models
        homeFragmentViewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);


        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init views
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.users_posts_toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.users_posts_swipe_to_refresh_layout);
        feedRecyclerView = (RecyclerView) rootView.findViewById(R.id.users_posts_recycler_view);
        noDataViewStub = (ViewStub) rootView.findViewById(R.id.no_data_view_stub);

        getFragmentArgs(feedsArgsViewModel.getArg());

        setToolbarTitle();

        // Observing HomeFragmentViewModel data
        // Observing images or memes data
        homeFragmentViewModel.getImageOrMemeData().observe(getViewLifecycleOwner(), list -> {
            if (!(contentType == Constants.UPLOAD_IMAGE  ||  contentType == Constants.UPLOAD_MEMES_AND_COMEDY))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL){
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setImageOrMemesAdapter(convertToCustomObject(parseObjectList),true,true);
            }
        });

        // Observing quotes data
        homeFragmentViewModel.getQuotesOrQuestionData().observe(getViewLifecycleOwner(), list -> {
            if (!(contentType == Constants.UPLOAD_QUOTE_AND_THOUGHTS))
                return;

            if (list != null  &&  calledFrom == Constants.HOME_CALL){
                List<ParseObject> parseObjectList = filterModelLists(list);
                if (parseObjectList.size() > 0)
                    setQuotesAdapter(convertToCustomObject(parseObjectList),true,true);
            }
        });


        return rootView;
    }

    // Setting toolbar title
    private void setToolbarTitle(){
        switch (contentType){
            case Constants.UPLOAD_IMAGE:{
                fragmentToolbar.setTitle(getString(R.string.there_images_home_fragment));
                break;
            }
            case Constants.UPLOAD_MEMES_AND_COMEDY:{
                fragmentToolbar.setTitle(getString(R.string.memes_and_comedy_home_fragment));
                break;
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                fragmentToolbar.setTitle(getString(R.string.quotes_and_thought_home_fragment));
                break;
            }
        }
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



    private void getFragmentArgs(final Bundle fragArgs){
        if (fragArgs != null){

            ArrayList<CustomParseObject> customParseObjects = null;

            if (fragArgs.containsKey(Constants.BUNDLE_ADAPTER_LIST))
                customParseObjects = fragArgs.getParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST);

            // This fragment is called from HomeFragment (100)
            if (fragArgs.containsKey(Constants.BUNDLE_HOME_CALL)  &&  fragArgs.containsKey(Constants.BUNDLE_CONTENT_TYPE)){
                contentType = fragArgs.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = fragArgs.getInt(Constants.BUNDLE_HOME_CALL);
                swipeRefreshLayout.setEnabled(false);
                listenScroll = false;
                fetchParseUser = ParseUser.getCurrentUser();
            }

            // This fragment is called from SearchFragment (101)
            else if (fragArgs.containsKey(Constants.BUNDLE_SEARCH_CALL)  &&  fragArgs.containsKey(Constants.BUNDLE_CONTENT_TYPE)){
                contentType = fragArgs.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = fragArgs.getInt(Constants.BUNDLE_SEARCH_CALL);
                if (customParseObjects != null)
                    setContentAdapters(customParseObjects, true, false);

                else
                    generateQueryThenFetch(null);

            }

            // This fragment is called from UserFragment (102)
            else if (fragArgs.containsKey(Constants.BUNDLE_USER_CALL)
                    &&  fragArgs.containsKey(Constants.BUNDLE_CONTENT_TYPE)
                    &&  fragArgs.containsKey(Constants.BUNDLE_PARSE_USER)){

                contentType = fragArgs.getInt(Constants.BUNDLE_CONTENT_TYPE);
                calledFrom = fragArgs.getInt(Constants.BUNDLE_USER_CALL);
                fetchParseUser = fragArgs.getParcelable(Constants.BUNDLE_PARSE_USER);
                swipeRefreshLayout.setRefreshing(true);
                if (customParseObjects != null)
                    setContentAdapters(customParseObjects, true, false);

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
            fetchDataFromNetwork(parseQuery, true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // observing post more info view model
        postDetailsViewModel.getParseUserData().observe(getViewLifecycleOwner(), parseUser -> {
            if (parseUser != null){
                navigateToUserFragment(parseUser);
                postDetailsViewModel.setParseUserData(null);
            }
        });

        postDetailsViewModel.getProcessToDo().observe(getViewLifecycleOwner(), processToDo -> {
            if (processToDo == PostDetailsViewModel.PROCESS_POST_DELETED){
                navController.navigateUp();
                postDetailsViewModel.setProcessData(0);
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (selectedParseObject == null)
                    return;

                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,selectedParseObject);
                navController.navigate(R.id.action_imagesFeedFragment_to_reportPostFragment,bundle);
            }
        });

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
                    fetchDataFromNetwork(parseQuery, true);
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
                if (linearLayoutManager == null  ||  !listenScroll  ||  dataEnded)
                    return;

                if (dy > 0) {
                    totalCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (loadData &&  (totalCount <= (lastVisibleItem + refreshThreshold))){
                        loadData = false;
                        skipData = totalCount;
                        showProgressBar(true);

                        ParseQuery<ParseObject> parseQuery = generateParseQuery(fetchParseUser);

                        if (parseQuery != null)
                            fetchDataFromNetwork(parseQuery,false);
                    }
                }
            }
        });
    }

    // Refreshing all the flags
    private void refreshAllFlags(){
        skipData = 0;
    }

    private void showProgressBar(final boolean showProgress){
        switch (contentType){
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                if (imagesOrMemesFeedAdapter != null) {
                    if (showProgress)
                        imagesOrMemesFeedAdapter.showProgressBar();
                    else
                        imagesOrMemesFeedAdapter.hideProgressBar();
                }
                break;
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                if (quotesFeedAdapter != null) {
                    if (showProgress)
                        quotesFeedAdapter.showProgressBar();
                    else
                        quotesFeedAdapter.hideProgressBar();
                }
                break;
            }
        }
    }

    // Generating the parse query for network call
    private ParseQuery<ParseObject> generateParseQuery(final ParseUser user){
        switch (contentType){
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                return imageOrImagesQuery(user);
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                return quotesQuery(user);
            }
            default:
                return null;
        }
    }


    @Override
    public void latestUserData(ParseUser parseUser, ParseException exception) {
        if (parseUser != null){
            fetchParseUser = parseUser;
            ParseQuery<ParseObject> parseQuery = generateParseQuery(fetchParseUser);
            if (parseQuery != null)
                fetchDataFromNetwork(parseQuery,true);
            else if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }
    }

    // Getting images or memes query
    private ParseQuery<ParseObject> imageOrImagesQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.SINGLE_IMAGE);
        keys.add(ParseConstants.DESCRIPTION);
        keys.add(ParseConstants.LOCATION);
        keys.add(ParseConstants.TOTAL_CONNECTED_USERS);
        keys.add(ParseConstants.LIKES_USERNAMES);
        keys.add(ParseConstants.TOTAL_POST_LIKES);
        keys.add(ParseConstants.TOTAL_POST_COMMENTS);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);
        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }

    // Getting Quotes query
    private ParseQuery<ParseObject> quotesQuery(final ParseUser user){
        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.SINGLE_IMAGE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.DESCRIPTION);
        keys.add(ParseConstants.LOCATION);
        keys.add(ParseConstants.TOTAL_CONNECTED_USERS);
        keys.add(ParseConstants.LIKES_USERNAMES);
        keys.add(ParseConstants.TOTAL_POST_LIKES);
        keys.add(ParseConstants.TOTAL_POST_COMMENTS);

        ParseQuery<ParseObject> parseQuery = queryAccordingToCall(user);
        if (parseQuery != null)
            parseQuery.selectKeys(keys);

        return parseQuery;
    }


    private ParseQuery<ParseObject> queryAccordingToCall(ParseUser user){
        ParseQuery<ParseObject> parseQuery = null;
        if (user != null){
            if (calledFrom == Constants.USER_CALL){
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

    private ArrayList<CustomParseObject> getAdapterList(){
        ArrayList<CustomParseObject> arrayList = null;
        if (imagesOrMemesFeedAdapter != null)
            arrayList = new ArrayList<>(imagesOrMemesFeedAdapter.getCustomParseObjects());
        else if (quotesFeedAdapter != null)
            arrayList = new ArrayList<>(quotesFeedAdapter.getCustomParseObjects());

        return arrayList;
    }


    // Fetching the data from network
    private void fetchDataFromNetwork(@NonNull final ParseQuery<ParseObject> parseQuery, final boolean changeFullData){
        parseQuery.whereEqualTo(ParseConstants.CONTENT_TYPE,contentType);
        parseQuery.setLimit(limit);
        parseQuery.setSkip(skipData);
        parseQuery.include(ParseConstants.POST_OWNER);
        parseQuery.findInBackground((objects, e) -> {
            if (e == null){
                if (objects.size() > 0) {
                    loadData = true;
                    setContentAdapters(convertToCustomObject(objects),changeFullData,true);
                }
                else {
                    if (isAdapterNull())
                        noDataFound();

                    dataEnded = true;
                    showProgressBar(false);
                }
            }

            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        });
    }

    // Converting parse object list to custom parse object
    private List<CustomParseObject> convertToCustomObject(final List<ParseObject> parseObjectList){
        final List<CustomParseObject> customParseObjects = new ArrayList<>();
        for (ParseObject obj: parseObjectList) {
            customParseObjects.add(new CustomParseObject(obj));
        }

        return customParseObjects;
    }


    private boolean isAdapterNull(){
        switch (contentType){
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                return imagesOrMemesFeedAdapter == null;
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                return quotesFeedAdapter == null;
            }
            default:
                return false;
        }
    }


    // Setting no data view if no data found from first fetch
    private void noDataFound(){
        if (noDataView == null)
            noDataView = noDataViewStub.inflate();

        ImageView noDataImageView = (ImageView) noDataView.findViewById(R.id.no_data_image_view);
        TextView noDataTextView = (TextView) noDataView.findViewById(R.id.no_data_message_text_view);

        switch (contentType){
            case Constants.UPLOAD_IMAGE:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_image_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_images_found_message));
                break;
            }

            case Constants.UPLOAD_MEMES_AND_COMEDY:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_meme_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_memes_found_message));
                break;
            }

            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_quotes_production_icon)).into(noDataImageView);
                noDataTextView.setText(getString(R.string.no_quotes_found_message));
                break;
            }
        }
    }


    // Setting adapters according to content type (We have to set only two adapters)
    private void setContentAdapters(final List<CustomParseObject> customParseObjects, final boolean changeFullData,final boolean triggerLikeQuery){
        switch (contentType){
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                setImageOrMemesAdapter(customParseObjects,changeFullData,triggerLikeQuery);
                break;
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                setQuotesAdapter(customParseObjects,changeFullData,triggerLikeQuery);
                break;
            }
        }
    }

    // Setting ImagesOrMemes content adapter
    private void setImageOrMemesAdapter(final List<CustomParseObject> customParseObjects, final boolean changeFullData, final boolean triggerLikeQuery){
        if (imagesOrMemesFeedAdapter == null){
            imagesOrMemesFeedAdapter = new ImagesOrMemesFeedAdapter(customParseObjects,mContext,this,this);
            linearLayoutManager = new LinearLayoutManager(mContext);
            feedRecyclerView.setAdapter(imagesOrMemesFeedAdapter);
            feedRecyclerView.setLayoutManager(linearLayoutManager);
        }
        else {
            if (changeFullData)
                imagesOrMemesFeedAdapter.updateWholeData(customParseObjects);
            else
                imagesOrMemesFeedAdapter.appendData(customParseObjects);
        }

        if (triggerLikeQuery){
            checkIfLiked(imagesOrMemesFeedAdapter.getCustomParseObjects(),startFromFlag);
            startFromFlag = imagesOrMemesFeedAdapter.getItemCount() - 1;
        }
    }

    // Setting Quotes content adapter
    private void setQuotesAdapter(final List<CustomParseObject> customParseObjects, final boolean changeFullData, final boolean triggerLikeQuery){
        if (quotesFeedAdapter == null){
            quotesFeedAdapter = new QuotesFeedAdapter(customParseObjects,mContext,this,this);
            linearLayoutManager = new LinearLayoutManager(mContext);
            feedRecyclerView.setAdapter(quotesFeedAdapter);
            feedRecyclerView.setLayoutManager(linearLayoutManager);
        }
        else{
            if (changeFullData)
                quotesFeedAdapter.updateWholeData(customParseObjects);
            else
                quotesFeedAdapter.appendData(customParseObjects);
        }

        if (triggerLikeQuery){
            checkIfLiked(quotesFeedAdapter.getCustomParseObjects(),startFromFlag);
            startFromFlag = quotesFeedAdapter.getItemCount() - 1;
        }
    }

    // Checking if the post that retrieved has been liked by current user
    private void checkIfLiked(final List<CustomParseObject> customParseObjectsList,final int startFlag){
        App.getCachedExecutorService().execute(() -> {
            final String currentUsername = ParseUser.getCurrentUser().getUsername();
            for (int i=startFlag ;i<customParseObjectsList.size(); i++){
                final List<String> connectedUsers = customParseObjectsList.get(i).getParseObject().getList(ParseConstants.LIKES_USERNAMES);
                final int position = i;
                boolean postLikedFlag = false;
                if (connectedUsers == null)
                    continue;

                for (String username:connectedUsers) {
                    if (username.equals(currentUsername)){
                        postLikedFlag = true;
                        App.getMainThread().post(() -> {
                            if (imagesOrMemesFeedAdapter != null)
                                imagesOrMemesFeedAdapter.postLiked(position);
                            else if (quotesFeedAdapter != null)
                                quotesFeedAdapter.postLiked(position);
                        });
                    }

                    if (postLikedFlag)
                        break;
                }
            }
        });
    }

    @Override
    public void selectedPost(int currentPos,ParseObject object, ParseUser user) {
        if (user != null)
            navigateToUserFragment(user);
    }

    private void navigateToUserFragment(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            navController.navigate(R.id.action_imagesFeedFragment_to_bottomProfileFragment);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_imagesFeedFragment_to_usersProfileFragment);
    }


    @Override
    public void feedCallbackWithPosition(CustomParseObject customParseObject, int position, int work) {
        if (customParseObject == null)
            return;

        switch (work){
            case WORK_WITH_LIKE:
                workWithLike(customParseObject,position);
                break;

            case WORK_WITH_COMMENT:
                workWithComment(customParseObject);
                break;

            case WORK_WITH_TOTAL_LIKES:
                navigateToPostLikesFrag(customParseObject);
                break;

            case WORK_WITH_MORE:
                workWithMoreDetails(customParseObject);
                break;

            case WORK_WITH_SHARE:
                workWithShare(customParseObject);
                break;
        }
    }

    // User has tapped like image view
    private void workWithLike(final CustomParseObject customParseObject,int position){
        boolean isLikedPost = customParseObject.getIsLikedByMe();
        ParseObject parseObject = customParseObject.getParseObject();
        int totalLikes;
        if (isLikedPost){
            isLikedPost = false;
            totalLikes = (parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)) - 1;
        }
        else{
            isLikedPost = true;
            totalLikes = (parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)) + 1;
        }

        customParseObject.getParseObject().put(ParseConstants.TOTAL_POST_LIKES,totalLikes);
        customParseObject.setPostLikedByMe(isLikedPost);

        FetchLatestData.likeOrDislikePost(parseObject.getObjectId(),null);

        if (imagesOrMemesFeedAdapter != null)
            imagesOrMemesFeedAdapter.likeImageTapped(customParseObject,position);
        else if (quotesFeedAdapter != null)
            quotesFeedAdapter.likeImageTapped(customParseObject,position);
    }

    // navigate to post likes fragment
    private void navigateToPostLikesFrag(final CustomParseObject customParseObject){
        final ParseObject parseObject = customParseObject.getParseObject();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES,parseObject);
        feedsArgsViewModel.insertPostLikesArgStarting(bundle);
        navController.navigate(R.id.action_imagesFeedFragment_to_postLikesFragment);
    }



    // navigate to post comments fragment
    private void workWithComment(CustomParseObject customParseObject){
        ParseObject postObject = customParseObject.getParseObject();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT,postObject);
        feedsArgsViewModel.insertPostCommentStartingArg(bundle);
        navController.navigate(R.id.action_imagesFeedFragment_to_postCommentsFragment);
    }

    // navigating to bottom model fragment
    private void workWithMoreDetails(CustomParseObject customParseObject){
        ParseObject parseObject = customParseObject.getParseObject();
        selectedParseObject = parseObject;
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT,parseObject);
        bundle.putBoolean(Constants.BUNDLE_REFRESH_OBJECT,true);

        PostMoreInfoFragment postMoreInfoFragment = new PostMoreInfoFragment();
        postMoreInfoFragment.setArguments(bundle);
        postMoreInfoFragment.show(getParentFragmentManager(),PostMoreInfoFragment.FRAGMENT_TAG);
    }

    // Share content with other application
    private void workWithShare(final CustomParseObject customParseObject){
        List<String> imageUrl;
        final ParseFile imageContent = customParseObject.getParseObject().getParseFile(ParseConstants.SINGLE_IMAGE);
        if (imageContent != null){
            imageUrl = new ArrayList<>();
            imageUrl.add(imageContent.getUrl());
            SharePost.sharePost(DocNormalizer.htmlToNormal(customParseObject.getParseObject().getString(ParseConstants.DESCRIPTION)).toString(),
                    null,null,imageUrl,mContext);
        }
    }


    @Override
    public void onDestroyView() {
        final ArrayList<CustomParseObject> customParseObjects = getAdapterList();
        if (customParseObjects != null)
            feedsArgsViewModel.addCustomObjectInLast(customParseObjects,0);

        super.onDestroyView();
        rootView = null;
        selectedParseObject = null;
        linearLayoutManager = null;
        fragmentToolbar = null;
        swipeRefreshLayout = null;
        feedRecyclerView = null;
        quotesFeedAdapter = null;
        imagesOrMemesFeedAdapter = null;
        noDataViewStub = null;
        noDataView = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastArg();
        super.onDestroy();
    }
}