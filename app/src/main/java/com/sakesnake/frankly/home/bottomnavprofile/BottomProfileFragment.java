package com.sakesnake.frankly.home.bottomnavprofile;

import android.content.Context;
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

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.ParseViewModel;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.MyUploads;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.MyUploadsAdapter;
import com.sakesnake.frankly.home.bottomnavprofile.myuploads.UploadsAdapterCallback;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.home.profileSupporters.ProfileSupportersFragment;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.appbar.AppBarLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.boltsinternal.Task;

import java.util.ArrayList;

public class BottomProfileFragment extends Fragment implements
        AppBarLayout.OnOffsetChangedListener,View.OnTouchListener, UploadsAdapterCallback {
    private View rootView;

    private TextView usernameTextView,originalNameTextView,profileDescriptionTextView,
            tWatchersTextView,tWatchingTextView,tLikesTextView,tLikedProfileTextView;

    private LinearLayout watchersLinearLayout,watchingLinearLayout,likesLinearLayout,likedProfileLinearLayout;

    private final ArrayList<MyUploads> myUploadsArrayList = new ArrayList<>();

    private RecyclerView myUploadsRecyclerView;

    private AppBarLayout appBarLayout;

    private SwipeRefreshLayout profileFragmentSwipeRefreshLayout;

    private Context mContext;

    private ImageView accountSettingsProfileFragment,profilePicImageView,profileBannerImageView;

    private Button editProfileBtn;

    private ImageButton changeProfileBannerImageBtn;

    private NavController navController;

    private MyUploadsAdapter myUploadsAdapter;

    private SessionErrorViewModel sessionErrorViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private ParseViewModel parseViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bottom_profile, container, false);

        // Getting view models
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
        parseViewModel = new ViewModelProvider(requireActivity()).get(ParseViewModel.class);
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);


        //filling myUploads array list
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

        //init views
        myUploadsRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_uploads_profile_fragment_recycler_view);
        accountSettingsProfileFragment = (ImageView) rootView.findViewById(R.id.account_setting_profile_fragment_image_view);
        editProfileBtn = (Button) rootView.findViewById(R.id.edit_profile_home_fragment_image_view);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.fragment_bottom_profile_app_bar_layout);
        profileFragmentSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_bottom_profile_swipe_to_refresh_layout);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_text_view);
        originalNameTextView = (TextView) rootView.findViewById(R.id.profile_original_name_text_view);
        profileDescriptionTextView = (TextView) rootView.findViewById(R.id.profile_description_text_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_photo_image_view);
        profileBannerImageView = (ImageView) rootView.findViewById(R.id.profile_banner_image_view);
        changeProfileBannerImageBtn = (ImageButton) rootView.findViewById(R.id.change_banner_image_btn);
        tWatchersTextView = (TextView) rootView.findViewById(R.id.total_watchers_text_view);
        tWatchingTextView = (TextView) rootView.findViewById(R.id.total_watching_text_view);
        watchersLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_watchers_linear_layout);
        watchingLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_watch_list_linear_layout);
        tLikesTextView = (TextView) rootView.findViewById(R.id.total_likes_text_view);
        tLikedProfileTextView = (TextView) rootView.findViewById(R.id.total_liked_profile_text_view);
        likesLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_likes_linear_layout);
        likedProfileLinearLayout = (LinearLayout) rootView.findViewById(R.id.my_liked_profile_linear_layout);


        // setting app bar offset change listener
        appBarLayout.addOnOffsetChangedListener(this);


        // filling myUploads recycler view
        myUploadsAdapter = new MyUploadsAdapter(mContext, myUploadsArrayList,this);
        myUploadsRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        myUploadsRecyclerView.setAdapter(myUploadsAdapter);

        // Observing Parse View model
        parseViewModel.getCurrentUser().observe(getViewLifecycleOwner(),parseUser -> {
            if (parseUser != null)
                setViewsWithDetails(parseUser);
            else
                getLatestUserData(ParseUser.getCurrentUser().getObjectId());
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigate to edit profile fragment
        editProfileBtn.setOnClickListener(view1 -> navController.navigate(R.id.action_bottomProfileFragment_to_editProfileFragment));
        changeProfileBannerImageBtn.setOnClickListener(view1 -> navController.navigate(R.id.action_bottomProfileFragment_to_editProfileFragment));

        watchingLinearLayout.setOnClickListener(view1 -> navigateToProfileSupporters(ProfileSupportersFragment.QUERY_WATCHING,true));
        watchersLinearLayout.setOnClickListener(view1 -> navigateToProfileSupporters(ProfileSupportersFragment.QUERY_WATCHERS,false));

        likedProfileLinearLayout.setOnClickListener(view1 -> navigateToProfileSupporters(ProfileSupportersFragment.QUERY_LIKED,true));


        // navigating to account settings
        accountSettingsProfileFragment.setOnClickListener(v -> {
            navController.navigate(R.id.action_bottomProfileFragment_to_profileSettingsFragment);
        });

        // Listening on swipe listener and updating Parse.Current user
        profileFragmentSwipeRefreshLayout.setOnRefreshListener(() -> {
            updateUserData(parseViewModel.getLatestUserData());
        });

    }

    // Navigate to profile supporters fragment
    private void navigateToProfileSupporters(int queryConstant, boolean grantDeletePermission){
        ParseUser user;
        if ((user = parseViewModel.getLatestUserData()) == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_SAVED_PARSE_USER,user);
        bundle.putInt(Constants.BUNDLE_PROFILE_QUERY_CONSTANT,queryConstant);
        bundle.putBoolean(Constants.BUNDLE_GRANT_DELETE_PERMISSION,grantDeletePermission);
        feedsArgsViewModel.insertSupporterFragArg(bundle);
        navController.navigate(R.id.action_bottomProfileFragment_to_profileSupportersFragment);
    }


    // Here position is content type
    @Override
    public void getContentType(int position) {
        final Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_CONTENT_TYPE,position);
        bundle.putInt(Constants.BUNDLE_USER_CALL,Constants.USER_CALL);
        bundle.putParcelable(Constants.BUNDLE_PARSE_USER,ParseUser.getCurrentUser());
        feedsArgsViewModel.addFragArg(bundle);

        switch (position){
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                navController.navigate(R.id.action_bottomProfileFragment_to_imagesFeedFragment);
                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:
            case Constants.UPLOAD_GIVE_QUESTION:
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                navController.navigate(R.id.action_bottomProfileFragment_to_writingFeedsFragment);
                break;
            }
        }
    }


    // Searching user using object id
    private void getLatestUserData(String objectId){
        profileFragmentSwipeRefreshLayout.setRefreshing(true);
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.fromLocalDatastore().getInBackground(objectId).continueWithTask((task -> {
            if (task.getError() == null  &&  task.getResult() != null)
                setViewsWithDetails(task.getResult());
            else
                setDefaultData();

            return parseQuery.fromNetwork().getInBackground(objectId);

        }),Task.UI_THREAD_EXECUTOR).continueWithTask((task -> {
            if (task.getError() == null && task.getResult() != null) {
                ParseUser.unpinAllInBackground(e -> {
                    if (e == null) {
                        ParseUser latestUserData = task.getResult();
                        latestUserData.pinInBackground();
                        parseViewModel.setCurrentUser(latestUserData);
                    }
                    else if(e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                        feedsArgsViewModel.clearAllArgs();
                        sessionErrorViewModel.isSessionExpired(true);
                    }
                });
            }
            return task;
        }),Task.UI_THREAD_EXECUTOR);
    }

    private void updateUserData(final ParseUser objectToUpdate){
        if (objectToUpdate == null) {
            profileFragmentSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Getting latest ParseObject and then typecasting it to ParseUser
        objectToUpdate.fetchInBackground((parseObject,e)->{
            if (e == null)
                parseViewModel.setCurrentUser((ParseUser) parseObject);
            else{
                if (e.getCode() == ParseException.INVALID_SESSION_TOKEN)
                    sessionErrorViewModel.isSessionExpired(true);
            }
        });
    }


    // filling views with parse user details
    private void setViewsWithDetails(@NonNull final ParseUser user){
        usernameTextView.setText(user.getUsername());
        originalNameTextView.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));
        tWatchersTextView.setText(getString(R.string.number_to_string,user.getInt(ParseConstants.PROFILE_TOTAL_WATCHERS)));
        tWatchingTextView.setText(getString(R.string.number_to_string,user.getInt(ParseConstants.PROFILE_TOTAL_WATCHING)));
        tLikesTextView.setText(getString(R.string.number_to_string,user.getInt(ParseConstants.PROFILE_TOTAL_LIKES)));
        tLikedProfileTextView.setText(getString(R.string.number_to_string,user.getInt(ParseConstants.PROFILE_TOTAL_LIKED_PROFILE)));

        // Getting profile description
        String description = user.getString(ParseConstants.PROFILE_DESCRIPTION);
        if (description != null  &&  !(description.equals(""))) {
            profileDescriptionTextView.setVisibility(View.VISIBLE);
            profileDescriptionTextView.setText(DocNormalizer.htmlToNormal(description));
        }
        else {
            profileDescriptionTextView.setVisibility(View.GONE);
        }

        final ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null) {
            Glide.with(mContext).load(profilePic.getUrl()).centerCrop().into(profilePicImageView);
        }

        final ParseFile profileBanner = user.getParseFile(ParseConstants.PROFILE_BANNER);
        if (profileBanner != null) {
            Glide.with(mContext).load(profileBanner.getUrl())
                    .placeholder(ContextCompat.getDrawable(mContext, R.drawable.profile_default_banner))
                    .centerCrop()
                    .into(profileBannerImageView);
        }
        else {
            Glide.with(mContext)
                    .load(ContextCompat.getDrawable(mContext, R.drawable.profile_default_banner))
                    .centerCrop()
                    .into(profileBannerImageView);
        }

        profileFragmentSwipeRefreshLayout.setRefreshing(false);
    }


    // This method will be invoked if there is no data in localDataStore And No data retrieve from network also
    private void setDefaultData(){
        profileFragmentSwipeRefreshLayout.setRefreshing(false);
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null)
            setViewsWithDetails(user);
        else
            sessionErrorViewModel.isSessionExpired(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myUploadsRecyclerView = null;
        accountSettingsProfileFragment = null;
        myUploadsAdapter = null;
        myUploadsArrayList.clear();
        appBarLayout = null;
        profileFragmentSwipeRefreshLayout = null;
        rootView = null;
        usernameTextView = null;
        originalNameTextView = null;
        profileDescriptionTextView = null;
        profilePicImageView = null;
        profileBannerImageView = null;
        changeProfileBannerImageBtn = null;
        tWatchersTextView = null;
        tWatchingTextView = null;
        watchersLinearLayout = null;
        watchingLinearLayout = null;
        tLikesTextView = null;
        tLikedProfileTextView = null;
        likesLinearLayout = null;
        likedProfileLinearLayout = null;
        editProfileBtn = null;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        // Disabling swipe to refresh when offset is not 0 or when app bar is not on its default position
        profileFragmentSwipeRefreshLayout.setEnabled(verticalOffset == 0);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int event = motionEvent.getActionMasked();
        switch (event){
            case MotionEvent.ACTION_DOWN:{
                view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(0).start();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                view.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                break;
            }
        }
        return true;
    }
}