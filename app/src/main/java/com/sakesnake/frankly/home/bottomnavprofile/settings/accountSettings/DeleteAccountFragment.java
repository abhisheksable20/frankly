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
import android.widget.Toast;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class DeleteAccountFragment extends Fragment {

    private View rootView;

    private EditText currentPasswordEditText, deleteReasonEditText;

    private Button deleteAccountBtn;

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

        rootView = inflater.inflate(R.layout.fragment_delete_account, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init views
        initFragmentViews();

        return rootView;
    }

    // initializing fragment views
    private void initFragmentViews(){
        currentPasswordEditText = (EditText) rootView.findViewById(R.id.current_password_edit_text);
        deleteReasonEditText = (EditText) rootView.findViewById(R.id.account_delete_reason_edit_text);
        deleteAccountBtn = (Button) rootView.findViewById(R.id.delete_account_permanently_btn);
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.delete_account_fragment_toolbar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());

        // Deleting account permanently
        deleteAccountBtn.setOnClickListener(view1 -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String deleteReason = deleteReasonEditText.getText().toString().trim();
            if (isFieldsWrittenProperly(currentPassword,deleteReason)){
                enableOrDisableBtn(false);
                loginWithCurrentPassword(currentPassword,deleteReason);
            }
        });

    }

    // Is all fields written properly
    private boolean isFieldsWrittenProperly(final String currentPassword,final String deleteReason){
        if (currentPassword.equals("") || currentPassword.length() <= 0) {
            currentPasswordEditText.setError("Please enter your password");
            return false;
        }

        if (deleteReason.equals("") || deleteReason.length() <= 0) {
            deleteReasonEditText.setError("This field cannot be empty");
            return false;
        }

        return true;
    }

    private void loginWithCurrentPassword(final String currentPassword, final String deleteReason){
        ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), currentPassword, (user, e) -> {
            if (e == null)
                saveAccountDeleteReason(deleteReason, user.getUsername());
            else{
                Log.e(Constants.TAG, "Cannot authenticate the user with current password: "+e.getMessage());
                String failedToLogin = "Cannot authenticate you with current password you provided";
                showSnackBar(failedToLogin,ContextCompat.getColor(mContext,R.color.red_help_people));
                enableOrDisableBtn(true);
            }
        });
    }

    // saving the account delete reason
    private void saveAccountDeleteReason(final String deleteReason, final String username){
        final ParseObject parseObject = new ParseObject(ParseConstants.ACCOUNT_DELETE_REASONS_CLASS_NAME);
        parseObject.put(ParseConstants.DELETE_REASON,deleteReason);
        parseObject.put(ParseConstants.DELETED_ACCOUNT_USERNAME,username);
        parseObject.saveInBackground(e -> {
            if (e == null)
                deleteAccountPermanently();
            else{
                String failedToAddReason = "Something went wrong. Please try again later";
                showSnackBar(failedToAddReason,ContextCompat.getColor(mContext,R.color.red_help_people));
                enableOrDisableBtn(true);
            }
        });
    }

    // Delete account permanently
    private void deleteAccountPermanently(){
        final ParseUser user = ParseUser.getCurrentUser();
        user.deleteInBackground(e -> {
            if (e == null)
                doFlushingOfUserData();
            else{
                String failedToDelete = "Failed to delete your account. Please try again";
                enableOrDisableBtn(true);
                showSnackBar(failedToDelete,ContextCompat.getColor(mContext,R.color.red_help_people));
            }
        });
    }

    // Logging out user permanently
    private void doFlushingOfUserData(){
        ParseUser.logOutInBackground(e -> {
            ParseObject.unpinAllInBackground();
            ParseUser.unpinAllInBackground();
            final SessionErrorViewModel sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);
            final FeedsArgsViewModel feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
            feedsArgsViewModel.clearAllArgs();
            Toast.makeText(mContext, "Your account deleted successfully.", Toast.LENGTH_LONG).show();
            sessionErrorViewModel.isSessionExpired(true);
        });
    }

    // Show snackBar to user
    private void showSnackBar(final String msg, final int bgColor){
        Snackbar.make(deleteAccountBtn,msg,3000)
                .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                .setBackgroundTint(bgColor)
                .show();
    }

    // Enable or disable the btn
    private void enableOrDisableBtn(final boolean enable){
        deleteAccountBtn.setEnabled(enable);
        if (enable){
            deleteAccountBtn.setText(getString(R.string.delete_account_permanently));
            deleteAccountBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.red_help_people)));
        }
        else{
            deleteAccountBtn.setText(getString(R.string.deleting_account));
            deleteAccountBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_red_color)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        currentPasswordEditText = null;
        deleteAccountBtn = null;
        deleteReasonEditText = null;
        fragmentToolbar = null;
    }
}