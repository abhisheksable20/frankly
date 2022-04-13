package com.sakesnake.frankly.home.postfeeds;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.RandomColorGenerator;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.home.postfeeds.postMoreDetails.PostDetailsViewModel;
import com.sakesnake.frankly.home.postfeeds.postMoreDetails.PostMoreInfoFragment;
import com.sakesnake.frankly.parsedatabase.BooleanParseCallback;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.sakesnake.frankly.parsedatabase.ParseObjectCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class DreamOrIdeasFullFragment extends Fragment implements ParseObjectCallback, BooleanParseCallback {

    private View rootView;

    private Toolbar fragmentToolbar;

    private ImageView profilePicImageView, likeImageView, commentImageView,shareImageView, locationImageView,contentBackgroundImageView;

    private TextView usernameTextView, postingTimeTextView, mainContentTextView, totalLikesTextView,
            totalCommentsTextView, contentDescriptionTextView, totalConnectedUsersTextView, locationTextView,isDreamMatchingHeadingTextView;

    private Button postMoreOptionBtn;

    private ProgressBar progressBar;

    private int contentType = -1;

    private ParseObject currentParseObject = null;

    private boolean postLikedByCurrent = false;

    private NavController navController;

    private FeedsArgsViewModel feedsArgsViewModel;

    private PostDetailsViewModel postDetailsViewModel;

    private CharSequence mainContentText;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_dream_or_ideas_full, container, false);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);


        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);


        // init views
        initView();
        initFragArgs(feedsArgsViewModel.getLastFullFeedArg());

        return rootView;
    }

    // init all fragment views
    private void initView(){
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.dream_or_idea_feed_fragment_toolbar);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_pic_dream_or_idea_feed_fragment_image_view);
        likeImageView = (ImageView) rootView.findViewById(R.id.feed_like_user_post_image_view);
        commentImageView = (ImageView) rootView.findViewById(R.id.feed_comment_user_post_image_view);
        shareImageView = (ImageView) rootView.findViewById(R.id.feed_share_user_post_image_view);
        locationImageView = (ImageView) rootView.findViewById(R.id.feed_user_post_location_image_view);
        contentBackgroundImageView = (ImageView) rootView.findViewById(R.id.background_dream_or_idea_feed_fragment_image_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_dream_or_idea_feed_fragment_text_view);
        postingTimeTextView = (TextView) rootView.findViewById(R.id.post_posting_timing_dream_or_idea_feed_fragment_text_view);
        mainContentTextView = (TextView) rootView.findViewById(R.id.content_dream_or_idea_feed_fragment_text_view);
        totalLikesTextView = (TextView) rootView.findViewById(R.id.feed_total_likes_text_view);
        totalCommentsTextView = (TextView) rootView.findViewById(R.id.feed_total_comments_text_view);
        contentDescriptionTextView = (TextView) rootView.findViewById(R.id.feed_user_post_description_text_view);
        totalConnectedUsersTextView = (TextView) rootView.findViewById(R.id.feed_total_connected_users_text_view);
        locationTextView = (TextView) rootView.findViewById(R.id.feed_post_location_text_view);
        isDreamMatchingHeadingTextView = (TextView) rootView.findViewById(R.id.dream_match_heading_dream_feed_fragment_text_view);
        postMoreOptionBtn = (Button) rootView.findViewById(R.id.feed_user_post_more_details_button);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);

        disableMoreOptionsView();
    }

    // Disabling more options view
    private void disableMoreOptionsView(){
        contentDescriptionTextView.setEnabled(false);
        postMoreOptionBtn.setEnabled(false);
        totalConnectedUsersTextView.setEnabled(false);
    }


    private void initFragArgs(final Bundle bundle){
        if (bundle == null)
            return;

        // No need to refresh parse object
        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT);
            contentType = currentParseObject.getInt(ParseConstants.CONTENT_TYPE);
            if (bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER)){
                postLikedByCurrent = true;
            }
            setViews(currentParseObject);
        }
        // Refresh the parse object from network
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
            contentType = currentParseObject.getInt(ParseConstants.CONTENT_TYPE);
            FetchLatestData.fetchRemainingParseObjectFields(currentParseObject,this);
            FetchLatestData.isILikedThisPost(currentParseObject.getObjectId(),this);
        }

        setToolbarTitle();
        setReactionTypeImage();
    }

    private void setToolbarTitle(){
        switch (contentType){
            case Constants.UPLOAD_DREAM:{
                isDreamMatchingHeadingTextView.setVisibility(View.VISIBLE);
                fragmentToolbar.setTitle(getString(R.string.dreams_home_fragment));
                break;
            }
            case Constants.UPLOAD_IDEAS:{
                isDreamMatchingHeadingTextView.setVisibility(View.GONE);
                fragmentToolbar.setTitle(getString(R.string.ideas_home_fragment));
                break;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Share content with other apps
        shareImageView.setOnClickListener(view1 -> {
            if (currentParseObject == null)
                return;

            SharePost.sharePost(null,null,mainContentText.toString(),null,mContext);
        });

        // observing post more info view model
        postDetailsViewModel.getParseUserData().observe(getViewLifecycleOwner(), parseUser -> {
            if (parseUser != null){
                navigateToUserFrag(parseUser);
                postDetailsViewModel.setParseUserData(null);
            }
        });

        // Navigate to profile fragment
        usernameTextView.setOnClickListener(view1 -> {
            if (currentParseObject != null){
                final ParseUser parseUser = currentParseObject.getParseUser(ParseConstants.POST_OWNER);
                if (parseUser != null)
                    navigateToUserFrag(parseUser);
            }
        });

        postDetailsViewModel.getProcessToDo().observe(getViewLifecycleOwner(), processToDo -> {
            if (processToDo == PostDetailsViewModel.PROCESS_POST_DELETED){
                postDetailsViewModel.setProcessData(0);
                navController.navigateUp();
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (currentParseObject == null)
                    return;
                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,currentParseObject);
                navController.navigate(R.id.action_dreamOrIdeasFullFragment_to_reportPostFragment,bundle);
            }
        });

        // Navigating to back fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        fragmentToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.more_options_toolbar_menu)
                openMoreOptionModel();

            return false;
        });


        // Navigating bottom model sheet
        postMoreOptionBtn.setOnClickListener(view1 -> openMoreOptionModel());
        contentDescriptionTextView.setOnClickListener(view1 -> openMoreOptionModel());
        totalConnectedUsersTextView.setOnClickListener(view1 -> openMoreOptionModel());


        // navigating to post comment fragment
        commentImageView.setOnClickListener(view1 -> navigateToPostCommentFrag());
        totalCommentsTextView.setOnClickListener(view1 -> navigateToPostCommentFrag());

        // navigating to post likes fragment
        totalLikesTextView.setOnClickListener(view1 -> navigateToPostLikesFrag());


        // Liking the post
        likeImageView.setOnClickListener(view1 -> {
            // This post is already liked now disliking it
            if (postLikedByCurrent){
                postLikedByCurrent = false;
                likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
                int totalLike = (Integer.parseInt(totalLikesTextView.getText().toString())) - 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLike));
            }
            else{
                postLikedByCurrent = true;
                likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                int totalLike = (Integer.parseInt(totalLikesTextView.getText().toString())) + 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLike));
            }

            likeOrDislikePost();
        });
    }

    // navigating to user profile or bottom user fragment
    private void navigateToUserFrag(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            navController.navigate(R.id.action_dreamOrIdeasFullFragment_to_bottomProfileFragment);
            return;
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_dreamOrIdeasFullFragment_to_usersProfileFragment);
    }

    // Open bottom model sheet (Post more details)
    private void openMoreOptionModel(){
        if (currentParseObject == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT,currentParseObject);
        PostMoreInfoFragment postMoreInfoFragment = new PostMoreInfoFragment();
        postMoreInfoFragment.setArguments(bundle);
        postMoreInfoFragment.show(getParentFragmentManager(),PostMoreInfoFragment.FRAGMENT_TAG);
    }


    // navigating to post likes fragment
    private void navigateToPostLikesFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES,currentParseObject);
            feedsArgsViewModel.insertPostLikesArgStarting(bundle);
            navController.navigate(R.id.action_dreamOrIdeasFullFragment_to_postLikesFragment);
        }
    }


    // navigating to post comment fragment
    private void navigateToPostCommentFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT,currentParseObject);
            feedsArgsViewModel.insertPostCommentStartingArg(bundle);
            navController.navigate(R.id.action_dreamOrIdeasFullFragment_to_postCommentsFragment);
        }
    }


    // Liking or disliking the post
    private void likeOrDislikePost(){
        if (currentParseObject == null)
            return;

        FetchLatestData.likeOrDislikePost(currentParseObject.getObjectId(),this);
    }

    @Override
    public void isTrueFromServer(boolean isTrue, ParseException parseException) {
        if (parseException == null){
            postLikedByCurrent = isTrue;
            if (isTrue)
                likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
            else
                likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
        }
        else
            Log.e(Constants.TAG, "Parse cloud code error (isPostLikedByCurrent) OR (likeOrDislikePost): "+parseException.getCode());
    }

    // Setting how to react on post image view
    private void setReactionTypeImage(){
        switch (contentType){
            case Constants.UPLOAD_DREAM:{
                likeImageView.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.dream_match_icon));
                if (postLikedByCurrent)
                    likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));

                break;
            }
            case Constants.UPLOAD_IDEAS:{
                likeImageView.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.like_user_post_icon));
                if (postLikedByCurrent)
                    likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                break;
            }
            default:{
                likeImageView.setVisibility(View.GONE);
            }
        }
    }

    // Latest parse object fetched from network
    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null){
            currentParseObject = object;
            setViews(object);
        }
        else
            progressBar.setVisibility(View.GONE);
    }

    // Enabling more options views because fresh object has been fetched
    private void enableMoreOptionsView(){
        postMoreOptionBtn.setEnabled(true);
        contentDescriptionTextView.setEnabled(true);
        totalConnectedUsersTextView.setEnabled(true);
    }


    // Setting fragments view with details
    private void setViews(final ParseObject parseObject){

        enableMoreOptionsView();

        final ParseUser postOwner = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (postOwner != null){
            ParseFile parseFile = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (parseFile != null)
                Glide.with(mContext).load(parseFile.getUrl()).into(profilePicImageView);

            usernameTextView.setText(postOwner.getUsername());
        }

        final ParseFile parseFile = parseObject.getParseFile(ParseConstants.LARGE_CONTENT_TEXT);
        if (parseFile != null){
            parseFile.getDataInBackground((data, e) -> {
                if (e == null){
                    String str = new String(data);
                    mainContentText = DocNormalizer.htmlToNormal(str);
                    mainContentTextView.setText(mainContentText);

                }
                progressBar.setVisibility(View.GONE);
            });
        }

        String postDescription = parseObject.getString(ParseConstants.DESCRIPTION);
        if (postDescription != null  &&  postDescription.trim().length() > 0)
            contentDescriptionTextView.setText(DocNormalizer.htmlToNormal(postDescription));
        else
            contentDescriptionTextView.setVisibility(View.GONE);

        String location = parseObject.getString(ParseConstants.LOCATION);
        if (location != null && location.length() > 0)
            locationTextView.setText(location);
        else{
            locationImageView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
        }

        contentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
        postingTimeTextView.setText(getString(R.string.posted_timing,DateConversion.getPostTime(parseObject.getCreatedAt().getTime())));
        totalLikesTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        totalCommentsTextView.setText(getString(R.string.number_to_string_comment,parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        totalConnectedUsersTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));
    }



    @Override
    public void onDestroyView() {
        if (currentParseObject != null)
            feedsArgsViewModel.modifyLastParseObj(currentParseObject,postLikedByCurrent);
        super.onDestroyView();
        fragmentToolbar = null;
        profilePicImageView = null;
        likeImageView = null;
        commentImageView = null;
        shareImageView = null;
        locationImageView = null;
        usernameTextView = null;
        postingTimeTextView = null;
        mainContentTextView = null;
        totalLikesTextView = null;
        totalCommentsTextView = null;
        contentDescriptionTextView = null;
        totalConnectedUsersTextView = null;
        locationTextView = null;
        isDreamMatchingHeadingTextView = null;
        postMoreOptionBtn = null;
        progressBar = null;
        contentBackgroundImageView = null;
        rootView = null;
    }


    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastFullFeedArg();
        super.onDestroy();
    }

}