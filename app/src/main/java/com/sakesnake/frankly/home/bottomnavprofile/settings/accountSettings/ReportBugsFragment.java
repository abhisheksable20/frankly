package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseObject;

public class ReportBugsFragment extends Fragment {

    private View rootView;

    private EditText bugTitleEditText, bugLocationEditText, bugMessageEditText;

    private Button sendBugReportBtn;

    private Toolbar fragmentToolbar;

    private NavController navController;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_report_bugs, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init fragment views
        initFragmentViews();

        return rootView;
    }

    // initializing fragment views
    private void initFragmentViews(){
        bugTitleEditText = (EditText) rootView.findViewById(R.id.bug_title_edit_text);
        bugLocationEditText = (EditText) rootView.findViewById(R.id.bug_location_edit_text);
        bugMessageEditText = (EditText) rootView.findViewById(R.id.bug_message_edit_text);
        sendBugReportBtn = (Button) rootView.findViewById(R.id.send_bug_report_btn);
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.report_bug_fragment_toolbar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Send bug report to parse server
        sendBugReportBtn.setOnClickListener(view1 -> {
            final String title = bugTitleEditText.getText().toString().trim();
            final String location = bugLocationEditText.getText().toString().trim();
            final String message = bugMessageEditText.getText().toString().trim();
            if (isFieldsWrittenProperly(title,location,message)) {
                enableOrDisableBtn(false);
                sendBugReport(title, location, message);
            }
        });
    }

    // Is all fields written properly
    private boolean isFieldsWrittenProperly(final String title, final String location, final String message){
        if (title.equals("") || title.length() < 1) {
            bugTitleEditText.setError("Title cannot be empty");
            return false;
        }

        if (location.equals("") || location.length() < 1) {
            bugLocationEditText.setError("Bug location cannot be empty");
            return false;
        }

        if (message.equals("") || message.length() < 1) {
            bugMessageEditText.setError("Message cannot be empty");
            return false;
        }

        return true;
    }

    // Sending bug report to parse server
    private void sendBugReport(final String title, final String location, final String message){
        ParseObject parseObject = new ParseObject(ParseConstants.REPORT_BUG_CLASS_NAME);
        parseObject.put(ParseConstants.BUG_TITLE,title);
        parseObject.put(ParseConstants.BUG_LOCATION,location);
        parseObject.put(ParseConstants.BUG_MESSAGE,message);
        parseObject.saveInBackground(e -> {
            if (e == null)
                showSnackBar("Your response recorded successfully.",ContextCompat.getColor(mContext,R.color.upload_logo_color));
            else
                showSnackBar("Failed to record your response.Please try again.",ContextCompat.getColor(mContext,R.color.red_help_people));

            enableOrDisableBtn(true);
        });
    }

    private void enableOrDisableBtn(final boolean enable){
        sendBugReportBtn.setEnabled(enable);
        if (enable){
            sendBugReportBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
            sendBugReportBtn.setText(getString(R.string.report_bug));
        }
        else{
            sendBugReportBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
            sendBugReportBtn.setText(getString(R.string.reporting_bug));
        }
    }

    private void showSnackBar(final String msg, final int bgColor){
        Snackbar.make(sendBugReportBtn,msg,5000)
                .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                .setBackgroundTint(bgColor).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        bugTitleEditText = null;
        bugLocationEditText = null;
        bugMessageEditText = null;
        sendBugReportBtn = null;
        fragmentToolbar = null;
    }
}