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

import java.util.ArrayList;
import java.util.List;

public class PoemFeedFullFragment extends Fragment implements ParseObjectCallback, DocInfoCallback, BooleanParseCallback {

    private View rootView;

    private ImageView navigateBackImageView, postMoreOptionImageView, profilePicImageView,
            likePostImageView, commentPostImageView, shareImageView, locationImageView,appBarBackgroundImageView,
            mainContentBackgroundImageView;

    private TextView keywordsTextView, positingTimingTextView, usernameTextView,
            titleTextView, authorTextView, mainContentTextView, totalLikesTextView, totalCommentsTextView,contentDescriptionTextView,
            totalConnectedUsersTextView, locationTextView;

    private Button moreDetailsBtn;

    private ProgressBar progressBar;

    private Context mContext;

    private ParseObject currentParseObject;

    private boolean likedByCurrentUser = false;

    private CharSequence mainContentText;

    private String attachedImageUrl = null;

    private FeedsArgsViewModel feedsArgsViewModel;

    private PostDetailsViewModel postDetailsViewModel;

    private NavController navController;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_poem_feed_full, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);

        // init views
        initFragViews();

        // init fragment argument
        initFragArg(feedsArgsViewModel.getLastFullFeedArg());

        return rootView;
    }

    // init fragments views
    private void initFragViews(){
        navigateBackImageView = (ImageView) rootView.findViewById(R.id.navigate_back_poem_or_story_fragment_image_view);
        postMoreOptionImageView = (ImageView) rootView.findViewById(R.id.refresh_poem_or_story_fragment_feed_image_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_image_poem_or_story_feed_fragment_image_view);
        likePostImageView = (ImageView) rootView.findViewById(R.id.feed_like_user_post_image_view);
        commentPostImageView = (ImageView) rootView.findViewById(R.id.feed_comment_user_post_image_view);
        shareImageView = (ImageView) rootView.findViewById(R.id.feed_share_user_post_image_view);
        locationImageView = (ImageView) rootView.findViewById(R.id.feed_user_post_location_image_view);
        appBarBackgroundImageView = (ImageView) rootView.findViewById(R.id.content_background_poem_or_story_image_view);
        mainContentBackgroundImageView = (ImageView) rootView.findViewById(R.id.background_poem_feed_fragment_image_view);
        keywordsTextView = (TextView) rootView.findViewById(R.id.keywords_of_poem_or_story_feed_fragment_text_view);
        positingTimingTextView = (TextView) rootView.findViewById(R.id.post_posting_timing_poem_or_story_feed_fragment_text_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_poem_or_story_feed_fragment_text_view);
        titleTextView = (TextView) rootView.findViewById(R.id.title_poem_or_story_feed_fragment_text_view);
        authorTextView = (TextView) rootView.findViewById(R.id.author_poem_or_story_feed_fragment_text_view);
        mainContentTextView = (TextView) rootView.findViewById(R.id.content_poem_or_story_feed_fragment_text_view);
        totalLikesTextView = (TextView) rootView.findViewById(R.id.feed_total_likes_text_view);
        totalCommentsTextView = (TextView) rootView.findViewById(R.id.feed_total_comments_text_view);
        contentDescriptionTextView = (TextView) rootView.findViewById(R.id.feed_user_post_description_text_view);
        totalConnectedUsersTextView = (TextView) rootView.findViewById(R.id.feed_total_connected_users_text_view);
        locationTextView = (TextView) rootView.findViewById(R.id.feed_post_location_text_view);
        moreDetailsBtn = (Button) rootView.findViewById(R.id.feed_user_post_more_details_button);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);

        disableMoreOptionsView();
    }

    // Disabling more options view
    private void disableMoreOptionsView(){
        postMoreOptionImageView.setEnabled(false);
        contentDescriptionTextView.setEnabled(false);
        moreDetailsBtn.setEnabled(false);
        totalConnectedUsersTextView.setEnabled(false);
    }


    private void initFragArg(final Bundle bundle){
        if (bundle == null)
            return;

        // Do not need to update this parse object
        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT);
            if (bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER)) {
                likedByCurrentUser = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.upload_logo_color));
            }
            setViews(currentParseObject);
        }
        // Refresh this parse object
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_OBJECT)){
            currentParseObject = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
            FetchLatestData.fetchRemainingParseObjectFields(currentParseObject,this);
            FetchLatestData.isILikedThisPost(currentParseObject.getObjectId(),this);
        }
        else
            progressBar.setVisibility(View.GONE);
    }


    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null) {
            currentParseObject = object;
            setViews(currentParseObject);
        }
        else
            progressBar.setVisibility(View.GONE);
    }

    // Enabling more options view
    private void enableMoreOptionsView(){
        postMoreOptionImageView.setEnabled(true);
        contentDescriptionTextView.setEnabled(true);
        moreDetailsBtn.setEnabled(true);
        totalConnectedUsersTextView.setEnabled(true);
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

    private void setViews(final ParseObject parseObject){
        enableMoreOptionsView();
        final ParseUser postOwner = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (postOwner != null){
            ParseFile parseFile = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (parseFile != null)
                Glide.with(mContext).load(parseFile.getUrl()).into(profilePicImageView);

            usernameTextView.setText(postOwner.getUsername());
        }

        mainContentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        String title = parseObject.getString(ParseConstants.SMALL_TEXT_ONE);
        if (title != null)
            titleTextView.setText(DocNormalizer.htmlToNormal(title));

        String author = parseObject.getString(ParseConstants.SMALL_TEXT_TWO);
        if (author != null)
            authorTextView.setText(getString(R.string.author,DocNormalizer.htmlToNormal(author)));

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

        ParseFile backgroundImage = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (backgroundImage != null) {
            attachedImageUrl = backgroundImage.getUrl();
            Glide.with(mContext).load(attachedImageUrl).into(appBarBackgroundImageView);
        }
        else
            appBarBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

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

        positingTimingTextView.setText(getString(R.string.posted_timing,DateConversion.getPostTime(parseObject.getCreatedAt().getTime())));
        totalLikesTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        totalCommentsTextView.setText(getString(R.string.number_to_string_comment,parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        totalConnectedUsersTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));
    }

    @Override
    public void getDocInfo(int totalWords, String rootWords) {
        keywordsTextView.setText(rootWords);
        keywordsTextView.setSelected(true);
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
                postDetailsViewModel.setProcessData(0);
                navController.navigateUp();
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (currentParseObject == null)
                    return;
                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,currentParseObject);
                navController.navigate(R.id.action_poemFeedFullFragment_to_reportPostFragment,bundle);
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

        // Share content with other apps
        shareImageView.setOnClickListener(view1 -> {
            if (currentParseObject == null)
                return;

            List<String> imageUrl = null;
            if (attachedImageUrl != null){
                imageUrl = new ArrayList<>();
                imageUrl.add(attachedImageUrl);
            }

            SharePost.sharePost(currentParseObject.getString(ParseConstants.SMALL_TEXT_ONE),ParseConstants.SMALL_TEXT_TWO,mainContentText.toString(),imageUrl,mContext);

        });


        // Navigating to previous fragment
        navigateBackImageView.setOnClickListener(view1 -> navController.navigateUp());

        // Navigating to bottom sheet fragment
        moreDetailsBtn.setOnClickListener(view1 -> openMoreOptionModel());
        postMoreOptionImageView.setOnClickListener(view1 -> openMoreOptionModel());
        contentDescriptionTextView.setOnClickListener(view1 -> openMoreOptionModel());
        totalConnectedUsersTextView.setOnClickListener(view1 -> openMoreOptionModel());


        // navigating post comment fragment
        commentPostImageView.setOnClickListener(view1 -> navigateToPostCommentFrag());
        totalCommentsTextView.setOnClickListener(view1 -> navigateToPostCommentFrag());

        // navigate to post liks fragment
        totalLikesTextView.setOnClickListener(view1 -> navigateToPostLikesFrag());


        // liking or disliking the post
        likePostImageView.setOnClickListener(view1 -> {
            // Was liked post now disliking it
            if (likedByCurrentUser){
                likedByCurrentUser = false;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString().trim())) - 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));

            }
            // Was disliked now liking it
            else{
                likedByCurrentUser = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString().trim())) + 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));
            }

            likeOrDislikePost();
        });
    }

    // navigate to user or bottom user fragment
    private void navigateToUserFragment(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
            navController.navigate(R.id.action_poemFeedFullFragment_to_bottomProfileFragment);
            return;
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_poemFeedFullFragment_to_usersProfileFragment);
    }


    // navigating to post likes fragment
    private void navigateToPostLikesFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES,currentParseObject);
            feedsArgsViewModel.insertPostLikesArgStarting(bundle);
            navController.navigate(R.id.action_poemFeedFullFragment_to_postLikesFragment);
        }
    }


    // navigate to post comment fragment
    private void navigateToPostCommentFrag(){
        if (currentParseObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT,currentParseObject);
            feedsArgsViewModel.insertPostCommentStartingArg(bundle);
            navController.navigate(R.id.action_poemFeedFullFragment_to_postCommentsFragment);
        }
    }


    // Like or dislike the post
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

    @Override
    public void onDestroyView() {
        if (currentParseObject != null)
            feedsArgsViewModel.modifyLastParseObj(currentParseObject,likedByCurrentUser);
        super.onDestroyView();
        nullAllView();
    }

    private void nullAllView(){
        rootView = null;
        navigateBackImageView = null;
        postMoreOptionImageView = null;
        profilePicImageView = null;
        likePostImageView = null;
        commentPostImageView = null;
        shareImageView = null;
        locationImageView = null;
        appBarBackgroundImageView = null;
        mainContentBackgroundImageView = null;
        keywordsTextView = null;
        positingTimingTextView = null;
        usernameTextView = null;
        titleTextView = null;
        authorTextView = null;
        mainContentTextView = null;
        totalLikesTextView = null;
        totalCommentsTextView = null;
        contentDescriptionTextView = null;
        totalConnectedUsersTextView = null;
        locationTextView = null;
        moreDetailsBtn = null;
        progressBar = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastFullFeedArg();
        super.onDestroy();
    }
}