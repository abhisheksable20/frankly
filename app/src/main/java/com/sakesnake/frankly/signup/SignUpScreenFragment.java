package com.sakesnake.frankly.signup;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

public class SignUpScreenFragment extends Fragment
{
    private MaterialCardView signUpWithEmailCardView;
    private NavController controller;
    private TextView alreadyHaveAnAccount;
    private View rootView;
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
        rootView = inflater.inflate(R.layout.fragment_sign_up_screen, container, false);

        // getting navigation controller
        controller = Navigation.findNavController(getActivity(),R.id.main_app_fragment);

        // init views
        signUpWithEmailCardView = (MaterialCardView) rootView.findViewById(R.id.sign_up_email_card_view);
        alreadyHaveAnAccount = (TextView) rootView.findViewById(R.id.already_have_account_text_view);

        if (getArguments() != null){
            if (getArguments().getBoolean(Constants.BUNDLE_ACCOUNT_CREATION_FAILED,false)) {
                Snackbar.make(getActivity().findViewById(android.R.id.content),
                        getArguments().getString(Constants.BUNDLE_ACCOUNT_CREATION_FAILED_REASON),
                        3000)
                        .setBackgroundTint(ContextCompat.getColor(mContext,R.color.red_help_people))
                        .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                        .show();
            }
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        signUpWithEmailCardView.setOnClickListener(v ->
        {
            controller.navigate(R.id.action_signUpScreenFragment_to_emailSignUpFragment);
        });

        alreadyHaveAnAccount.setOnClickListener(v ->
        {
            controller.navigate(R.id.action_signUpScreenFragment_to_loginFragment);
        });
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        signUpWithEmailCardView = null;
        alreadyHaveAnAccount = null;
        rootView = null;
    }
}