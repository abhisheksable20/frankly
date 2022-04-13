package com.sakesnake.frankly.home.bottomnavhome;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
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
import android.widget.TextView;

import com.sakesnake.frankly.App;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavhome.nofeeds.AdapterPositionCallback;
import com.sakesnake.frankly.home.bottomnavhome.nofeeds.NoFeeds;
import com.sakesnake.frankly.home.bottomnavhome.nofeeds.NoFeedsAdapter;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.WritingUploadViewModel;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.home.postfeeds.homePostFeedsAdapters.HomeFeedsAdapter;
import com.sakesnake.frankly.home.postfeeds.homePostFeedsAdapters.HomeFeedsModel;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.ImagesHomeAdapterCallback;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// This fragment is not optimized it will be lagged many time fix it later.
public class BottomHomeFragment extends Fragment implements ImagesHomeAdapterCallback, PostFeedsCallback,Runnable, AdapterPositionCallback {

    private View rootView;

    private Context mContext;

    private ViewStub imageOrMemesViewStub,dreamOrIdeasViewStub,blogOrNewsViewStub,
            quotesOrQuestionViewStub,storyOrPoemViewStub,noContentViewStub;

    private View imageOrMemesView,dreamOrIdeasView,blogOrNewsView,
            quotesOrQuestionView,storyOrPoemView,noContentView;

    private NestedScrollView nestedScrollView;

    private int currentScrollPos = 0;

    private HomeFeedsAdapter imageOrMemesAdapter,dreamOrIdeasAdapter,blogOrNewsAdapter,
            quotesOrQuestionAdapter,storyOrPoemAdapter;

    private HomeFragmentViewModel homeFragmentViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private SessionErrorViewModel sessionErrorViewModel;

    private TextView homeFragmentUsernameTextView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private NavController navController;

    private volatile AtomicInteger CONTENT_RETRIEVED = new AtomicInteger(0);

    private boolean NO_CONTENT_INVOKED = false;

