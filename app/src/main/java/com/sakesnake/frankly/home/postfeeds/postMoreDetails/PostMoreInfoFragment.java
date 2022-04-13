package com.sakesnake.frankly.home.postfeeds.postMoreDetails;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.FetchLatestData;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.sakesnake.frankly.parsedatabase.ParseObjectCallback;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// This fragment will invoke two network request
// This fragment may load slow because it have to do network request work
//Optimize it later
public class PostMoreInfoFragment extends BottomSheetDialogFragment implements ParseObjectCallback, PostFeedsCallback {

    private View rootView;

    private TextView usernameTextView, visitProfileTextView,descriptionHeadingTextView,
            contentDescriptionTextView,connectedUsersHeadingTextView,deletePostHeadingTextView,cancelDeleteTextView,
            deletePostTextView,reportPostTextView;

    private CardView deletePostCardView;

    private ImageView profilePicImageView;

    private ConstraintLayout deletePostConstraintLayout;

    private RecyclerView connectedUsersRecyclerView;

    private ProgressBar fragmentProgressBar;

    private Animation animation;

    private Context mContext;

    private boolean deleteTriggered = false;

    private ParseObject currentParseObject;

    private PostDetailsViewModel postDetailsViewModel;

    public static final String FRAGMENT_TAG = "com.example.sakesnake.PostMoreInfoFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_post_more_info, container, false);

        // init view models
        postDetailsViewModel = new ViewModelProvider(requireActivity()).get(PostDetailsViewModel.class);


        // init fragment views
        initFragViews();

        // init fragment argument
        initFragArguments(getArguments());

