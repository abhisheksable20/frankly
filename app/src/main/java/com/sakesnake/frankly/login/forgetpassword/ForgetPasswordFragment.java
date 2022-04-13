package com.sakesnake.frankly.login.forgetpassword;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseCloud;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public class ForgetPasswordFragment extends Fragment {

    private View rootView;
    private EditText getEmailOrUsernameEditText;
    private TextView resetUsingEmailOrUsernameTextView, headingToggleEmailUsernameTextView;
    private Toolbar toolbar;
    private ProgressBar resetPasswordProgressBar;
    private Button resetPasswordBtn;
    private Context mContext;
    private boolean resetUsingEmail = true;
    private NavController forgetPasswordNavController;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_forget_password, container, false);

        // Getting nav controller
        forgetPasswordNavController = Navigation.findNavController(getActivity(),R.id.main_app_fragment);

        // init views
        getEmailOrUsernameEditText = (EditText) rootView.findViewById(R.id.get_forget_input_edit_text);
        resetPasswordBtn = (Button) rootView.findViewById(R.id.forget_password_fragment_reset_password_button);
        resetUsingEmailOrUsernameTextView = (TextView) rootView.findViewById(R.id.forget_password_fragment_reset_using_mail_or_username_text_view);
        headingToggleEmailUsernameTextView = (TextView) rootView.findViewById(R.id.enter_your_registered_toggle_text_view);
        resetPasswordProgressBar = (ProgressBar) rootView.findViewById(R.id.resetting_password_progress_bar);
        toolbar = (Toolbar) rootView.findViewById(R.id.forget_password_fragment_toolbar);

        SpannableString spannableString = new SpannableString(getResources().getString(R.string.reset_using_username));
        spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        resetUsingEmailOrUsernameTextView.setText(spannableString);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sending link to reset password Button
        resetPasswordBtn.setOnClickListener(view1 -> {
            if (IsInternetAvailable.isInternetAvailable(mContext)){
                final String inputText = getEmailOrUsernameEditText.getText().toString().trim().toLowerCase();
                if (isInputFieldFilledProperly(inputText)){

                    passwordResettingStart(true);

                    // Reset password using user username through Parse Cloud Code
                    if(!(resetUsingEmail)){
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put(Constants.RESET_PASSWORD_USING_USERNAME,inputText);
                        ParseCloud.callFunctionInBackground("resetPasswordUsingUsername",hashMap,(message,e)->{
                            if (e == null){
                                setSnackBar(view1,message.toString(),ContextCompat.getColor(mContext,R.color.upload_logo_color),3000);
                            }else{
                                setSnackBar(view1,e.getMessage(),
                                        ContextCompat.getColor(mContext,R.color.red_help_people),1500);
                            }

                            passwordResettingStart(false);
                        });
                    }

                    // Reset password using email address inside app directly
                    else{
                        ParseUser.requestPasswordResetInBackground(inputText, e ->{
                            if (e == null){
                                setSnackBar(view1,
                                        "Reset link was sent on your provided email address.",
                                        ContextCompat.getColor(mContext,R.color.upload_logo_color),3000);
                            }else{
                                setSnackBar(view1,"Something went wrong. Please try again.",
                                        ContextCompat.getColor(mContext,R.color.red_help_people),1500);
                            }

                            passwordResettingStart(false);
                        });
                    }
                }
            }else{
                setSnackBar(view1,"No Internet Connection",ContextCompat.getColor(mContext,R.color.white),1500);
            }
        });

        // Toggling between reset password using email or username
        resetUsingEmailOrUsernameTextView.setOnClickListener(view1 -> {
            SpannableString spannableString;

            // Was reset using user email address now reset using username
            if (resetUsingEmail){
                resetUsingEmail = false;
                spannableString = new SpannableString(getResources().getString(R.string.reset_using_email));
                spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                getEmailOrUsernameEditText.setHint(getResources().getString(R.string.reset_password_using_username_hint));
                headingToggleEmailUsernameTextView.setText(getResources().getString(R.string.enter_your_username));
            }
            // Was reset using user username now reset using email address
            else{
                resetUsingEmail = true;
                spannableString = new SpannableString(getResources().getString(R.string.reset_using_username));
                spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                getEmailOrUsernameEditText.setHint(getResources().getString(R.string.reset_password_hint));
                headingToggleEmailUsernameTextView.setText(getResources().getString(R.string.enter_your_registered_email));
            }

            resetUsingEmailOrUsernameTextView.setText(spannableString);
        });

        // Navigating back to login fragment
        toolbar.setNavigationOnClickListener(view1 -> {
            forgetPasswordNavController.navigateUp();
        });
    }

    // Showing Setting SnackBar
    private void setSnackBar(final View view, final String message, final int backgroundColor,final int duration){
        Snackbar.make(view,message,duration)
                .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                .setBackgroundTint(backgroundColor)
                .show();
    }

    // Enabling disabling views
    private void passwordResettingStart(boolean disableResetBtn){
        // Reset process has been started
        if (disableResetBtn){
            resetPasswordBtn.setEnabled(false);
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(resetPasswordBtn,"scaleX",1.0f,0f);
            animator1.start();
            resetPasswordProgressBar.setVisibility(View.VISIBLE);
        }
        // Reset process ended
        else{
            resetPasswordBtn.setEnabled(true);
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(resetPasswordBtn,"scaleX",0f,1.0f);
            animator1.start();
            resetPasswordProgressBar.setVisibility(View.GONE);
        }
    }

    // Checking is getUsernameOrEmail filled properly or not
    private boolean isInputFieldFilledProperly(final String inputText){
        // Check is email written properly
        if (resetUsingEmail){
            if (!(inputText.contains(".com")  && inputText.contains("@") && inputText.length() > 6)){
                getEmailOrUsernameEditText.setError("Please enter valid email.");
                return false;
            }
            else{
                return true;
            }
        }
        // Check is username written properly
        else{
            if (inputText.length() <= 3  ||  inputText.length() >= 18){
                getEmailOrUsernameEditText.setError("Please enter valid username");
                return false;
            }
            else{
                return true;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getEmailOrUsernameEditText = null;
        resetUsingEmailOrUsernameTextView = null;
        resetPasswordBtn = null;
        resetPasswordProgressBar = null;
        headingToggleEmailUsernameTextView = null;
    }
}