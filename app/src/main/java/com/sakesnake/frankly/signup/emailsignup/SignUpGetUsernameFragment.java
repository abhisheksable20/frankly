package com.sakesnake.frankly.signup.emailsignup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SignUpGetUsernameFragment extends Fragment {

    private View rootView;

    private AnimatedVectorDrawableCompat drawableCompat;

    private EditText getUserNameEditText;

    private ImageView goToGetPasswordImageView;

    private Context mContext;

    private TextView isUserNameAvailableTextView;

    private NavController usernameController;

    private String perfectUsername;

    private boolean isUserNameExist = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sign_up_get_username, container, false);

        //Getting controller
        usernameController = Navigation.findNavController(getActivity(), R.id.main_app_fragment);

        //init views
        getUserNameEditText = (EditText) rootView.findViewById(R.id.get_user_name_edit_text);
        goToGetPasswordImageView = (ImageView) rootView.findViewById(R.id.go_to_get_signup_password_image_view);
        isUserNameAvailableTextView = (TextView) rootView.findViewById(R.id.is_user_name_available_text_view);


        //creating drawable
        drawableCompat = AnimatedVectorDrawableCompat.create(mContext, R.drawable.next_arrow_vector_animation);
        goToGetPasswordImageView.setImageDrawable(drawableCompat);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!(IsInternetAvailable.isInternetAvailable(mContext))) {
            Snackbar.make(view, "No Internet Connection", 2000).setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people)).setTextColor(ContextCompat.getColor(mContext, R.color.white)).show();
        }

        goToGetPasswordImageView.setOnClickListener(v -> {
            if (IsInternetAvailable.isInternetAvailable(mContext)) {
                if (checkUsernameThenProceed() && isUserNameExist) {
                    proceedToGetPassword(getUserNameEditText.getText().toString().toLowerCase().trim());
                }
            }
        });


        //Edit text checking text change listener
        getUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (IsInternetAvailable.isInternetAvailable(mContext)) {
                    if (drawableCompat != null) {
                        if (!(drawableCompat.isRunning())) {
                            drawableCompat.start();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (IsInternetAvailable.isInternetAvailable(mContext)) {
                    isUsernameAvailable(s.toString().trim());
                }
            }
        });
    }

    private boolean checkUsernameThenProceed() {
        if (getUserNameEditText.getText().toString().trim().length() == 0) {
            getUserNameEditText.setError("Set username to proceed");
            return false;
        } else if (getUserNameEditText.getText().toString().trim().length() <= 3) {
            getUserNameEditText.setError("Username length must be greater than three characters");
            return false;
        } else if (getUserNameEditText.getText().toString().trim().length() >= 18) {
            getUserNameEditText.setError("Username length must smaller than 18 characters");
            return false;
        } else {
            return true;
        }
    }

    private void isUsernameAvailable(String username) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", username);

        userQuery.getFirstInBackground((object, e) ->
        {
            if (e == null) {
                if (drawableCompat != null) {
                    if (drawableCompat.isRunning()) {
                        drawableCompat.stop();
                    }
                }
                isUserNameAvailableTextView.setText(getString(R.string.this_username_is_not_available));
            } else {
                if (drawableCompat != null) {
                    if (drawableCompat.isRunning()) {
                        drawableCompat.stop();
                    }
                }
                isUserNameExist = true;
                isUserNameAvailableTextView.setText("");
            }
        });
    }

    private void proceedToGetPassword(String perfectUsername) {
        getUserNameEditText.setError(null);
        this.perfectUsername = perfectUsername;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_SIGNUP_USERNAME_KEY, perfectUsername);
        bundle.putString(Constants.BUNDLE_SIGNUP_EMAIL_TRANSFERRED_KEY, getArguments().getString(Constants.BUNDLE_SIGNUP_EMAIL_KEY));
        usernameController.navigate(R.id.action_signUpGetUsernameFragment_to_signUpGetPasswordFragment, bundle);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getUserNameEditText = null;
        goToGetPasswordImageView = null;
        isUserNameAvailableTextView = null;
        rootView = null;
    }
}