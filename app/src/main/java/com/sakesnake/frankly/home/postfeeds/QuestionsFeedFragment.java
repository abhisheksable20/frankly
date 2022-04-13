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
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.sakesnake.frankly.parsedatabase.ParseObjectUserListCallback;
import com.sakesnake.frankly.parsedatabase.ParseCommentRelationCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.sakesnake.frankly.parsedatabase.ParseObjectCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public class QuestionsFeedFragment extends Fragment implements ParseCommentRelationCallback,
        ParseObjectUserListCallback, ParseObjectCallback, BooleanParseCallback {

    private View rootView;

    private Toolbar fragmentToolbar;

    private ImageView profilePicImageView, likePostImageView, giveAnswerImageView,sharePostImageView, locationImageView,
                      mainContentBackgroundImageView;

    private TextView usernameTextView, postedTimeTextView,mainContentTextView,answerHeadingTextView,totalLikesTextView,
            totalAnswerTextView,totalConnectedUsers,locationTextView,descriptionTextView;

    private Button moreOptionsBtn;

    private RecyclerView answersRecyclerView;

    private Context mContext;

    private ParseObject currentObject;

    private List<ParseObject> postComments;

    private boolean isLikedByCurrent = false;

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

        rootView = inflater.inflate(R.layout.fragment_questions_feed, container, false);

        // Init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);


        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init fragment views
        initFragViews();

        // init fragment arguments
        initFragArg(feedsArgsViewModel.getLastFullFeedArg());

        return rootView;

    }

    // Init fragment views
    private void initFragViews(){
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.feed_question_fragment_toolbar);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.feed_user_profile_photo_image_view);
        likePostImageView = (ImageView) rootView.findViewById(R.id.feed_like_user_post_image_view);
        giveAnswerImageView = (ImageView) rootView.findViewById(R.id.feed_comment_user_post_image_view);
        sharePostImageView = (ImageView) rootView.findViewById(R.id.feed_share_user_post_image_view);
        locationImageView = (ImageView) rootView.findViewById(R.id.feed_user_post_location_image_view);
        mainContentBackgroundImageView = (ImageView) rootView.findViewById(R.id.question_feed_background_image_view);
        usernameTextView = (TextView) rootView.findViewById(R.id.feed_user_username_text_view);
        postedTimeTextView = (TextView) rootView.findViewById(R.id.feed_post_posting_time_text_view);
        mainContentTextView = (TextView) rootView.findViewById(R.id.question_feed_content_text_view);
        answerHeadingTextView = (TextView) rootView.findViewById(R.id.answers_heading_text_view);
        totalLikesTextView = (TextView) rootView.findViewById(R.id.feed_total_likes_text_view);
        totalAnswerTextView = (TextView) rootView.findViewById(R.id.feed_total_comments_text_view);
        totalConnectedUsers = (TextView) rootView.findViewById(R.id.feed_total_connected_users_text_view);
        locationTextView = (TextView) rootView.findViewById(R.id.feed_post_location_text_view);
        descriptionTextView = (TextView) rootView.findViewById(R.id.feed_user_post_description_text_view);
        moreOptionsBtn = (Button) rootView.findViewById(R.id.feed_user_post_more_details_button);
        answersRecyclerView = (RecyclerView) rootView.findViewById(R.id.answers_for_question_recycler_view);

        giveAnswerImageView.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.question_comment_icon));

        disableMoreOptionsView();
    }

    // Enabling more options view
    private void disableMoreOptionsView(){
        moreOptionsBtn.setEnabled(false);
        descriptionTextView.setEnabled(false);
        totalConnectedUsers.setEnabled(false);
    }


    // init the fragment arguments
    private void initFragArg(final Bundle bundle){
        if (bundle == null)
            return;

        if (bundle.containsKey(Constants.BUNDLE_SAVED_PARSE_OBJECT)){
            currentObject = bundle.getParcelable(Constants.BUNDLE_SAVED_PARSE_OBJECT);
            isLikedByCurrent = bundle.getBoolean(Constants.BUNDLE_IS_LIKED_USER,false);
            if (isLikedByCurrent)
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));

            if (postComments == null)
                fetchAnswersFromNetwork();

            setViews(currentObject);
        }
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_PARSE_OBJECT)){
            currentObject = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
            FetchLatestData.fetchRemainingParseObjectFields(currentObject,this);
            FetchLatestData.isILikedThisPost(currentObject.getObjectId(),this);
            fetchAnswersFromNetwork();
        }
    }

    @Override
    public void isTrueFromServer(boolean isTrue, ParseException parseException) {
        if (parseException == null){
            isLikedByCurrent = isTrue;
            if (isTrue)
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
            else
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
        }
        else
            Log.e(Constants.TAG, "Parse cloud function failed (isLikedBuCurrent) OR (likeDislikePost): "+parseException.getMessage());
    }

    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null){
            currentObject = object;
            setViews(currentObject);
        }
        else
            Log.e(Constants.TAG, "Cannot fetch latest parse object: " + parseException.getMessage());
    }


    // Fetching the answer from network
    private void fetchAnswersFromNetwork(){
        FetchLatestData.fetchCommentRelation(currentObject.getObjectId(),this);
    }

    @Override
    public void getCommentRelationObject(ParseObject objectWithCommentRelation) {
        if (objectWithCommentRelation != null)
            FetchLatestData.fetchPostComments(objectWithCommentRelation,5,0,this);
    }

    @Override
    public void postObjectUserLists(List<ParseObject> postComments,List<ParseUser> parseUsers) {
        this.postComments = postComments;
    }

    // Setting post answers recycler view adapter
    private void setPostAnswersAdapter(final List<ParseObject> parseObjectList){

    }

    // Enabling more options view
    private void enableMoreOptionsView(){
        moreOptionsBtn.setEnabled(true);
        descriptionTextView.setEnabled(true);
        totalConnectedUsers.setEnabled(true);
    }


    // Setting fragment views with data
    private void setViews(final ParseObject parseObject){
        enableMoreOptionsView();
        // Getting post owner
        final ParseUser postOwner = parseObject.getParseUser(ParseConstants.POST_OWNER);
        // Setting the user details
        if (postOwner != null){
            final ParseFile parseFile = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (parseFile != null)
                Glide.with(mContext).load(parseFile.getUrl()).into(profilePicImageView);

            usernameTextView.setText(postOwner.getUsername());
        }

        final String mainContent = parseObject.getString(ParseConstants.SMALL_TEXT_ONE);
        if (mainContent != null)
            mainContentTextView.setText(DocNormalizer.htmlToNormal(mainContent));

        mainContentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        final String postDescription = parseObject.getString(ParseConstants.DESCRIPTION);
        if (postDescription != null &&  postDescription.length() > 0)
            descriptionTextView.setText(DocNormalizer.htmlToNormal(postDescription));
        else
            descriptionTextView.setVisibility(View.GONE);


        final String postLocation = parseObject.getString(ParseConstants.LOCATION);
        if (postLocation != null  &&  postLocation.length() > 0)
            locationTextView.setText(postLocation);
        else {
            locationImageView.setVisibility(View.GONE);
            locationTextView.setVisibility(View.GONE);
        }

        postedTimeTextView.setText(getString(R.string.posted_timing,DateConversion.getPostTime(parseObject.getCreatedAt().getTime())));
        totalLikesTextView.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        totalConnectedUsers.setText(getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));

        final int totalAnswers = parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS);
        if (totalAnswers <= 1)
            totalAnswerTextView.setText(getString(R.string.number_to_string_answers_singular,totalAnswers));
        else
            totalAnswerTextView.setText(getString(R.string.number_to_string_answers_plural,totalAnswers));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // observing post more info view model
        postDetailsViewModel.getParseUserData().observe(getViewLifecycleOwner(), parseUser -> {
            if (parseUser != null){
                navigateUserFragment(parseUser);
                postDetailsViewModel.setParseUserData(null);
            }
        });

        // Share content with apps
        sharePostImageView.setOnClickListener(view1 -> {
            if (currentObject == null)
                return;

            SharePost.sharePost(currentObject.getString(ParseConstants.SMALL_TEXT_ONE),null,null,null,mContext);
        });

        postDetailsViewModel.getProcessToDo().observe(getViewLifecycleOwner(), processToDo -> {
            if (processToDo == PostDetailsViewModel.PROCESS_POST_DELETED){
                navController.navigateUp();
                postDetailsViewModel.setProcessData(0);
            }
            else if (processToDo == PostDetailsViewModel.PROCESS_REPORT_POST){
                postDetailsViewModel.setProcessData(0);
                if (currentObject == null)
                    return;
                final Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT,currentObject);
                navController.navigate(R.id.action_questionsFeedFragment_to_reportPostFragment,bundle);
            }
        });

        // Navigate to profile fragment
        usernameTextView.setOnClickListener(view1 -> {
            if (currentObject != null){
                final ParseUser parseUser = currentObject.getParseUser(ParseConstants.POST_OWNER);
                if (parseUser != null)
                    navigateUserFragment(parseUser);
            }
        });

        // navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        fragmentToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.more_options_toolbar_menu)
                openMoreOptionModel();

            return false;
        });

        // navigating to bottom model sheet fragment
        moreOptionsBtn.setOnClickListener(view1 -> openMoreOptionModel());
        totalConnectedUsers.setOnClickListener(view1 -> openMoreOptionModel());
        descriptionTextView.setOnClickListener(view1 -> openMoreOptionModel());



        // navigate to post comment fragment
        giveAnswerImageView.setOnClickListener(view1 -> navigateToPostCommentFrag());
        totalAnswerTextView.setOnClickListener(view1 -> navigateToPostCommentFrag());

        // navigate to post likes fragment
        totalLikesTextView.setOnClickListener(view1 -> navigateToPostLikesFrag());


        likePostImageView.setOnClickListener(view1 -> {
            // was liked now disliking the post
            if (isLikedByCurrent){
                isLikedByCurrent = false;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) - 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));
            }
            else{
                isLikedByCurrent = true;
                likePostImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                int totalLikes = (Integer.parseInt(totalLikesTextView.getText().toString())) + 1;
                totalLikesTextView.setText(getString(R.string.number_to_string,totalLikes));
            }

            likeOrDislikePost();
        });
    }

    private void navigateUserFragment(final ParseUser parseUser){
        if (parseUser.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
            navController.navigate(R.id.action_questionsFeedFragment_to_bottomProfileFragment);
            return;
        }

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,parseUser);
        feedsArgsViewModel.addUserProfileArgs(bundle);
        navController.navigate(R.id.action_questionsFeedFragment_to_usersProfileFragment);
    }


    // Open bottom model sheet (Post more details)
    private void openMoreOptionModel(){
        if (currentObject == null)
            return;

        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT,currentObject);
        PostMoreInfoFragment postMoreInfoFragment = new PostMoreInfoFragment();
        postMoreInfoFragment.setArguments(bundle);
        postMoreInfoFragment.show(getParentFragmentManager(),PostMoreInfoFragment.FRAGMENT_TAG);
    }


    // navigating to post likes fragment
    private void navigateToPostLikesFrag(){
        if (currentObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_LIKES,currentObject);
            feedsArgsViewModel.insertPostLikesArgStarting(bundle);
            navController.navigate(R.id.action_questionsFeedFragment_to_postLikesFragment);
        }
    }


    // navigate to post comment fragment
    private void navigateToPostCommentFrag(){
        if (currentObject != null){
            final Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_POST_COMMENT,currentObject);
            feedsArgsViewModel.insertPostCommentStartingArg(bundle);
            navController.navigate(R.id.action_questionsFeedFragment_to_postCommentsFragment);
        }
    }

    // liking or dislike the post
    private void likeOrDislikePost(){
        if (currentObject == null)
            return;

        FetchLatestData.likeOrDislikePost(currentObject.getObjectId(),this);
    }

    @Override
    public void onDestroyView() {
        if (currentObject != null)
            feedsArgsViewModel.modifyLastParseObj(currentObject,isLikedByCurrent);
        super.onDestroyView();
        nullAllViews();
    }

    private void nullAllViews(){
        fragmentToolbar = null;
        answersRecyclerView = null;
        rootView = null;
        profilePicImageView = null;
        likePostImageView = null ;
        giveAnswerImageView = null;
        sharePostImageView = null;
        locationImageView = null;
        usernameTextView = null;
        postedTimeTextView = null;
        mainContentTextView = null;
        answerHeadingTextView = null;
        totalLikesTextView = null;
        totalAnswerTextView = null;
        totalConnectedUsers = null;
        locationTextView = null;
        descriptionTextView = null;
        mainContentBackgroundImageView = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastFullFeedArg();
        super.onDestroy();
    }
}