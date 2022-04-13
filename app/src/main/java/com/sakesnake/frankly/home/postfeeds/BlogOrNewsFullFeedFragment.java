package com.sakesnake.frankly.home.postfeeds;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.TruncateDescription;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class BlogOrNewsFullFeedFragment extends Fragment implements ParseObjectCallback, DocInfoCallback, BooleanParseCallback {

    private View rootView;

    // Views instances
    private ImageView navigateBackImageView, moreOptionsImageView, profilePicImageView,
            textToWhiteImageView, textToGreyImageView, currentTextColorImageView, likePostImageView,
            commentPostImageView, sharePostImageView, locationImageView, backgroundImageView;

    private TextView totalWordsTextView, keywordsTextView, postingTimeTextView, usernameTextView,
            contentTitleTextView, mainContentTextView, totalLikesTextView,
            totalCommentsTextView, contentDescriptionTextView, locationTextView, totalConnectedUserTextView;

    private Button morePostDetailsBtn;

    private ProgressBar progressBar;

    private RecyclerView attachedImagesRecyclerView;

    private Context mContext;

    private PostDetailsViewModel postDetailsViewModel;

    private FeedsArgsViewModel feedsArgsViewModel;

    private NavController navController;

    private ParseObject currentParseObj;

    private AttachedImagesAdapter attachedImagesAdapter;

    private List<String> serverAttachedImages = null;

    private CharSequence mainContentText;

    private boolean isLikedByCurrent = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_blog_or_news_full_feed, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);

        // init views (Do view work after this method)
        initView();

        // Getting fragment arguments
        getArgs(feedsArgsViewModel.getLastFullFeedArg());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observing post more info view model
        postDetailsViewModel.getParseUserData().observe(getViewLifecycleOwner(), parseUser -> {
            if (parseUser != null) {
                navigateToUserProfileFrag(parseUser);
                postDetailsViewModel.setParseUserData(null);
            }
        });

        // Navigate to profile fragment
        usernameTextView.setOnClickListener(view1 -> {
            if (currentParseObj != null){
                final ParseUser parseUser = currentParseObj.getParseUser(ParseConstants.POST_OWNER);
                if (parseUser != null)
                    navigateToUserProfileFrag(parseUser);
            }
        });

        postDetailsViewModel.getProcessToDo().observe(getViewLifecycleOwner(), processToDo -> {
            if (processToDo == PostDetailsViewModel.PROCESS_POST_DELETED) {
                postDetailsViewModel.setProcessData(0);
                navController.navigateUp();
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (currentParseObj == null)
                    return;
                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,currentParseObj);
                navController.navigate(R.id.action_blogOrNewsFullFeedFragment_to_reportPostFragment,bundle);
            }
        });

        // Share post across the other apps
        sharePostImageView.setOnClickListener(view1 -> {
            if (currentParseObj == null)
                return;

            String title = DocNormalizer.htmlToNormal(currentParseObj.getString(ParseConstants.SMALL_TEXT_ONE)).toString();
            SharePost.sharePost(title,null,mainContentText.toString(),serverAttachedImages,mContext);
        });


        // Navigating to back fragment
        navigateBackImageView.setOnClickListener(view1 -> navController.navigateUp());


        // navigating to post comment fragment
        commentPostImageView.setOnClickListener(view1 -> navigateToPostComment());
        totalCommentsTextView.setOnClickListener(view1 -> navigateToPostComment());

        // navigating to like fragment
        totalLikesTextView.setOnClickListener(view1 -> navigateToPostLikes());

        // More details model bottom sheet fragment
        morePostDetailsBtn.setOnClickListener(view1 -> openMoreOptionModel());
        moreOptionsImageView.setOnClickListener(view1 -> openMoreOptionModel());
        contentDescriptionTextView.setOnClickListener(view1 -> openMoreOptionModel());
        totalConnectedUserTextView.setOnClickListener(view1 -> openMoreOptionModel());


        // liking this post via cloud code function
        likePostImageView.setOnClickListener(view1 -> {
            if (currentParseObj == null)
                return;

            // Post is liked now dislike it
            if (isLikedByCurrent) {
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                isLikedByCurrent = false;
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) - 1;
                totalLikesTextView.setText(getString(R.string.number_to_string, totalLikes));
            }
            // Post is disliked now like it
            else {
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.upload_logo_color));
                isLikedByCurrent = true;
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) + 1;
                totalLikesTextView.setText(getString(R.string.number_to_string, totalLikes));
            }

            likeDislikePost();
        });

        // Changing text color to white
        textToWhiteImageView.setOnClickListener(view1 -> {
            changeTextColorToWhite();
            currentTextColorImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
        });

        // Changing text color to grey
        textToGreyImageView.setOnClickListener(view1 -> {
            changeTextColorToGrey();
            currentTextColorImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.touch_background_color));
        });

    }

    // Navigating to user profile fragment
    private void navigateToUserProfileFrag(final ParseUser parseUser) {
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            navController.navigate(R.id.action_blogOrNewsFullFeedFragment_to_bottomProfileFragment);
            return;
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER, parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_blogOrNewsFullFeedFragment_to_usersProfileFragment);
    }


    // Open bottom model sheet (Post more details)
    private void openMoreOptionModel() {
        if (currentParseObj == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT, currentParseObj);
        PostMoreInfoFragment postMoreInfoFragment = new PostMoreInfoFragment();
        postMoreInfoFragment.setArguments(bundle);
        postMoreInfoFragment.show(getParentFragmentManager(), PostMoreInfoFragment.FRAGMENT_TAG);
    }


    // navigating to post likes fragment
    private void navigateToPostLikes() {
        if (currentParseObj == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES, currentParseObj);
        feedsArgsViewModel.insertPostLikesArgStarting(bundle);
        navController.navigate(R.id.action_blogOrNewsFullFeedFragment_to_postLikesFragment);
    }


    // navigating to post comments fragment
    private void navigateToPostComment() {
        if (currentParseObj == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT, currentParseObj);
        feedsArgsViewModel.insertPostCommentStartingArg(bundle);
        navController.navigate(R.id.action_blogOrNewsFullFeedFragment_to_postCommentsFragment);
    }


    // like this post
    private void likeDislikePost() {
        if (currentParseObj == null)
            return;

        FetchLatestData.likeOrDislikePost(currentParseObj.getObjectId(), this);
    }


    // Method to change text color to white
    private void changeTextColorToWhite() {
        SpannableString spannableString = getTextSpanWithColor(ContextCompat.getColor(mContext, R.color.white));
        mainContentTextView.setText(spannableString);
    }

    // Method to change text color to grey
    private void changeTextColorToGrey() {
        SpannableString spannableString = getTextSpanWithColor(ContextCompat.getColor(mContext, R.color.touch_background_color));
        mainContentTextView.setText(spannableString);
    }

    private SpannableString getTextSpanWithColor(final int color) {
        final String text = mainContentTextView.getText().toString();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(color),
                0,
                text.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }


    // init all views
    private void initView() {
        navigateBackImageView = (ImageView) rootView.findViewById(R.id.navigate_back_blog_or_news_fragment_image_view);
        moreOptionsImageView = (ImageView) rootView.findViewById(R.id.refresh_blog_or_news_fragment_feed_image_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_image_blog_or_news_feed_fragment_image_view);
        textToWhiteImageView = (ImageView) rootView.findViewById(R.id.text_color_white_image_view);
        textToGreyImageView = (ImageView) rootView.findViewById(R.id.text_color_grey_image_view);
        currentTextColorImageView = (ImageView) rootView.findViewById(R.id.text_color_blog_or_news_feed_fragment_image_view);
        likePostImageView = (ImageView) rootView.findViewById(R.id.feed_like_user_post_image_view);
        commentPostImageView = (ImageView) rootView.findViewById(R.id.feed_comment_user_post_image_view);
        sharePostImageView = (ImageView) rootView.findViewById(R.id.feed_share_user_post_image_view);
        locationImageView = (ImageView) rootView.findViewById(R.id.feed_user_post_location_image_view);
        backgroundImageView = (ImageView) rootView.findViewById(R.id.background_image_blog_or_news_feed_fragment_image_view);
        totalWordsTextView = (TextView) rootView.findViewById(R.id.total_words_blog_or_news_feed_fragment_text_view);
        keywordsTextView = (TextView) rootView.findViewById(R.id.keywords_of_blog_or_news_feed_fragment_text_view);
        postingTimeTextView = (TextView) rootView.findViewById(R.id.post_posting_timing_blog_or_news_feed_fragment_text_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_blog_or_news_feed_fragment_text_view);
        contentTitleTextView = (TextView) rootView.findViewById(R.id.content_title_blog_or_news_feed_fragment_text_view);
        mainContentTextView = (TextView) rootView.findViewById(R.id.main_content_blog_or_news_feed_fragment_text_view);
        totalLikesTextView = (TextView) rootView.findViewById(R.id.feed_total_likes_text_view);
        totalCommentsTextView = (TextView) rootView.findViewById(R.id.feed_total_comments_text_view);
        contentDescriptionTextView = (TextView) rootView.findViewById(R.id.feed_user_post_description_text_view);
        locationTextView = (TextView) rootView.findViewById(R.id.feed_post_location_text_view);
        totalConnectedUserTextView = (TextView) rootView.findViewById(R.id.feed_total_connected_users_text_view);
        morePostDetailsBtn = (Button) rootView.findViewById(R.id.feed_user_post_more_details_button);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);
        attachedImagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.attached_images_blog_or_news_feed_fragment_recycler_view);

        disableMoreOptionsView();
    }

    // Disabling more options view
    private void disableMoreOptionsView() {
        moreOptionsImageView.setEnabled(false);
        contentDescriptionTextView.setEnabled(false);
        morePostDetailsBtn.setEnabled(false);
        totalConnectedUserTextView.setEnabled(false);
    }

    private void getArgs(final Bundle bundle) {
        if (bundle == null)
            return;

        // Do not need to refresh the object
        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_OBJECT)) {
            currentParseObj = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT);
            if (bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER)) {
                isLikedByCurrent = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.upload_logo_color));
            } else
                isLikedByCurrent = false;

            setView(currentParseObj);
        }

        // Need to refresh the object
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_OBJECT)) {
            currentParseObj = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
            FetchLatestData.fetchRemainingParseObjectFields(currentParseObj, this);
            FetchLatestData.isILikedThisPost(currentParseObj.getObjectId(), this);

        } else
            progressBar.setVisibility(View.GONE);
    }

    // Callback of latest parse object from network
    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null) {
            currentParseObj = object;
            setView(object);
        } else if (parseException.getCode() == ParseException.INVALID_SESSION_TOKEN)
            progressBar.setVisibility(View.GONE);

    }

    @Override
    public void isTrueFromServer(boolean isTrue, ParseException parseException) {
        if (parseException == null) {
            isLikedByCurrent = isTrue;
            if (isTrue)
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.upload_logo_color));
            else
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
        } else
            Log.e(Constants.TAG, "Error from cloud code (isILikedThisPost) OR (likeOrDislikePost): " + parseException.getMessage());
    }

    // Enabling more options views because fresh object has been fetched
    private void enableMoreOptionsView() {
        moreOptionsImageView.setEnabled(true);
        contentDescriptionTextView.setEnabled(true);
        morePostDetailsBtn.setEnabled(true);
        totalConnectedUserTextView.setEnabled(true);
    }

    // Setting up fragment view data here
    private void setView(final ParseObject parseObject) {
        enableMoreOptionsView();
        final ParseUser parseUser = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (parseUser != null) {
            usernameTextView.setText(parseUser.getUsername());
            final ParseFile parseFile = parseUser.getParseFile(ParseConstants.PROFILE_PIC);
            if (parseFile != null)
                Glide.with(mContext).load(parseFile.getUrl()).into(profilePicImageView);
        }

        contentTitleTextView.setText(DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE)));
        final ParseFile parseFile = parseObject.getParseFile(ParseConstants.LARGE_CONTENT_TEXT);
        if (parseFile != null) {
            parseFile.getDataInBackground((data, e) -> {
                if (e == null) {
                    progressBar.setVisibility(View.GONE);
                    String mainContent = new String(data);
                    mainContentText = DocNormalizer.htmlToNormal(mainContent);
                    DocsInfo.totalWords(mainContentText.toString(), this);
                    mainContentTextView.setText(mainContentText);
                }
            });
        }

        String postDescription = parseObject.getString(ParseConstants.DESCRIPTION);
        if (postDescription != null && postDescription.trim().length() > 0)
            contentDescriptionTextView.setText(TruncateDescription.getTruncatedDescription(postDescription, R.color.white));
        else
            contentDescriptionTextView.setVisibility(View.GONE);

        String location = parseObject.getString(ParseConstants.LOCATION);
        if (location != null && location.length() > 0)
            locationTextView.setText(location);
        else {
            locationImageView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
        }

        postingTimeTextView.setText(getString(R.string.posted_timing, DateConversion.getPostTime(parseObject.getCreatedAt().getTime())));
        totalLikesTextView.setText(getString(R.string.number_to_string, parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        totalCommentsTextView.setText(getString(R.string.number_to_string_comment, parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        totalConnectedUserTextView.setText(getString(R.string.number_to_string, parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));

        JSONArray jsonArray = parseObject.getJSONArray(ParseConstants.MULTI_IMAGE);
        if (jsonArray != null && jsonArray.length() > 0) {
            try {
                    if (serverAttachedImages == null)
                        serverAttachedImages = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        serverAttachedImages.add(jsonArray.getJSONObject(i).getString("url"));
                    }

                    if (serverAttachedImages.size() > 0)
                        Glide.with(mContext).load(serverAttachedImages.get(0))
                                .into(backgroundImageView);

                    if (serverAttachedImages.size() > 1)
                        setAttachedImagesAdapter(serverAttachedImages);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else
            backgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

    }

    // Setting attached image recycler view adapter
    private void setAttachedImagesAdapter(final List<String> imagesUrl) {
        if (attachedImagesAdapter == null)
            attachedImagesAdapter = new AttachedImagesAdapter(mContext, null, imagesUrl);
        attachedImagesRecyclerView.setVisibility(View.VISIBLE);
        attachedImagesRecyclerView.setAdapter(attachedImagesAdapter);
        attachedImagesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    public void getDocInfo(int totalWords, String rootWords) {
        keywordsTextView.setText(rootWords);
        keywordsTextView.setSelected(true);

        String string = "Total words \u2022 " + totalWords;

        SpannableString spannableString = new SpannableString(string);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, string.indexOf("\u2022"), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        totalWordsTextView.setText(spannableString);
    }

    @Override
    public void onDestroyView() {
        if (currentParseObj != null)
            feedsArgsViewModel.modifyLastParseObj(currentParseObj, isLikedByCurrent);

        super.onDestroyView();
        nullAllView();
    }

    private void nullAllView() {
        rootView = null;
        navigateBackImageView = null;
        moreOptionsImageView = null;
        profilePicImageView = null;
        textToWhiteImageView = null;
        textToGreyImageView = null;
        currentTextColorImageView = null;
        likePostImageView = null;
        commentPostImageView = null;
        sharePostImageView = null;
        locationImageView = null;
        backgroundImageView = null;
        totalWordsTextView = null;
        keywordsTextView = null;
        postingTimeTextView = null;
        usernameTextView = null;
        contentTitleTextView = null;
        mainContentTextView = null;
        totalLikesTextView = null;
        totalCommentsTextView = null;
        contentDescriptionTextView = null;
        locationTextView = null;
        totalConnectedUserTextView = null;
        attachedImagesRecyclerView = null;
        morePostDetailsBtn = null;
        progressBar = null;
        attachedImagesAdapter = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastFullFeedArg();
        super.onDestroy();
    }

}