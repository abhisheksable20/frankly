package com.sakesnake.frankly.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseSessions;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class LoginFragment extends Fragment {
    private View rootView;
    private EditText getLoginUsernameEditText, getLoginPasswordEditText;
    private ImageView loginInAccountImageView;
    private TextView forgetPasswordTextView;
    private AnimatedVectorDrawableCompat drawableCompat;
    private Context mContext;
    private NavController loginNavController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        //getting Controller
        loginNavController = Navigation.findNavController(getActivity(), R.id.main_app_fragment);

        //init views
        getLoginUsernameEditText = (EditText) rootView.findViewById(R.id.get_login_username_edit_text);
        getLoginPasswordEditText = (EditText) rootView.findViewById(R.id.get_login_password_edit_text);
        loginInAccountImageView = (ImageView) rootView.findViewById(R.id.login_in_account_image_view);
        forgetPasswordTextView = (TextView) rootView.findViewById(R.id.forget_password_text_view);

        drawableCompat = AnimatedVectorDrawableCompat.create(mContext, R.drawable.next_arrow_vector_animation);
        loginInAccountImageView.setImageDrawable(drawableCompat);

        if (getArguments() != null) {
            if (getArguments().getBoolean(Constants.BUNDLE_ACCOUNT_CREATED_SUCCESSFULLY))
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Link has been sent on your email verify it before login", 5000)
                        .setTextColor(mContext.getColor(R.color.blue_color))
                        .setBackgroundTint(mContext.getColor(R.color.white))
                        .show();

        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!(IsInternetAvailable.isInternetAvailable(mContext))) {
            Snackbar.make(view, "No Internet Connection", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(ContextCompat.getColor(mContext, R.color.white)).show();
        }

        loginInAccountImageView.setOnClickListener(v ->
        {
            if (IsInternetAvailable.isInternetAvailable(mContext)) {
                if (isCredentialsFilledProperly()) {
                    loginInAccountImageView.setEnabled(false);
                    if (drawableCompat != null) {
                        if (!(drawableCompat.isRunning())) {
                            drawableCompat.start();
                        }
                    }
                    loginUser(getLoginUsernameEditText.getText().toString().trim(), getLoginPasswordEditText.getText().toString().trim(), v);
                }
            } else {
                Snackbar.make(v, "No Internet Connection", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(ContextCompat.getColor(mContext, R.color.white)).show();
            }

        });

        // Navigating forget password fragment
        forgetPasswordTextView.setOnClickListener(view1 -> {
            loginNavController.navigate(R.id.action_loginFragment_to_forgetPasswordFragment);
        });


    }

    private boolean isCredentialsFilledProperly() {
        if (getLoginUsernameEditText.getText().toString().trim().isEmpty() && getLoginPasswordEditText.getText().toString().trim().isEmpty()) {
            getLoginUsernameEditText.setError("Enter your username");
            return false;
        } else if (getLoginUsernameEditText.getText().toString().trim().isEmpty()) {
            getLoginUsernameEditText.setError("Username is required");
            return false;
        } else if (getLoginPasswordEditText.getText().toString().trim().isEmpty()) {
            getLoginPasswordEditText.setError("Password is required");
            return false;
        } else {
            return true;
        }
    }

    private void loginUser(String username, String password, View view) {
        ParseUser.logInInBackground(username, password, (user, e) -> {
            if (e == null) {
                if (user.getBoolean("emailVerified")) {
                    ParseSessions.saveDeviceName();
                    if (user.getString("originalName") == null)
                        loginNavController.navigate(R.id.action_loginFragment_to_signUpGetNameFragment);
                    else
                        loginNavController.navigate(R.id.action_loginFragment_to_homeFragment);
                } else
                    Snackbar.make(view, "Please verify your email first.", 2000).setTextColor(mContext.getColor(R.color.upload_logo_color)).show();
            }
            else {
                if (e.getCode() == 101)
                    Snackbar.make(view, "Please enter valid username or password.", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(mContext.getColor(R.color.white)).show();
                else
                    Snackbar.make(view, "Opps! Something went wrong. Please try again.", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(mContext.getColor(R.color.white)).show();
            }
            if (drawableCompat != null) {
                if (drawableCompat.isRunning())
                    drawableCompat.stop();
            }
            loginInAccountImageView.setEnabled(true);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoginUsernameEditText = null;
        getLoginPasswordEditText = null;
        rootView = null;
    }
}