    private volatile long REFRESHING_STARTING_TIME = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_bottom_home, container, false);

        // Getting viewModels
        homeFragmentViewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // Unhiding bottom navigation view if hidden
        sessionErrorViewModel.setIsToHideBottomNavView(false);

        // init views
        homeFragmentUsernameTextView = (TextView) rootView.findViewById(R.id.home_fragment_username_text_view);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.home_feed_swipe_to_refresh_layout);
        imageOrMemesViewStub = (ViewStub) rootView.findViewById(R.id.home_images_or_memes_view_stub);
        dreamOrIdeasViewStub = (ViewStub) rootView.findViewById(R.id.home_dreams_or_ideas_view_stub);
        blogOrNewsViewStub = (ViewStub) rootView.findViewById(R.id.home_blogs_or_news_view_stub);
        noContentViewStub = (ViewStub) rootView.findViewById(R.id.home_feed_no_content_view_stub);
        quotesOrQuestionViewStub = (ViewStub) rootView.findViewById(R.id.home_quotes_or_question_view_stub);
        storyOrPoemViewStub = (ViewStub) rootView.findViewById(R.id.home_story_or_poem_view_stub);
        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.bottom_home_nested_scroll_view);
        nestedScrollView.setScrollY(currentScrollPos);


        // Gritting user with his username
        homeFragmentUsernameTextView.setText(getString(R.string.greet_user_with_username, ParseUser.getCurrentUser().getUsername()));


        // Observing view model data's
        // Observing how to refresh the data
        homeFragmentViewModel.getRefreshType().observe(getViewLifecycleOwner(), refreshType->{
            if (refreshType == HomeFragmentViewModel.REFRESH_NETWORK_DATA){
                if (IsInternetAvailable.isInternetAvailable(mContext)) {
                    swipeRefreshLayout.setRefreshing(true);
                    refreshAllContent();
                }
                else
                    Snackbar.make(homeFragmentUsernameTextView,"No internet connection available.",3000)
                            .setBackgroundTint(ContextCompat.getColor(mContext,R.color.red_help_people))
                            .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                            .show();
            }
        });

        // Observing Images Or Memes Data
        homeFragmentViewModel.getImageOrMemeData().observe(getViewLifecycleOwner(), imagesOrMemes -> {
            if (imagesOrMemes == null)
                return;

            final List<ParseObject> imagesList = new ArrayList<>();
            final List<ParseObject> memesList = new ArrayList<>();
            for (int i = 0; i < imagesOrMemes.size(); i++) {
                ParseObject object = imagesOrMemes.get(i);
                if ((object.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_IMAGE)  &&  imagesList.size() <= 15)
                    imagesList.add(object);
                else if (memesList.size() <= 15)
                    memesList.add(object);
                else
                    break;
            }
            final List<HomeFeedsModel> homeFeedsModels = new ArrayList<>();
            if (imagesList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.there_images_home_fragment),
                        Constants.UPLOAD_IMAGE, imagesList));
            }
            if (memesList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.memes_and_comedy_home_fragment),
                        Constants.UPLOAD_MEMES_AND_COMEDY, memesList));
            }

            setImageOrMemeAdapter(homeFeedsModels);

            if ((!(imagesList.size() > 0)  || !(memesList.size() > 0)) &&  !NO_CONTENT_INVOKED)
                setNoContentAdapter();
        });


        // Observing Blog Or News Data
        homeFragmentViewModel.getBlogOrNewsData().observe(getViewLifecycleOwner(), blogOrNews -> {
            if (blogOrNews == null)
                return;

            final List<ParseObject> blogLists = new ArrayList<>();
            final List<ParseObject> newsLists = new ArrayList<>();
            for (int i=0; i<blogOrNews.size(); i++){
                ParseObject object = blogOrNews.get(i);
                if ((object.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_BLOG)  &&  blogLists.size() <= 15)
                    blogLists.add(object);
                else if (newsLists.size() <= 15)
                    newsLists.add(object);
                else
                    break;
            }

            final List<HomeFeedsModel> homeFeedsModels = new ArrayList<>();
            if (blogLists.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.blog_home_fragment),
                        Constants.UPLOAD_BLOG, blogLists));
            }
            if (newsLists.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.news_and_updates_home_fragment),
                        Constants.UPLOAD_NEWS_AND_UPDATE, newsLists));
            }

            setBlogOrNewsAdapter(homeFeedsModels);

            if ((!(blogLists.size() > 0)  || !(newsLists.size() > 0)) &&  !NO_CONTENT_INVOKED)
                setNoContentAdapter();
        });

        // Observing Dream Or Ideas Data
        homeFragmentViewModel.getDreamsOrIdeasData().observe(getViewLifecycleOwner(), dreamOrIdeas -> {
            if (dreamOrIdeas == null)
                return;

            final List<ParseObject> dreamLists = new ArrayList<>();
            final List<ParseObject> ideasLists = new ArrayList<>();
            for (int i=0; i<dreamOrIdeas.size(); i++){
                ParseObject object = dreamOrIdeas.get(i);
                if ((object.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_DREAM)  &&  dreamLists.size() <= 15)
                    dreamLists.add(object);
                else  if (ideasLists.size() <= 15)
                    ideasLists.add(object);
                else
                    break;
            }

            final List<HomeFeedsModel> homeFeedsModels = new ArrayList<>();
            if (dreamLists.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.dreams_home_fragment),
                        Constants.UPLOAD_DREAM, dreamLists));
            }
            if (ideasLists.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.ideas_home_fragment),
                        Constants.UPLOAD_IDEAS, ideasLists));
            }

            setDreamOrIdeasAdapter(homeFeedsModels);

            if ((!(dreamLists.size() > 0)  || !(ideasLists.size() > 0)) &&  !NO_CONTENT_INVOKED)
                setNoContentAdapter();

        });

        // Observing Quotes Or Question Data
        homeFragmentViewModel.getQuotesOrQuestionData().observe(getViewLifecycleOwner(),quotesOrQuestion->{
            if (quotesOrQuestion == null)
                return;

            final List<ParseObject> quotesList = new ArrayList<>();
            final List<ParseObject> questionList = new ArrayList<>();
            for (int i=0; i<quotesOrQuestion.size(); i++){
                ParseObject object = quotesOrQuestion.get(i);
                if ((object.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_QUOTE_AND_THOUGHTS)  &&  quotesList.size() <= 15)
                    quotesList.add(object);
                else if (questionList.size() <= 15)
                    questionList.add(object);
                else
                    break;
            }

            final List<HomeFeedsModel> homeFeedsModels = new ArrayList<>();
            if (quotesList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.quotes_and_thought_home_fragment),
                        Constants.UPLOAD_QUOTE_AND_THOUGHTS, quotesList));
            }
            if (questionList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.answer_there_question),
                        Constants.UPLOAD_GIVE_QUESTION, questionList));
            }

            setQuotesOrQuestionAdapter(homeFeedsModels);

            if ((!(quotesList.size() > 0)  || !(questionList.size() > 0)) &&  !NO_CONTENT_INVOKED)
                setNoContentAdapter();

        });

        // Observing Story Or Poem
        homeFragmentViewModel.getStoryOrPoemData().observe(getViewLifecycleOwner(), storyOrPoemData -> {
            if (storyOrPoemData == null)
                return;

            final List<ParseObject> storyList = new ArrayList<>();
            final List<ParseObject> poemList = new ArrayList<>();
            for (int i = 0; i < storyOrPoemData.size(); i++) {
                ParseObject object = storyOrPoemData.get(i);
                if ((object.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_STORY) && storyList.size() <= 15)
                    storyList.add(object);
                else if (poemList.size() <= 15)
                    poemList.add(object);
                else
                    break;
            }

            final List<HomeFeedsModel> homeFeedsModels = new ArrayList<>();
            if (storyList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.stories_home_fragment),
                        Constants.UPLOAD_STORY, storyList));
            }
            if (poemList.size() > 0){
                homeFeedsModels.add(new HomeFeedsModel(getString(R.string.poems_home_fragment),
                        Constants.UPLOAD_POEM, poemList));
            }

            setStoryOrPoemAdapter(homeFeedsModels);

            if ((!(storyList.size() > 0)  || !(poemList.size() > 0)) &&  !NO_CONTENT_INVOKED)
                setNoContentAdapter();

        });


        return rootView;
    }


    // Setting Image Or Memes Adapter
    private void setImageOrMemeAdapter(@NonNull final List<HomeFeedsModel> feedsModelList){
        // Inflating images or memes view stub
        if (imageOrMemesView == null)
            imageOrMemesView = imageOrMemesViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) imageOrMemesView.findViewById(R.id.home_images_or_memes_recycler_view);
        if (imageOrMemesAdapter != null) {
            imageOrMemesAdapter.updateData(feedsModelList);
            return;
        }

        imageOrMemesAdapter = new HomeFeedsAdapter(mContext,feedsModelList,Constants.UPLOAD_IMAGE,this,this);
        feedRecyclerView.setAdapter(imageOrMemesAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }



    // Setting Dream Or Ideas Adapter
    private void setDreamOrIdeasAdapter(@NonNull final List<HomeFeedsModel> homeFeedsModels){
        // Inflating dream or ideas view stub
        if (dreamOrIdeasView == null)
            dreamOrIdeasView = dreamOrIdeasViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) dreamOrIdeasView.findViewById(R.id.home_dream_ideas_recycler_view);
        if (dreamOrIdeasAdapter != null){
            dreamOrIdeasAdapter.updateData(homeFeedsModels);
            return;
        }

        dreamOrIdeasAdapter = new HomeFeedsAdapter(mContext,homeFeedsModels,Constants.UPLOAD_DREAM,this,this);
        feedRecyclerView.setAdapter(dreamOrIdeasAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    // Setting Blog Or News Adapter
    private void setBlogOrNewsAdapter(@NonNull final List<HomeFeedsModel> modelList){
        // Inflating blog or news view stub
        if (blogOrNewsView == null)
            blogOrNewsView = blogOrNewsViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) blogOrNewsView.findViewById(R.id.home_blog_or_news_recycler_view);
        if (blogOrNewsAdapter != null){
            blogOrNewsAdapter.updateData(modelList);
            return;
        }

        blogOrNewsAdapter = new HomeFeedsAdapter(mContext,modelList,Constants.UPLOAD_BLOG,this,this);
        feedRecyclerView.setAdapter(blogOrNewsAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    // Setting Quotes Or Question Adapter
    private void setQuotesOrQuestionAdapter(@NonNull final List<HomeFeedsModel> modelList){
        // Inflating Quotes Or Question view stub
        if (quotesOrQuestionView == null)
            quotesOrQuestionView = quotesOrQuestionViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) quotesOrQuestionView.findViewById(R.id.home_quotes_question_recycler_view);
        if (quotesOrQuestionAdapter != null){
            quotesOrQuestionAdapter.updateData(modelList);
            return;
        }

        quotesOrQuestionAdapter = new HomeFeedsAdapter(mContext,modelList,Constants.UPLOAD_QUOTE_AND_THOUGHTS,this,this);
        feedRecyclerView.setAdapter(quotesOrQuestionAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    // Setting Story Or Poem Adapter
    private void setStoryOrPoemAdapter(@NonNull final List<HomeFeedsModel> modelList){
        // Inflating Story Or Poem View Stub
        if (storyOrPoemView == null)
            storyOrPoemView = storyOrPoemViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) storyOrPoemView.findViewById(R.id.home_story_or_poem_recycler_view);
        if (storyOrPoemAdapter != null){
            storyOrPoemAdapter.updateData(modelList);
            return;
        }

        storyOrPoemAdapter = new HomeFeedsAdapter(mContext,modelList,Constants.UPLOAD_STORY,this,this);
        feedRecyclerView.setAdapter(storyOrPoemAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    // Setting No content adapter
    private void setNoContentAdapter(){
        NO_CONTENT_INVOKED = true;

        //Inflating no content view
        if (noContentView == null)
            noContentView = noContentViewStub.inflate();

        RecyclerView feedRecyclerView = (RecyclerView) noContentView.findViewById(R.id.home_feed_no_content_recycler_view);
        NoFeedsAdapter noFeedsAdapter = new NoFeedsAdapter(mContext,getNoContentList(),this);
        feedRecyclerView.setAdapter(noFeedsAdapter);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false));
    }


    // Here position is content type
    @Override
    public void adapterPosition(int position) {
        sessionErrorViewModel.setIsToHideBottomNavView(true);

        // getting selected position view model
        WritingUploadViewModel viewModel = new ViewModelProvider(requireActivity()).get(WritingUploadViewModel.class);
        viewModel.setPosition(position);

        if (navController != null)
        {
            if (position <= 14)
            {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.BUNDLE_CLEAR_ATTACHED_IMAGES,true);

                if (position == 0  ||  position == 5)
                    navController.navigate(R.id.action_bottomHomeFragment_to_mobileImagesFragment,bundle);

                else {
                    MultiImageViewModel multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);
                    multiImageViewModel.setPreSelectedImages(new ArrayList<>());
                    navController.navigate(R.id.action_bottomHomeFragment_to_writingUploadFragment);
                }
            }
        }
    }

    // Getting no content adapter list
    private List<NoFeeds> getNoContentList(){

        final List<NoFeeds> noFeedsList = new ArrayList<>();

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext,R.drawable.no_image_production_icon),
                getString(R.string.there_images_home_fragment),
                getString(R.string.image_upload_message),
                0));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_blogs_found_production_icon),
                getString(R.string.blog_home_fragment),
                getString(R.string.blog_upload_message),
                1));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_dreams_production_icon),
                getString(R.string.dreams_home_fragment),
                getString(R.string.dream_upload_message),
                2));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_quotes_production_icon),
                getString(R.string.quotes_and_thought_home_fragment),
                getString(R.string.quotes_upload_message),
                3));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_news_production_icon),
                getString(R.string.news_and_updates_home_fragment),
                getString(R.string.news_upload_message),
                4));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_meme_production_icon),
                getString(R.string.memes_and_comedy_home_fragment),
                getString(R.string.memes_upload_message),
                5));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_storys_production_icon),
                getString(R.string.stories_home_fragment),
                getString(R.string.story_upload_message),
                6));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_poem_production_icon),
                getString(R.string.poems_home_fragment),
                getString(R.string.poem_upload_message),
                7));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_questions_production_icon),
                getString(R.string.ask_a_question_upload_fragment),
                getString(R.string.question_upload_message),
                9));

        noFeedsList.add(new NoFeeds(ContextCompat.getDrawable(mContext, R.drawable.no_idea_production_icon),
                getString(R.string.ideas_home_fragment),
                getString(R.string.ideas_upload_message),
                10));

        return noFeedsList;
    }


    // When object and user is null then current is position is refered as content type
    @Override
    public void selectedPost(int currentPos,ParseObject object, ParseUser user) {
        if (object == null && user == null){
            showMorePost(currentPos);
            return;
        }

        if (user != null)
            navToUserProfFrag(user);

        if (object == null)
            return;

        final int contentType = object.getInt(ParseConstants.CONTENT_TYPE);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT,object);
        switch (contentType){
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                navToImageFeedFrag(contentType);
                break;
            }
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                feedsArgsViewModel.addFullFeedsArg(bundle);
                navController.navigate(R.id.action_bottomHomeFragment_to_blogOrNewsFullFeedFragment);
                break;
            }
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_IDEAS:{
                feedsArgsViewModel.addFullFeedsArg(bundle);
                navController.navigate(R.id.action_bottomHomeFragment_to_dreamOrIdeasFullFragment);
                break;
            }
            case Constants.UPLOAD_STORY:{
                feedsArgsViewModel.addFullFeedsArg(bundle);
                navController.navigate(R.id.action_bottomHomeFragment_to_storyFeedFullFragment);
                break;
            }
            case Constants.UPLOAD_POEM:{
                feedsArgsViewModel.addFullFeedsArg(bundle);
                navController.navigate(R.id.action_bottomHomeFragment_to_poemFeedFullFragment);
                break;
            }
            case Constants.UPLOAD_GIVE_QUESTION:{
                feedsArgsViewModel.addFullFeedsArg(bundle);
                navController.navigate(R.id.action_bottomHomeFragment_to_questionsFeedFragment);
                break;
            }
        }
    }

    private void showMorePost(final int contentType){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_CONTENT_TYPE,contentType);
        bundle.putInt(Constants.BUNDLE_HOME_CALL,Constants.HOME_CALL);
        feedsArgsViewModel.addFragArg(bundle);
        navController.navigate(R.id.action_bottomHomeFragment_to_writingFeedsFragment,bundle);
    }

    // Here position means content type
    @Override
    public void imageSelected(int position, ParseUser parseUser) {
        if (parseUser != null)
            navToUserProfFrag(parseUser);
        else
            navToImageFeedFrag(position);
    }

    private void navToImageFeedFrag(final int contentType){
        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_CONTENT_TYPE,contentType);
        bundle.putInt(Constants.BUNDLE_HOME_CALL,Constants.HOME_CALL);
        feedsArgsViewModel.addFragArg(bundle);
        navController.navigate(R.id.action_bottomHomeFragment_to_imagesFeedFragment);
    }

    // Navigate to user profile fragment
    private void navToUserProfFrag(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
            navController.navigate(R.id.action_bottomHomeFragment_to_bottomProfileFragment);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_bottomHomeFragment_to_usersProfileFragment);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        swipeRefreshLayout.setOnRefreshListener(()->{
            homeFragmentViewModel.setRefreshData(HomeFragmentViewModel.REFRESH_NETWORK_DATA);
        });
    }

    // Refreshing the parse objects using network
    private void refreshAllContent(){
        REFRESHING_STARTING_TIME = Calendar.getInstance().getTimeInMillis();
        App.getScheduledExecutorService().execute(this);
        getUserFeedRelation();
        homeFragmentViewModel.setRefreshData(HomeFragmentViewModel.NO_REFRESHING);
    }

    private void getUserFeedRelation(){
        final ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.fetchInBackground(((object, e) -> {
            if (e == null){
                final ParseRelation<ParseObject> userRelation = object.getRelation(ParseConstants.USER_HOME_FEED_INCLUSIVE);
                retrieveFromNetwork(userRelation);
            }
            else if (e.getCode() == ParseException.INVALID_SESSION_TOKEN){
                sessionErrorViewModel.isSessionExpired(true);
            }
        }));
    }

    private void retrieveFromNetwork(@NonNull final ParseRelation<ParseObject> userFeedRelation){
        networkImagesOrMemes(userFeedRelation);
        networkBlogOrNews(userFeedRelation);
        networkDreamsOrIdeas(userFeedRelation);
        networkQuotesOrQuestion(userFeedRelation);
        networkStoryOrPoem(userFeedRelation);
    }

    // Runnable for checking how many content types have retrieved from the network
    @Override
    public void run() {
        if (CONTENT_RETRIEVED.get() >= 5  || ((Calendar.getInstance().getTimeInMillis() - REFRESHING_STARTING_TIME) == 10000)){
            App.getMainThread().post(() -> {
                if (rootView != null)
                    swipeRefreshLayout.setRefreshing(false);
            });
            return;
        }
        App.getScheduledExecutorService().schedule(this,1000, TimeUnit.MILLISECONDS);
    }


    //--------------- Fetching data from local data store (This methods do not work for selectKeys parameter) ------------------------//
    // Getting images or memes
    private void localImagesOrMemes(){
        final ParseQuery<ParseObject> imageOrMemeQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        final List<Integer> contentType = new ArrayList<>();
        contentType.add(0);
        contentType.add(5);
        imageOrMemeQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        imageOrMemeQuery.fromLocalDatastore().findInBackground(((objects, e) -> {
            if (e == null){
                homeFragmentViewModel.setImagesOrMemesData(objects);
            }
        }));
    }

    // Getting blog or news
    private void localBlogOrNews(){
        final ParseQuery<ParseObject> blogOrNewsQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        final List<Integer> contentType = new ArrayList<>();
        contentType.add(1);
        contentType.add(4);
        blogOrNewsQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        blogOrNewsQuery.fromLocalDatastore().findInBackground(((objects, e) -> {
            if (e == null  && objects.size()>0)
                homeFragmentViewModel.setBlogOrNewsData(objects);
        }));
    }

    // Getting dream or ideas
    private void localDreamOrIdeas(){
        final ParseQuery<ParseObject> dreamOrIdeasQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        final List<Integer> contentType = new ArrayList<>();
        contentType.add(2);
        contentType.add(10);
        dreamOrIdeasQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        dreamOrIdeasQuery.fromLocalDatastore().findInBackground(((objects, e) -> {
            if (e == null && objects.size()>0)
                homeFragmentViewModel.setDreamsOrIdeasData(objects);
        }));
    }


    // Getting quotes or question
    private void localQuotesOrQuestion(){
        final ParseQuery<ParseObject> quotesOrQuestionQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        final List<Integer> contentType = new ArrayList<>();
        contentType.add(3);
        contentType.add(9);
        quotesOrQuestionQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        quotesOrQuestionQuery.fromLocalDatastore().findInBackground(((objects, e) -> {
            if (e == null && objects.size()>0)
                homeFragmentViewModel.setQuotesOrQuestionData(objects);

        }));
    }

    // Getting story or poem
    private void localStoryOrPoem(){
        final ParseQuery<ParseObject> storyOrPoemQuery = ParseQuery.getQuery(ParseConstants.USERS_POSTS_CLASS_NAME);
        final List<Integer> contentType = new ArrayList<>();
        contentType.add(6);
        contentType.add(7);
        storyOrPoemQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        storyOrPoemQuery.fromLocalDatastore().findInBackground(((objects, e) -> {
            if (e == null && objects.size()>0)
                homeFragmentViewModel.setStoryOrPoemData(objects);
        }));
    }
    // ---------------------- End fetching from local data store-----------------------------------------//



    //-------------------- Fetching Data from Network--------------------------------//
    // Getting images or memes data (All fields will be retrieved)
    private void networkImagesOrMemes(final ParseRelation<ParseObject> userFeedRelation){
        final ParseQuery<ParseObject> imagesOrMemes = userFeedRelation.getQuery();

        final List<Integer> contentType = new ArrayList<>();
        contentType.add(Constants.UPLOAD_IMAGE);
        contentType.add(Constants.UPLOAD_MEMES_AND_COMEDY);

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

        imagesOrMemes.include(ParseConstants.POST_OWNER);
        imagesOrMemes.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        imagesOrMemes.selectKeys(keys).findInBackground(((objects, e) -> {
            if (e == null)
                homeFragmentViewModel.setImagesOrMemesData(objects);
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }

            CONTENT_RETRIEVED.incrementAndGet();
        }));
    }

    // Getting blog or news
    private void networkBlogOrNews(final ParseRelation<ParseObject> userFeedRelation){
        final ParseQuery<ParseObject> blogOrNewsQuery = userFeedRelation.getQuery();

        final List<Integer> contentType = new ArrayList<>();
        contentType.add(Constants.UPLOAD_BLOG);
        contentType.add(Constants.UPLOAD_NEWS_AND_UPDATE);

        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.MULTI_IMAGE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.CONTENT_PREVIEW);

        blogOrNewsQuery.include(ParseConstants.POST_OWNER);
        blogOrNewsQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        blogOrNewsQuery.selectKeys(keys).findInBackground(((objects, e) -> {
            if (e == null)
                homeFragmentViewModel.setBlogOrNewsData(objects);
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }

            CONTENT_RETRIEVED.incrementAndGet();
        }));
    }

    // Getting dreams and ideas
    private void networkDreamsOrIdeas(final ParseRelation<ParseObject> userFeedRelation){
        final ParseQuery<ParseObject> dreamsOrIdeasQuery = userFeedRelation.getQuery();

        final List<Integer> contentType = new ArrayList<>();
        contentType.add(Constants.UPLOAD_DREAM);
        contentType.add(Constants.UPLOAD_IDEAS);

        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.CONTENT_PREVIEW);
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.SINGLE_IMAGE);

        dreamsOrIdeasQuery.include(ParseConstants.POST_OWNER);
        dreamsOrIdeasQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        dreamsOrIdeasQuery.selectKeys(keys).findInBackground(((objects, e) -> {
            if (e == null)
                homeFragmentViewModel.setDreamsOrIdeasData(objects);
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }

            CONTENT_RETRIEVED.incrementAndGet();
        }));
    }


    // Getting quotes or question(Full fields are retrieved)
    private void networkQuotesOrQuestion(final ParseRelation<ParseObject> userFeedRelation){
        final ParseQuery<ParseObject> quotesOrQuestionQuery = userFeedRelation.getQuery();

        final List<Integer> contentType = new ArrayList<>();
        contentType.add(Constants.UPLOAD_QUOTE_AND_THOUGHTS);
        contentType.add(Constants.UPLOAD_GIVE_QUESTION);

        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.SINGLE_IMAGE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.DESCRIPTION);
        keys.add(ParseConstants.LOCATION);
        keys.add(ParseConstants.LIKES_USERNAMES);
        keys.add(ParseConstants.TOTAL_CONNECTED_USERS);
        keys.add(ParseConstants.TOTAL_POST_LIKES);
        keys.add(ParseConstants.TOTAL_POST_COMMENTS);

        quotesOrQuestionQuery.include(ParseConstants.POST_OWNER);
        quotesOrQuestionQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        quotesOrQuestionQuery.selectKeys(keys).findInBackground(((objects, e) -> {
            if (e == null)
                homeFragmentViewModel.setQuotesOrQuestionData(objects);
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }

            CONTENT_RETRIEVED.incrementAndGet();
        }));
    }

    // Getting story or poem
    private void networkStoryOrPoem(final ParseRelation<ParseObject> userFeedRelation){
        final ParseQuery<ParseObject> storyOrPoemQuery = userFeedRelation.getQuery();

        final List<Integer> contentType = new ArrayList<>();
        contentType.add(Constants.UPLOAD_STORY);
        contentType.add(Constants.UPLOAD_POEM);

        final List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.POST_OWNER);
        keys.add(ParseConstants.CONTENT_TYPE);
        keys.add(ParseConstants.SMALL_TEXT_ONE);
        keys.add(ParseConstants.SMALL_TEXT_TWO);
        keys.add(ParseConstants.CONTENT_PREVIEW);
        keys.add(ParseConstants.SINGLE_IMAGE);

        storyOrPoemQuery.include(ParseConstants.POST_OWNER);
        storyOrPoemQuery.whereContainedIn(ParseConstants.CONTENT_TYPE,contentType);
        storyOrPoemQuery.selectKeys(keys).findInBackground(((objects, e) -> {
            if (e == null)
                homeFragmentViewModel.setStoryOrPoemData(objects);
            else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }

            CONTENT_RETRIEVED.incrementAndGet();
        }));
    }


    // --------------------------- End retrieving from network -----------------------------------------//

    @Override
    public void onDestroyView() {
        currentScrollPos = nestedScrollView.getScrollY();
        super.onDestroyView();
        NO_CONTENT_INVOKED = false;
        rootView = null;
        imageOrMemesAdapter = null;
        blogOrNewsAdapter = null;
        quotesOrQuestionAdapter = null;
        storyOrPoemAdapter = null;
        dreamOrIdeasAdapter = null;
        homeFragmentUsernameTextView = null;
        swipeRefreshLayout = null;
        imageOrMemesViewStub = null;
        dreamOrIdeasViewStub = null;
        blogOrNewsViewStub = null;
        quotesOrQuestionViewStub = null;
        storyOrPoemViewStub = null;
        noContentViewStub = null;
        imageOrMemesView = null;
        dreamOrIdeasView = null;
        blogOrNewsView = null;
        quotesOrQuestionView = null;
        storyOrPoemView = null;
        noContentView = null;
        nestedScrollView = null;
    }
}