package com.sakesnake.frankly.home.postfeeds;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import com.sakesnake.frankly.porterstemmer.DocInfoCallback;
import com.sakesnake.frankly.porterstemmer.DocsInfo;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class StoryFeedFullFragment extends Fragment implements DocInfoCallback, ParseObjectCallback, BooleanParseCallback {

    private View rootView;

    private ImageView navigateBackImageView, refreshPostImageView, backgroundImageView, profilePicImageView,
            whiteColorImageView, greyColorImageView, textColorImageView, likePostImageView, commentPostImageView,
            sharePostImageView, locationImageView;

    private TextView contentKeywordsTextView, postedTimingTextView, titleTextView, authorTextView, mainContentTextView,
            usernameTextView, totalLikesTextView, totalCommentsTextView, contentDescriptionTextView, locationTextView,
            totalConnectedUserTextView;

    private Button moreDetailsBtn;

    private ProgressBar progressBar;

    private Context mContext;

    private ParseObject currentParseObject;

    private boolean likedByCurrentUser = false;

    private CharSequence mainContentText;

    private NavController navController;

    private FeedsArgsViewModel feedsArgsViewModel;

    private PostDetailsViewModel postDetailsViewModel;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_story_feed_full, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);


        // init fragment views
        initFragViews();

        // init fragment arguments
        initFragArg(feedsArgsViewModel.getLastFullFeedArg());

        return rootView;
    }

    // init fragment views
    private void initFragViews(){
        navigateBackImageView = (ImageView) rootView.findViewById(R.id.navigate_back_poem_or_story_fragment_image_view);
        refreshPostImageView = (ImageView) rootView.findViewById(R.id.refresh_poem_or_story_fragment_feed_image_view);
        backgroundImageView = (ImageView) rootView.findViewById(R.id.content_background_poem_or_story_image_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_image_poem_or_story_feed_fragment_image_view);
        whiteColorImageView = (ImageView) rootView.findViewById(R.id.text_color_white_image_view);
        greyColorImageView = (ImageView) rootView.findViewById(R.id.text_color_grey_image_view);
        textColorImageView = (ImageView) rootView.findViewById(R.id.text_color_story_feed_fragment_image_view);
        likePostImageView = (ImageView) rootView.findViewById(R.id.feed_like_user_post_image_view);
        commentPostImageView = (ImageView) rootView.findViewById(R.id.feed_comment_user_post_image_view);
        sharePostImageView = (ImageView) rootView.findViewById(R.id.feed_share_user_post_image_view);
        locationImageView = (ImageView) rootView.findViewById(R.id.feed_user_post_location_image_view);
        contentKeywordsTextView = (TextView) rootView.findViewById(R.id.keywords_of_poem_or_story_feed_fragment_text_view);
        postedTimingTextView = (TextView) rootView.findViewById(R.id.post_posting_timing_poem_or_story_feed_fragment_text_view);
        titleTextView = (TextView) rootView.findViewById(R.id.title_poem_or_story_feed_fragment_text_view);
        authorTextView = (TextView) rootView.findViewById(R.id.author_poem_or_story_feed_fragment_text_view);
        mainContentTextView = (TextView) rootView.findViewById(R.id.content_poem_or_story_feed_fragment_text_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_poem_or_story_feed_fragment_text_view);
        totalLikesTextView = (TextView) rootView.findViewById(R.id.feed_total_likes_text_view);
        totalCommentsTextView = (TextView) rootView.findViewById(R.id.feed_total_comments_text_view);
        contentDescriptionTextView = (TextView) rootView.findViewById(R.id.feed_user_post_description_text_view);
        locationTextView = (TextView) rootView.findViewById(R.id.feed_post_location_text_view);
        totalConnectedUserTextView = (TextView) rootView.findViewById(R.id.feed_total_connected_users_text_view);
        moreDetailsBtn = (Button) rootView.findViewById(R.id.feed_user_post_more_details_button);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);

        disableMoreOptionsView();
    }

    // Disabling more options view
    private void disableMoreOptionsView(){
        moreDetailsBtn.setEnabled(false);
        contentDescriptionTextView.setEnabled(false);
        totalConnectedUserTextView.setEnabled(false);
    }

    private void initFragArg(final Bundle bundle){
        if (bundle == null)
            return;

        // No need to update the parse object
        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT);
            if (bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER)) {
                likedByCurrentUser = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.upload_logo_color));
            }

            setViews(currentParseObject);
        }
        // Update this parse object
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
            FetchLatestData.fetchRemainingParseObjectFields(currentParseObject,this);
            FetchLatestData.isILikedThisPost(currentParseObject.getObjectId(),this);
        }
        else
            progressBar.setVisibility(View.GONE);
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

    // Enabling more options view
    private void enableMoreOptionsView(){
        moreDetailsBtn.setEnabled(true);
        contentDescriptionTextView.setEnabled(true);
        totalConnectedUserTextView.setEnabled(true);
    }

    // setting up fragment views with details
    private void setViews(final ParseObject parseObject){
        enableMoreOptionsView();
        final ParseUser postOwner = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (postOwner != null){
            ParseFile parseFile = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (parseFile != null)
                Glide.with(mContext).load(parseFile.getUrl()).into(profilePicImageView);

            usernameTextView.setText(postOwner.getUsername());
        }

        String title = parseObject.getString(ParseConstants.SMALL_TEXT_ONE);
        if (title != null)
            titleTextView.setText(DocNormalizer.htmlToNormal(title));
        String author = parseObject.getString(ParseConstants.SMALL_TEXT_TWO);
        if (author != null)
            authorTextView.setText(DocNormalizer.htmlToNormal(author));

        ParseFile parseFile = parseObject.getParseFile(ParseConstants.LARGE_CONTENT_TEXT);
        if (parseFile != null){
            parseFile.getDataInBackground((data, e) -> {
                progressBar.setVisibility(View.GONE);
                if (e == null){
                    String str = new String(data);
                    mainContentText = DocNormalizer.htmlToNormal(str);
                    mainContentTextView.setText(mainContentText);
                    DocsInfo.totalWords(mainContentText.toString(),this);
                }
            });
        }

        backgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        String description = parseObject.getString(ParseConstants.DESCRIPTION);
        if (description != null  &&  description.trim().length() > 0)
            contentDescriptionTextView.setText(DocNormalizer.htmlToNormal(description));
        else
            contentDescriptionTextView.setVisibility(View.GONE);

        String location = parseObject.getString(ParseConstants.LOCATION);
        if (location != null)
            locationTextView.setText(location);
        else{
            locationImageView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
        }

        postedTimingTextView.setText(getString(R.string.posted_timing,DateConversion.getPostTime(parseObject.getCreatedAt().getTime())));
        totalLikesTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        totalCommentsTextView.setText(getString(R.string.number_to_string_comment,parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        totalConnectedUserTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));
    }

    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null){
            currentParseObject = object;
            setViews(currentParseObject);
        }
        else
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void getDocInfo(int totalWords, String rootWords) {
        contentKeywordsTextView.setText(rootWords);
        contentKeywordsTextView.setSelected(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // observing post more info view model
        postDetailsViewModel.getParseUserData().observe(getViewLifecycleOwner(), parseUser -> {
            if (parseUser != null){
                navigateToUserFragment(parseUser);
                postDetailsViewModel.setParseUserData(parseUser);
            }
        });

        // Share content with apps
        sharePostImageView.setOnClickListener(view1 -> {
            if (currentParseObject == null)
                return;

            SharePost.sharePost(currentParseObject.getString(ParseConstants.SMALL_TEXT_ONE),currentParseObject.getString(ParseConstants.SMALL_TEXT_TWO),mainContentText.toString(),null,mContext);
        });

        postDetailsViewModel.getProcessToDo().observe(getViewLifecycleOwner(), processToDo -> {
            if (processToDo == PostDetailsViewModel.PROCESS_POST_DELETED){
                navController.navigateUp();
                postDetailsViewModel.setProcessData(0);
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (currentParseObject == null)
                    return;
                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,currentParseObject);
                navController.navigate(R.id.action_storyFeedFullFragment_to_reportPostFragment,bundle);
            }
        });

        // Navigate to profile fragment
        usernameTextView.setOnClickListener(view1 -> {
            if (currentParseObject != null){
                final ParseUser parseUser = currentParseObject.getParseUser(ParseConstants.POST_OWNER);
                if (parseUser != null)
                    navigateToUserFragment(parseUser);
            }
        });

        // Navigating back to back fragment
        navigateBackImageView.setOnClickListener(view1 -> navController.navigateUp());


        refreshPostImageView.setOnClickListener(view1 -> openMoreOptionModel());
        moreDetailsBtn.setOnClickListener(view1 -> openMoreOptionModel());
        totalConnectedUserTextView.setOnClickListener(view1 -> openMoreOptionModel());
        contentDescriptionTextView.setOnClickListener(view1 -> openMoreOptionModel());

        // navigating to post comment fragment
        commentPostImageView.setOnClickListener(view1 -> navigateToPostCommentFrag());
        totalCommentsTextView.setOnClickListener(view1 -> navigateToPostCommentFrag());

        // navigate to post like fragment
        totalLikesTextView.setOnClickListener(view1 -> navigateToPostLikeFrag());


        // liking or disliking the post
        likePostImageView.setOnClickListener(view1 -> {
            // Post is already liked now dislike it
            if (likedByCurrentUser){
                likedByCurrentUser = false;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) - 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));

            }
            // Post is disliked now like it
            else{
                likedByCurrentUser = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) + 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));
            }

            likeOrDislikePost();
        });


        // Changing text color to White
        whiteColorImageView.setOnClickListener(view1 -> {
            changeTextToWhite();
            textColorImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
        });

        // Changing text color to grey
        greyColorImageView.setOnClickListener(view1 -> {
            changeTextToGrey();
            textColorImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.touch_background_color));
        });

    }

    private void navigateToUserFragment(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
            navController.navigate(R.id.action_storyFeedFullFragment_to_bottomProfileFragment);
            return;
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_storyFeedFullFragment_to_usersProfileFragment);
    }


    // navigating to post likes fragment
    private void navigateToPostLikeFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES,currentParseObject);
            feedsArgsViewModel.insertPostLikesArgStarting(bundle);
            navController.navigate(R.id.action_storyFeedFullFragment_to_postLikesFragment);
        }
    }


    // navigating to post comment fragment
    private void navigateToPostCommentFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT,currentParseObject);
            feedsArgsViewModel.insertPostCommentStartingArg(bundle);
            navController.navigate(R.id.action_storyFeedFullFragment_to_postCommentsFragment);
        }
    }


    //  like or dislike the post (Call cloud function)
    private void likeOrDislikePost(){
        if (currentParseObject == null)
            return;

        FetchLatestData.likeOrDislikePost(currentParseObject.getObjectId(),this);
    }

    @Override
    public void isTrueFromServer(boolean isTrue, ParseException parseException) {
        if (parseException == null){
            likedByCurrentUser = isTrue;
            if (isTrue)
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
            else
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
        }
        else
            Log.e(Constants.TAG, "Failed to call cloud function (isILikedThisPost) OR (likeOrDislikePost): " + parseException.getMessage());

    }


    // Changing text color to white
    private void changeTextToWhite(){
        SpannableString spannableString = getSpannedString(ContextCompat.getColor(mContext,R.color.white));
        mainContentTextView.setText(spannableString);
    }

    private void changeTextToGrey(){
        SpannableString spannableString = getSpannedString(ContextCompat.getColor(mContext,R.color.touch_background_color));
        mainContentTextView.setText(spannableString);
    }


    private SpannableString getSpannedString(int color){
        final String text = mainContentTextView.getText().toString().trim();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(color),
                0,
                text.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    @Override
    public void onDestroyView() {
        if (currentParseObject != null)
            feedsArgsViewModel.modifyLastParseObj(currentParseObject,likedByCurrentUser);
        super.onDestroyView();
        nullAllViews();
    }

    private void nullAllViews(){
        rootView = null;
        navigateBackImageView = null;
        refreshPostImageView = null;
        backgroundImageView = null;
        profilePicImageView = null;
        whiteColorImageView = null;
        greyColorImageView = null;
        textColorImageView = null;
        likePostImageView = null;
        commentPostImageView = null;
        sharePostImageView = null;
        locationImageView = null;
        contentKeywordsTextView = null;
        postedTimingTextView = null;
        titleTextView = null;
        authorTextView = null;
        mainContentTextView = null;
        usernameTextView = null;
        totalLikesTextView = null;
        totalCommentsTextView = null;
        contentDescriptionTextView = null;
        locationTextView = null;
        totalConnectedUserTextView = null;
        moreDetailsBtn = null;
        progressBar = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastFullFeedArg();
        super.onDestroy();
    }

}