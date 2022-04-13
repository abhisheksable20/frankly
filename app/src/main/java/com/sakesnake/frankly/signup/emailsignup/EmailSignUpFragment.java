package com.sakesnake.frankly.signup.emailsignup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.IsInternetAvailable;
import com.sakesnake.frankly.R;
import com.google.android.material.snackbar.Snackbar;

public class EmailSignUpFragment extends Fragment
{
    private View rootView;

    private EditText getEmailEditText;

    private ImageView goToGetSignUpUsernameImageView;

    private TextView termsAndPolicyTextView;

    private NavController emailSignUpController;

    private String perfectEmail;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_email_sign_up, container, false);

        // getting navigation controller
        emailSignUpController = Navigation.findNavController(getActivity(),R.id.main_app_fragment);

        // init Views
        getEmailEditText = (EditText) rootView.findViewById(R.id.get_email_edit_text);
        goToGetSignUpUsernameImageView = (ImageView) rootView.findViewById(R.id.go_to_get_signup_username_image_view);
        termsAndPolicyTextView = (TextView) rootView.findViewById(R.id.terms_and_policy_text_view);

        setPolicyTextView();

        return rootView;
    }

    // Setting span to policy text view
    private void setPolicyTextView(){
        final String policyString = getString(R.string.check_terms_and_policy);
        SpannableString spannableString = new SpannableString(policyString);
        spannableString.setSpan(new UnderlineSpan(),policyString.indexOf("T"),policyString.indexOf("y"), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        termsAndPolicyTextView.setText(spannableString);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        goToGetSignUpUsernameImageView.setOnClickListener(v ->
        {
            if (!(getEmailEditText.getText().toString().trim().isEmpty()))
            {
                checkEmailThenProceed(getEmailEditText.getText().toString().trim(),v);
            }
            else
            {
                getEmailEditText.setError("Enter your email to proceed");
            }
        });

        // Navigating to privacy policy
        termsAndPolicyTextView.setOnClickListener(view1 -> {
            final Bundle bundle = new Bundle();
            bundle.putInt(Constants.BUNDLE_APP_POLICIES,5);
            bundle.putBoolean(Constants.BUNDLE_POLICY_FROM_SIGN_UP,true);
            emailSignUpController.navigate(R.id.action_emailSignUpFragment_to_appPoliciesFragment2,bundle);
        });
    }

    private void checkEmailThenProceed(String email, View view)
    {
        if (email.length() < 7)
        {
            getEmailEditText.setError("Enter valid email");
        }
        else if (!(email.contains("@"))  ||  !(email.contains(".")))
        {
            getEmailEditText.setError("Enter valid email");
        }
        else
        {
            perfectEmail = email;
            proceedToGetUsername(email,view);
        }
    }

    private void proceedToGetUsername(String perfectEmail,View view)
    {
        if (IsInternetAvailable.isInternetAvailable(mContext))
        {
            getEmailEditText.setError(null);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.BUNDLE_SIGNUP_EMAIL_KEY,perfectEmail);
            emailSignUpController.navigate(R.id.action_emailSignUpFragment_to_signUpGetUsernameFragment,bundle);
        }
        else
        {
            Snackbar.make(view,"No Internet Connection",2000).setBackgroundTint(ContextCompat.getColor(mContext,R.color.red_help_people)).setTextColor(ContextCompat.getColor(mContext,R.color.white)).show();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        getEmailEditText = null;
        goToGetSignUpUsernameImageView = null;
        termsAndPolicyTextView = null;
        rootView = null;
    }
}