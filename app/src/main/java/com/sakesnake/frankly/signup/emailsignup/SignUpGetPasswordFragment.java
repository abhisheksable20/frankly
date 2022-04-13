package com.sakesnake.frankly.signup.emailsignup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

public class SignUpGetPasswordFragment extends Fragment {
    private EditText getPasswordEditText, getConfirmPasswordEditText;
    private ImageView createAccountImageView;
    private AnimatedVectorDrawableCompat drawableCompat;
    private NavController getPasswordController;
    private View rootView;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sign_up_get_password, container, false);

        //init Views
        getPasswordEditText = (EditText) rootView.findViewById(R.id.get_password_edit_text);
        getConfirmPasswordEditText = (EditText) rootView.findViewById(R.id.get_confirm_password_edit_text);
        createAccountImageView = (ImageView) rootView.findViewById(R.id.create_accont_image_view);

        //getting Controller
        getPasswordController = Navigation.findNavController(getActivity(), R.id.main_app_fragment);

        drawableCompat = AnimatedVectorDrawableCompat.create(mContext, R.drawable.next_arrow_vector_animation);
        createAccountImageView.setImageDrawable(drawableCompat);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createAccountImageView.setOnClickListener(v ->
        {
            if (IsInternetAvailable.isInternetAvailable(mContext)) {
                if (isPasswordEnteredIsValid() && isPasswordsAreMatching()) {
                    Toast.makeText(mContext, "Creating your account please wait...", Toast.LENGTH_SHORT).show();
                    if (drawableCompat != null) {
                        if (!(drawableCompat.isRunning())) {
                            drawableCompat.start();
                        }
                    }
                    createAccountInSakesnake(getPasswordEditText.getText().toString().trim());
                }
            } else {
                Snackbar.make(v, "No Internet Connection", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(ContextCompat.getColor(mContext, R.color.white)).show();
            }
        });
    }

    private boolean isPasswordEnteredIsValid() {
        if (getPasswordEditText.getText().toString().trim().isEmpty() && getConfirmPasswordEditText.getText().toString().trim().isEmpty()) {
            getPasswordEditText.setError("Enter your password");
            return false;
        } else if (getPasswordEditText.getText().toString().trim().isEmpty()) {
            getPasswordEditText.setError("Enter your password");
            return false;
        } else if (getConfirmPasswordEditText.getText().toString().trim().isEmpty()) {
            getConfirmPasswordEditText.setError("Confirm your password");
            return false;
        } else if (getPasswordEditText.getText().toString().trim().length() < 4) {
            getPasswordEditText.setError("Password must be minimum 4 character");
            return false;
        } else {
            return true;
        }
    }

    private boolean isPasswordsAreMatching() {
        if (getConfirmPasswordEditText.getText().toString().trim().equals(getPasswordEditText.getText().toString().trim())) {
            return true;
        } else {
            getConfirmPasswordEditText.setError("Enter the same password as above");
            return false;
        }
    }

    private void createAccountInSakesnake(String password) {
        Bundle bundle = new Bundle();
        getPasswordEditText.setError(null);
        getConfirmPasswordEditText.setError(null);
        ParseUser user = new ParseUser();
        user.setUsername(getArguments().getString(Constants.BUNDLE_SIGNUP_USERNAME_KEY));
        user.setPassword(password);
        user.setEmail(getArguments().getString(Constants.BUNDLE_SIGNUP_EMAIL_TRANSFERRED_KEY));

        user.signUpInBackground(e ->
        {
            if (e == null) {
                ParseUser.logOut();
                bundle.putBoolean(Constants.BUNDLE_ACCOUNT_CREATED_SUCCESSFULLY, true);
                getPasswordController.navigate(R.id.action_signUpGetPasswordFragment_to_loginFragment, bundle);
            } else {
                ParseUser.logOut();
                bundle.putBoolean(Constants.BUNDLE_ACCOUNT_CREATION_FAILED, true);
                bundle.putString(Constants.BUNDLE_ACCOUNT_CREATION_FAILED_REASON, e.getMessage());
                getPasswordController.navigate(R.id.action_signUpGetPasswordFragment_to_signUpScreenFragment, bundle);
            }
            if (drawableCompat != null) {
                if (drawableCompat.isRunning()) {
                    drawableCompat.stop();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPasswordEditText = null;
        getConfirmPasswordEditText = null;
        createAccountImageView = null;
        rootView = null;
    }
}