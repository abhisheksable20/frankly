package com.sakesnake.frankly.signup.emailsignup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseACL;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

public class SignUpGetNameFragment extends Fragment {

    private View rootView;
    private ImageView setOriginalNameImageView;
    private EditText getOriginalNameEditText;
    private TextView backToLoginTextView,accountVerifiedMessage;
    private ProgressBar logoutProgressbar;
    private NavController navController;
    private AnimatedVectorDrawableCompat drawableCompat;
    private Context mContext;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sign_up_get_name, container, false);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.main_app_fragment);

        // init views
        setOriginalNameImageView = (ImageView) rootView.findViewById(R.id.get_name_fragment_image_view);
        getOriginalNameEditText = (EditText) rootView.findViewById(R.id.get_name_fragment_edit_text);
        backToLoginTextView = (TextView) rootView.findViewById(R.id.get_name_fragment_back_to_login_text_view);
        accountVerifiedMessage = (TextView) rootView.findViewById(R.id.get_name_fragment_account_verified_message);
        logoutProgressbar = (ProgressBar) rootView.findViewById(R.id.get_name_fragment_progress_bar);

        drawableCompat = AnimatedVectorDrawableCompat.create(mContext,R.drawable.next_arrow_vector_animation);
        setOriginalNameImageView.setImageDrawable(drawableCompat);

        String username = ParseUser.getCurrentUser().getUsername()+"";

        // Setting verified message
        SpannableString spannableString = new SpannableString(username+" "+getResources().getString(R.string.account_created_message));
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.upload_logo_color)),
                0,username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        accountVerifiedMessage.setText(spannableString);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Going back to login screen
        backToLoginTextView.setOnClickListener(view1 -> {
            backToLoginTextView.setEnabled(false);
            logoutProgressbar.setVisibility(View.VISIBLE);
            backToLoginTextView.setText(getResources().getString(R.string.logging_out_user_text));
            backToLoginTextView.setTextColor(ContextCompat.getColor(mContext,R.color.light_upload_color));
            if (IsInternetAvailable.isInternetAvailable(mContext)) {
                ParseUser.logOutInBackground(e ->{
                    if (e == null) {
                        navController.navigate(R.id.action_signUpGetNameFragment_to_loginFragment);
                    }
                    else {
                        showSnackBar(view1, e.getMessage() + "");
                        backToLoginTextView.setEnabled(true);
                        logoutProgressbar.setVisibility(View.GONE);
                    }
                });
            }else{
                showSnackBar(view1,"No Internet connection");
            }
        });

        // Setting user original name
        setOriginalNameImageView.setOnClickListener(view1 -> {
            if (IsInternetAvailable.isInternetAvailable(mContext)){
                setOriginalNameImageView.setEnabled(false);
                if (getOriginalNameEditText.getText().toString().trim().length() > 0){
                    if (drawableCompat != null){
                        drawableCompat.start();
                    }
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseObject parseObject = new ParseObject(ParseConstants.USERS_PRIVATE_DATA_CLASS_NAME);
                    ParseACL parseACL = new ParseACL(user);
                    parseACL.setPublicReadAccess(false);
                    parseObject.setACL(parseACL);
                    user.put(ParseConstants.PRIVATE_DATA,parseObject);
                    user.put(ParseConstants.PROFILE_ORIGINAL_NAME,getOriginalNameEditText.getText().toString().trim());
                    user.saveInBackground(e -> {
                        if (e == null){
                            navController.navigate(R.id.action_signUpGetNameFragment_to_homeFragment);
                        }else{
                            showSnackBar(view1,"Something went wrong. Please try again.");
                        }
                        setOriginalNameImageView.setEnabled(true);
                        if (drawableCompat.isRunning())
                            drawableCompat.stop();
                    });
                }else{
                    getOriginalNameEditText.setError("Please enter your name.");
                    setOriginalNameImageView.setEnabled(true);
                }
            }else{
                showSnackBar(view1,"No Internet Connection");
            }
        });
    }

    // Showing snackbar with white text and red background
    private void showSnackBar(View view, @NonNull String message){
        Snackbar.make(view,message,1500)
                .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                .setBackgroundTint(ContextCompat.getColor(mContext,R.color.red_help_people))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        getOriginalNameEditText = null;
        setOriginalNameImageView = null;
        backToLoginTextView = null;
    }
}