        return rootView;
    }

    // initializing fragment views
    private void initFragViews(){
        usernameTextView = (TextView) rootView.findViewById(R.id.profile_username_text_view);
        visitProfileTextView = (TextView) rootView.findViewById(R.id.visit_profile_text_view);
        descriptionHeadingTextView = (TextView) rootView.findViewById(R.id.description_heading_text_view);
        contentDescriptionTextView = (TextView) rootView.findViewById(R.id.content_description_text_view);
        connectedUsersHeadingTextView = (TextView) rootView.findViewById(R.id.connected_users_heading_text_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.profile_pic_image_view);
        connectedUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.connected_users_recycler_view);
        fragmentProgressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);
        deletePostHeadingTextView = (TextView) rootView.findViewById(R.id.delete_my_post_text_view);
        cancelDeleteTextView = (TextView) rootView.findViewById(R.id.cancel_post_delete_text_view);
        deletePostTextView = (TextView) rootView.findViewById(R.id.delete_post_text_view);
        deletePostCardView = (CardView) rootView.findViewById(R.id.delete_post_forever_card_view);
        deletePostConstraintLayout = (ConstraintLayout) rootView.findViewById(R.id.delete_post_constraint_layout);
        reportPostTextView = (TextView) rootView.findViewById(R.id.report_post_text_views);
    }

    // initializing fragment arguments
    private void initFragArguments(final Bundle bundle){
        if (bundle == null) {
            Log.e(Constants.TAG, "PostMoreInfoFragment bundle argument was empty.");
            dismiss();
            return;
        }

        currentParseObject = bundle.getParcelable(Constants.BUNDLE_FRESH_PARSE_OBJECT);
        if (currentParseObject != null) {
            if (bundle.getBoolean(Constants.BUNDLE_REFRESH_OBJECT))
                fetchParseObjectFromNet(currentParseObject);
            else {
                setViews(currentParseObject);
                getConnectedUsers(currentParseObject);
            }
        }
    }

    // Fetching latest data from network
    private void fetchParseObjectFromNet(final ParseObject parseObject){
        FetchLatestData.fetchRemainingParseObjectFields(parseObject,this);
    }


    @Override
    public void parseObjectCallback(ParseObject object, ParseException parseException) {
        if (parseException == null){
            setViews(object);
            getConnectedUsers(object);
        }
    }

    // Getting connected users from network
    private void getConnectedUsers(final ParseObject parseObject){
        List<String> connectedUsers = parseObject.getList(ParseConstants.CONNECTED_USERS_LIST);
        if (connectedUsers == null  ||  connectedUsers.size() == 0) {
            fragmentProgressBar.setVisibility(View.GONE);
            return;
        }

        List<String> keys = new ArrayList<>();
        keys.add(ParseConstants.PROFILE_USERNAME);
        keys.add(ParseConstants.PROFILE_PIC);
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.selectKeys(keys);
        parseQuery.whereContainedIn(ParseConstants.PROFILE_USERNAME,connectedUsers);

        parseQuery.findInBackground((objects, e) -> {
            fragmentProgressBar.setVisibility(View.GONE);
            if (e == null){
                if (objects.size() > 0)
                    setConnectedUsersList(objects);
            }
            else
                Log.e(Constants.TAG, "Error while retrieving post connected users: Error ------> " + e.getMessage());
        });
    }

    // Setting fragment views
    private void setViews(final ParseObject parseObject){
        ParseUser postOwner = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (postOwner != null){
            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl()).into(profilePicImageView);

            usernameTextView.setText(postOwner.getUsername());

            if (postOwner.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                deletePostHeadingTextView.setEnabled(true);
                deletePostHeadingTextView.setVisibility(View.VISIBLE);
            }
        }



        final String contentDescription = parseObject.getString(ParseConstants.DESCRIPTION);

        if (contentDescription == null || contentDescription.length() <= 0)
            return;

        descriptionHeadingTextView.setVisibility(View.VISIBLE);
        contentDescriptionTextView.setVisibility(View.VISIBLE);

        contentDescriptionTextView.setText(DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.DESCRIPTION)));
    }

    // Setting connected users list
    private void setConnectedUsersList(final List<ParseUser> parseUserList){
        connectedUsersHeadingTextView.setVisibility(View.VISIBLE);
        connectedUsersRecyclerView.setVisibility(View.VISIBLE);

        PostConnectedUserAdapter postConnectedUserAdapter = new PostConnectedUserAdapter(mContext,parseUserList,this);
        connectedUsersRecyclerView.setAdapter(postConnectedUserAdapter);
        connectedUsersRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2, LinearLayoutManager.VERTICAL,false));
    }

    @Override
    public void selectedPost(int position,ParseObject object, ParseUser user) {
        // navigate to user fragment
        if (user != null) {
            postDetailsViewModel.setParseUserData(user);
            dismiss();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigate to profile fragment
        visitProfileTextView.setOnClickListener(view1 -> {
            if (currentParseObject != null){
                ParseUser user = currentParseObject.getParseUser(ParseConstants.POST_OWNER);
                if (user != null) {
                    postDetailsViewModel.setParseUserData(user);
                    dismiss();
                }
            }
        });

        // Report post to frankly team
        reportPostTextView.setOnClickListener(view1 -> {
            postDetailsViewModel.setProcessData(PostDetailsViewModel.PROCESS_REPORT_POST);
            dismiss();
        });

        // Deleting the post
        // UnHiding the delete post constraint layout
        deletePostHeadingTextView.setOnClickListener(view1 -> {
            if (!deleteTriggered) {
                deletePostConstraintLayout.setVisibility(View.VISIBLE);
                deleteTriggered = true;
                animateDeleteView(1);
            }
        });

        // Cancelling the delete process (Not actual process)
        // Hiding the delete post constraint layout
        cancelDeleteTextView.setOnClickListener(view1 -> {
            deleteTriggered = false;
            animateDeleteView(0);
            deletePostConstraintLayout.setVisibility(View.GONE);
        });

        deletePostCardView.setOnClickListener(view1 -> {
            if (deleteTriggered){
                deleteTriggered = false;
                if (currentParseObject != null)
                    deleteThePost(currentParseObject);
            }
        });

    }

    private void enableDeleteViews(final boolean enable){
        deletePostCardView.setEnabled(enable);
        cancelDeleteTextView.setEnabled(enable);
    }


    // Animating delete constraint layout to SlideUp and SlideDown
    // 0 - Slide up (Hide)
    // 1 - Slide down (UnHide)
    private void animateDeleteView(final int triggerOption){
        if (triggerOption == 0)
            animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_up);
        else
            animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_down);

        deletePostConstraintLayout.startAnimation(animation);
    }

    // User has approved to delete his post now proceed to delete operation
    private void deleteThePost(final ParseObject parseObject){
        enableDeleteViews(false);
        changeDeleteBtnStyle(ContextCompat.getColor(mContext,R.color.light_red_color),"Deleting...");
        parseObject.deleteInBackground(e -> {
            enableDeleteViews(true);
            changeDeleteBtnStyle(ContextCompat.getColor(mContext,R.color.red_help_people),getString(R.string.delete_post));
            if (e == null) {
                Toast.makeText(mContext, "Successfully deleted your post. Refresh the page.", Toast.LENGTH_SHORT).show();
                postDetailsViewModel.setProcessData(PostDetailsViewModel.PROCESS_POST_DELETED);
                dismiss();
            }
            else
                Toast.makeText(mContext, "Failed to delete your post.", Toast.LENGTH_SHORT).show();
        });
    }

    // Changing the delete card style
    private void changeDeleteBtnStyle(final int color, final String btnText){
        deletePostCardView.setCardBackgroundColor(color);
        deletePostTextView.setText(btnText);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        nullAllViews();
    }

    // nulling all the fragment views
    private void nullAllViews(){
        rootView = null;
        usernameTextView = null;
        visitProfileTextView = null;
        descriptionHeadingTextView = null;
        contentDescriptionTextView = null;
        connectedUsersHeadingTextView = null;
        profilePicImageView = null;
        connectedUsersRecyclerView = null;
        fragmentProgressBar = null;
        deletePostHeadingTextView = null;
        cancelDeleteTextView = null;
        deletePostConstraintLayout = null;
        deletePostTextView = null;
        deletePostCardView = null;
        reportPostTextView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}