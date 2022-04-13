package com.sakesnake.frankly.home.postfeeds.reportPost;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ReportPostFragment extends Fragment implements SelectedAdapterPosition{

    private Context mContext;

    private View rootView;

    private RecyclerView reportTypesRecyclerView;

    private EditText postReportEditText;

    private TextView postOwner, postID, postContentType;

    private Button reportPostBtn;

    private Toolbar fragmentToolbar;

    private NavController navController;

    private ParseObject parseObject;

    final List<String> reportTypes = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_report_post, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init views
        initViews();

        // init fragment args
        initFragArg(getArguments());

        // set views
        setViews();

        return rootView;
    }

    // init fragment views
    private void initViews(){
        reportTypesRecyclerView = (RecyclerView) rootView.findViewById(R.id.report_types_recycler_view);
        postReportEditText = (EditText) rootView.findViewById(R.id.post_report_edit_text);
        postOwner = (TextView) rootView.findViewById(R.id.post_owner_text_view);
        postID = (TextView) rootView.findViewById(R.id.post_id_text_view);
        postContentType = (TextView) rootView.findViewById(R.id.content_type_text_view);
        reportPostBtn = (Button) rootView.findViewById(R.id.report_post_btn);
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.report_post_fragment_toolbar);
    }

    // init fragment arguments
    private void initFragArg(final Bundle bundle){
        if (bundle == null)
            return;

        if (bundle.containsKey(Constants.BUNDLE_REPORT_PARSE_OBJECT))
            parseObject = bundle.getParcelable(Constants.BUNDLE_REPORT_PARSE_OBJECT);
    }

    // setting fragment views
    private void setViews(){
        if (parseObject == null)
            return;

        ParseUser parseUser = parseObject.getParseUser(ParseConstants.POST_OWNER);
        if (parseUser != null)
            postOwner.setText(parseUser.getUsername());

        postID.setText(parseObject.getObjectId());

        int contentType = parseObject.getInt(ParseConstants.CONTENT_TYPE);
        switch (contentType){
            case Constants.UPLOAD_IMAGE:
                postContentType.setText(getString(R.string.there_images_home_fragment));
                break;

            case Constants.UPLOAD_BLOG:
                postContentType.setText(getString(R.string.blog_home_fragment));
                break;

            case Constants.UPLOAD_DREAM:
                postContentType.setText(getString(R.string.dreams_home_fragment));
                break;

            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:
                postContentType.setText(getString(R.string.quotes_and_thought_home_fragment));
                break;

            case Constants.UPLOAD_NEWS_AND_UPDATE:
                postContentType.setText(getString(R.string.news_and_updates_home_fragment));
                break;

            case Constants.UPLOAD_MEMES_AND_COMEDY:
                postContentType.setText(getString(R.string.memes_and_comedy_home_fragment));
                break;

            case Constants.UPLOAD_STORY:
                postContentType.setText(getString(R.string.stories_home_fragment));
                break;

            case Constants.UPLOAD_POEM:
                postContentType.setText(getString(R.string.poems_home_fragment));
                break;

            case Constants.UPLOAD_GIVE_QUESTION:
                postContentType.setText(getString(R.string.answer_there_question));
                break;
        }

        setRecyclerViewAdapter();
    }

    // Setting report type recycler view adapter
    private void setRecyclerViewAdapter(){
        reportTypes.add(getString(R.string.report_type_one));
        reportTypes.add(getString(R.string.report_type_two));
        reportTypes.add(getString(R.string.report_type_three));
        reportTypes.add(getString(R.string.report_type_four));
        reportTypes.add(getString(R.string.report_type_five));
        reportTypes.add(getString(R.string.report_type_six));
        reportTypes.add(getString(R.string.report_type_seven));
        reportTypes.add(getString(R.string.report_type_eight));

        ReportTypesAdapter reportTypesAdapter = new ReportTypesAdapter(reportTypes,mContext,this);
        reportTypesRecyclerView.setAdapter(reportTypesAdapter);
        reportTypesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Report post
        reportPostBtn.setOnClickListener(view1 -> {
            if (IsInternetAvailable.isInternetAvailable(mContext) && parseObject != null){
                String repo;
                if ((repo = userReport()) != null)
                    reportPost(repo,parseObject);
            }
            else
                Toast.makeText(mContext, "No Internet Available", Toast.LENGTH_SHORT).show();
        });

    }

    private String userReport(){
        final String userRepo = postReportEditText.getText().toString().trim();
        if (userRepo.length() > 0)
            return userRepo;
        else {
            postReportEditText.setError("This filed can't be empty");
            return null;
        }
    }

    // Enabling or disabling the report btn
    private void enableReportBtn(final boolean enableBtn){
        reportPostBtn.setEnabled(enableBtn);
        if (enableBtn){
            reportPostBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
            reportPostBtn.setText(getString(R.string.report_post));
        }
        else{
            reportPostBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
            reportPostBtn.setText(getString(R.string.reporting));
        }
    }

    // Uploading data to server
    private void reportPost(final String report, final ParseObject parseObject){
        enableReportBtn(false);
        final ParseObject reportObj = new ParseObject(ParseConstants.CONTENT_REPORTS_CLASS_NAME);
        reportObj.put(ParseConstants.REPORT_REASON,report);
        reportObj.put(ParseConstants.REPORT_OWNER,ParseUser.getCurrentUser());
        reportObj.put(ParseConstants.REPORT_POST,parseObject);
        reportObj.saveInBackground(e -> {
            enableReportBtn(true);
            showSnackBar(e == null);
        });
    }

    // Showing snackBar after success or fail
    private void showSnackBar(final boolean success){
        if (success){
            Snackbar.make(reportPostBtn,"The post reported successfully, we will take action on it.",3000)
                    .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                    .setBackgroundTint(ContextCompat.getColor(mContext,R.color.upload_logo_color))
                    .show();
        }
        else{
            Snackbar.make(reportPostBtn,"Failed to report post. Please try again or report us through mail (support@sakesnake.com)",5000)
                    .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                    .setBackgroundTint(ContextCompat.getColor(mContext,R.color.red_help_people))
                    .show();
        }
    }

    @Override
    public void selectedPosition(int position) {
        postReportEditText.setText(reportTypes.get(position));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        reportTypesRecyclerView = null;
        postReportEditText = null;
        postOwner = null;
        postID = null;
        postContentType = null;
        reportPostBtn = null;
        fragmentToolbar = null;
        reportTypes.clear();
    }
}