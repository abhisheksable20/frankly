package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

import android.content.Context;
import android.content.res.ColorStateList;
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
import android.widget.EditText;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseCloud;
import com.parse.ParseUser;

import java.util.HashMap;

public class ChangePasswordFragment extends Fragment {

    private View rootView;

    private EditText getCurrentPasswordEditText, getNewPasswordEditText, getConfirmedPasswordEditText;

    private Button saveChangesBtn;

    private Toolbar fragmentToolbar;

    private Context mContext;

    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        initFragmentView();

        return rootView;
    }

    // initializing fragment views
    private void initFragmentView(){
        getCurrentPasswordEditText = (EditText) rootView.findViewById(R.id.current_password_edit_text);
        getNewPasswordEditText = (EditText) rootView.findViewById(R.id.new_password_edit_text);
        getConfirmedPasswordEditText = (EditText) rootView.findViewById(R.id.confirm_password_edit_text);
        saveChangesBtn = (Button) rootView.findViewById(R.id.save_password_btn);
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.change_password_fragment_toolbar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentToolbar.setNavigationOnClickListener(view12 -> navController.navigateUp());

        saveChangesBtn.setOnClickListener(view1 -> {
            final String currentPassword = getCurrentPasswordEditText.getText().toString().trim();
            final String newPassword = getNewPasswordEditText.getText().toString().trim();
            final String confirmedPassword = getConfirmedPasswordEditText.getText().toString().trim();
            if (isCurrentPasswordAvailable(currentPassword) && isNewAndConfirmedPasswordSame(newPassword,confirmedPassword)) {
                enableOrDisableBtn(false);
                checkCurrentPasswordAndProceed(currentPassword, newPassword);
            }
        });
    }

    private void enableOrDisableBtn(final boolean enable){
        if (enable){
            saveChangesBtn.setEnabled(true);
            saveChangesBtn.setText(getString(R.string.account_settings_save_and_logout_all_active_session));
            saveChangesBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
        }
        else{
            saveChangesBtn.setEnabled(false);
            saveChangesBtn.setText(getString(R.string.saving_changes));
            saveChangesBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
        }
    }

    // Is current password written properly
    private boolean isCurrentPasswordAvailable(final String currentPassword){
        if (!currentPassword.equals("") && currentPassword.length() > 0)
            return true;

        getCurrentPasswordEditText.setError("Current password cannot be empty.");
        return false;
    }

    // Is new password and confirm are same
    private boolean isNewAndConfirmedPasswordSame(final String newPassword, final String confirmPassword){
        if (newPassword.equals(confirmPassword))
            return true;
        else{
            getConfirmedPasswordEditText.setError("Confirm password not matching new password.");
            return false;
        }
    }

    // Checking is current password is correct or not by logging in and the proceed if logged in
    private void checkCurrentPasswordAndProceed(final String currentPassword, final String newPassword){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null)
            return;

        ParseUser.logInInBackground(currentUser.getUsername(), currentPassword, (user, e) -> {
            if (e == null)
                changeAccountPassword(user,newPassword);
            else {
                enableOrDisableBtn(true);
                showSnackBar("Cannot authenticate account with current password provided", ContextCompat.getColor(mContext, R.color.red_help_people));
            }
        });
    }

    // Final step to change account password
    private void changeAccountPassword(final ParseUser currentLoggedUser,final String password){
        currentLoggedUser.setPassword(password);
        currentLoggedUser.saveInBackground(e -> {
            if (e == null) {
                showSnackBar("Password changed successfully.",ContextCompat.getColor(mContext,R.color.upload_logo_color));
                removeSessions();
            }
            else {
                enableOrDisableBtn(true);
                showSnackBar("Something went wrong.Please try again", ContextCompat.getColor(mContext, R.color.red_help_people));
            }
        });
    }

    // Removing currently logged in sessions including the current session
    private void removeSessions(){
        final HashMap<String,String> hashMap = new HashMap<>();
        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_SESSIONS,hashMap, (object,e) -> {
            if (e != null) {
                enableOrDisableBtn(true);
                Log.e(Constants.TAG, "Failed to remove sessions: " + e.getMessage());
            }
            else
                logoutTheCurrentUser();
        });
    }

    private void logoutTheCurrentUser(){
        ParseUser.logOutInBackground(e -> {
            if (e == null) {
                Log.d("ScaleTag", "logoutTheCurrentUser logout succeed: ");
                FeedsArgsViewModel feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
                SessionErrorViewModel sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            }
            else {
                enableOrDisableBtn(true);
                Log.d("ScaleTag", "logoutTheCurrentUser Failed to logout: ");
                Log.e(Constants.TAG, "Cannot logout the current user: " + e.getMessage());
            }
        });
    }

    // Showing the snackBar after process is done
    private void showSnackBar(final String message, final int color){
        Snackbar.make(saveChangesBtn,message,3000)
                .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                .setBackgroundTint(color)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getCurrentPasswordEditText = null;
        getNewPasswordEditText = null;
        getConfirmedPasswordEditText = null;
        saveChangesBtn = null;
        fragmentToolbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}