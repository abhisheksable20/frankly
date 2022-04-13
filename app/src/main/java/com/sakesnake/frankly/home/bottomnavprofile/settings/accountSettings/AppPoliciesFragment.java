package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseCloud;

import java.util.HashMap;

// 5:- Load Terms of Services
// 6:- Load Privacy policy

public class AppPoliciesFragment extends Fragment {

    private View rootView;

    private WebView webView;

    private Toolbar toolbar;

    private ProgressBar progressBar;

    private Context mContext;

    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_app_policies, container, false);

        //init fragment views
        webView = (WebView) rootView.findViewById(R.id.app_policies_web_view);
        toolbar = (Toolbar) rootView.findViewById(R.id.app_policies_fragment_toolbar);
        progressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);

        initFragmentArgs(getArguments());

        return rootView;
    }

    // initializing fragment arguments
    private void initFragmentArgs(final Bundle bundle){
        if (bundle == null)
            return;

        // Getting nav controller
        if (bundle.containsKey(Constants.BUNDLE_POLICY_FROM_SIGN_UP))
            navController = Navigation.findNavController(getActivity(),R.id.main_app_fragment);
        else
            navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        if (bundle.containsKey(Constants.BUNDLE_APP_POLICIES)) {
            if (bundle.getInt(Constants.BUNDLE_APP_POLICIES) == 5)
                toolbar.setTitle(getString(R.string.app_terms_and_condition));
            else if (bundle.getInt(Constants.BUNDLE_APP_POLICIES) == 6)
                toolbar.setTitle(getString(R.string.profile_settings_privacy_policy));

            loadPolicy(bundle.getInt(Constants.BUNDLE_APP_POLICIES, 0));
        }
    }

    // Getting policy url from parse server (Cloud code)
    private void loadPolicy(final int policy){
        final HashMap<String,Integer> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.POLICIES_KEY,policy);
        ParseCloud.callFunctionInBackground(ParseConstants.LOAD_POLICY_URL,hashMap,(url,e)->{
            progressBar.setVisibility(View.GONE);
            if (e == null && url instanceof String)
                loadUrl((String) url);
            else if (e != null)
                Toast.makeText(mContext, "Failed to load policy. Please visit Sakesnake.com ", Toast.LENGTH_SHORT).show();
        });
    }

    // Loading url in webView
    private void loadUrl(final String url){
        if (webView == null)
            return;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        toolbar.setNavigationOnClickListener(view1 -> {
            if (navController != null)
                navController.navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        webView = null;
        toolbar = null;
        progressBar = null;
    }
}