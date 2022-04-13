package com.sakesnake.frankly.home.postfeeds.postComments;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseObjectCallback;
import com.sakesnake.frankly.parsedatabase.ParseObjectUserListCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PostCommentsFragment extends Fragment implements ParseObjectCallback,ParseObjectUserListCallback, PostFeedsCallback {

    private View rootView;

    private Toolbar fragmentToolbar;

    private RecyclerView postCommentRecyclerView;

    private EditText getCommentEditText;

    private ImageView shareCommentImageView;

    private ProgressBar progressBar;

    private PostCommentsAdapter postCommentsAdapter;

    private LinearLayoutManager linearLayoutManager;

    private Context mContext;

    private NavController navController;

    private FeedsArgsViewModel feedsArgsViewModel;

    private SessionErrorViewModel sessionErrorViewModel;

    private boolean dataEnded = false;

    private boolean loadData = false;

    private static final int REFRESH_THRESHOLD = 5;

    private static final int FETCH_LIMIT = 10;

    private int recyclerCurrentPos = 0;

    private ParseObject currentPostObject;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_post_comments, container, false);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);


        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        // init views
        initView();

        // init frag arg
        initFragmentArgs(feedsArgsViewModel.getPostCommentLastArg());


        return rootView;
    }

    // init all fragment views
    private void initView(){
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.comment_fragment_toolbar);
        postCommentRecyclerView = (RecyclerView) rootView.findViewById(R.id.post_comments_recycler_view);
        getCommentEditText = (EditText) rootView.findViewById(R.id.get_comment_edit_text);
        shareCommentImageView = (ImageView) rootView.findViewById(R.id.post_comment_image_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);
    }

    // init fragment arguments
    private void initFragmentArgs(final Bundle bundle){
        if (bundle == null)
            return;

        if (bundle.containsKey(Constants.BUNDLE_SAVED_POST_COMMENT)){
            List<ParseObject> savedParseObject;
            currentPostObject = bundle.getParcelable(Constants.BUNDLE_SAVED_POST_COMMENT);
            if (bundle.containsKey(Constants.BUNDLE_ADAPTER_LIST)) {
                savedParseObject = bundle.getParcelableArrayList(Constants.BUNDLE_ADAPTER_LIST);
                setViews(savedParseObject);
                return;
            }

            FetchLatestData.fetchRemainingParseObjectFields(currentPostObject,this);
        }
        else if (bundle.containsKey(Constants.BUNDLE_FRESH_POST_COMMENT)){
            // With this object we can fetch object comment relation
            currentPostObject = bundle.getParcelable(Constants.BUNDLE_FRESH_POST_COMMENT);
            FetchLatestData.fetchRemainingParseObjectFields(currentPostObject,this);
        }

        if (currentPostObject != null){
            if (currentPostObject.getInt(ParseConstants.CONTENT_TYPE) == Constants.UPLOAD_GIVE_QUESTION)
                setViewForAnswer();
        }
    }


    private void setViewForAnswer(){
        fragmentToolbar.setTitle(getString(R.string.answers_heading));
        getCommentEditText.setHint(getString(R.string.enter_answer_hint));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Uploading the post comment
        shareCommentImageView.setOnClickListener(view1 -> {
            final String postComment = getPostComment();
            if (postComment != null) {
                if (IsInternetAvailable.isInternetAvailable(mContext))
                    uploadCommentViaCloud(postComment);
                else
                    showSnackBar(getString(R.string.no_internet_warning),ContextCompat.getColor(mContext,R.color.red_help_people));
            }
        });


        // Observing recycler view scroll
        postCommentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (linearLayoutManager == null || dataEnded || currentPostObject == null)
                    return;

                if (dy > 0){
                    final int totalCount = linearLayoutManager.getItemCount();
                    final int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                    if (loadData  &&  (totalCount <= (lastVisiblePosition + REFRESH_THRESHOLD))){
                        loadData = false;
                        FetchLatestData.fetchPostComments(currentPostObject,FETCH_LIMIT,
                                postCommentsAdapter.getItemCount(),
                                PostCommentsFragment.this);
                    }
                }
            }
        });
    }


    private String getPostComment(){
        if (getCommentEditText.getText().toString().trim().length() > 0)
            return DocNormalizer.spannedToHtml(getCommentEditText.getText());

        return null;
    }


    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        currentPostObject = object;
        FetchLatestData.fetchPostComments(currentPostObject,10,0,this);
    }


    // Uploading post comment using cloud code function
    private void uploadCommentViaCloud(final String comment){
        if (currentPostObject != null){
            shareCommentImageView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.light_upload_color));
            shareCommentImageView.setEnabled(false);
            showSnackBar(getString(R.string.posting_comment_message),ContextCompat.getColor(mContext,R.color.upload_logo_color));
            final HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put(HashMapKeys.POST_OBJECT_ID_KEY,currentPostObject.getObjectId());
            hashMap.put(HashMapKeys.POST_COMMENT_KEY,comment);
            ParseCloud.callFunctionInBackground(ParseConstants.COMMENT_THE_POST,hashMap,(object,e)->{
                shareCommentImageView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.upload_logo_color));
                shareCommentImageView.setEnabled(true);
                if (e == null)
                    showSnackBar(getString(R.string.comment_posted_successfully),ContextCompat.getColor(mContext,R.color.upload_logo_color));
                else
                    showSnackBar(getString(R.string.comment_post_failed),ContextCompat.getColor(mContext,R.color.red_help_people));
            });
        }
    }

    // showing snack bar to update the user knowledge
    private void showSnackBar(final String message,final int color){
        if (shareCommentImageView != null)
            Snackbar.make(mContext,shareCommentImageView,message,1500)
                    .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                    .setBackgroundTint(color)
                    .show();
    }


    @Override
    public void postObjectUserLists(List<ParseObject> postComments,List<ParseUser> parseUsers) {
        progressBar.setVisibility(View.GONE);
        if (postComments == null) {
            dataEnded = true;
            return;
        }

        if (postComments.size() > 0)
            setViews(postComments);

        else {
            dataEnded = true;
            if (postCommentsAdapter != null)
                postCommentsAdapter.hideProgressBar();
        }

        loadData = true;
    }


    private void setViews(final List<ParseObject> parseObjectList){
        progressBar.setVisibility(View.GONE);
        if (postCommentsAdapter == null){
            postCommentsAdapter = new PostCommentsAdapter(mContext,parseObjectList,this);
            linearLayoutManager = new LinearLayoutManager(mContext);
            postCommentRecyclerView.setAdapter(postCommentsAdapter);
            postCommentRecyclerView.setLayoutManager(linearLayoutManager);
            postCommentRecyclerView.scrollToPosition(recyclerCurrentPos);
            return;
        }

        postCommentsAdapter.appendData(parseObjectList);
    }

    @Override
    public void selectedPost(int currentPos,ParseObject object, ParseUser user) {
        recyclerCurrentPos = currentPos;
        if (user != null){
            if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())){
                navController.navigate(R.id.action_postCommentsFragment_to_bottomProfileFragment);
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.BUNDLE_FRESH_PARSE_USER,user);
            feedsArgsViewModel.addUserProfileArgs(bundle);
            navController.navigate(R.id.action_postCommentsFragment_to_usersProfileFragment);
        }
    }

    private ArrayList<ParseObject> getCommentsList(){
        ArrayList<ParseObject> parseObjects = null;
        if (postCommentsAdapter != null) {
            loadData = true;
            parseObjects = new ArrayList<>(postCommentsAdapter.getPostCommentsLists());
        }

        return parseObjects;
    }


    @Override
    public void onDestroyView() {
        if (currentPostObject != null){
            ArrayList<ParseObject> parseObjects = getCommentsList();
            feedsArgsViewModel.modifyPostCommentArg(currentPostObject,parseObjects);
        }
        super.onDestroyView();
        nullAllViews();
        postCommentsAdapter = null;
        linearLayoutManager = null;
    }

    private void nullAllViews(){
        rootView = null;
        fragmentToolbar = null;
        postCommentRecyclerView = null;
        getCommentEditText = null;
        shareCommentImageView = null;
        progressBar = null;
    }

    @Override
    public void onDestroy() {
        feedsArgsViewModel.deleteLastPostCommentArg();
        super.onDestroy();
    }
}