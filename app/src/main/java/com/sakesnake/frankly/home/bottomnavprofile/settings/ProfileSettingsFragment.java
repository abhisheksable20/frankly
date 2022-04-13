package com.sakesnake.frankly.home.bottomnavprofile.settings;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileSettingsFragment extends Fragment implements SettingsAdapterCallback{

    private View rootView;

    private Toolbar profileSettingsToolbar;

    private Button logoutBtn, deleteAccountBtn;

    private RecyclerView accountSettingsRecyclerView;

    private NavController navController;

    private Context mContext;

    private SessionErrorViewModel sessionErrorViewModel;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        // Getting navigation controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // init view models
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        // init views
        profileSettingsToolbar = (Toolbar) rootView.findViewById(R.id.profile_setting_fragment_toolbar);
        logoutBtn = (Button) rootView.findViewById(R.id.profile_settings_fragment_logout_btn);
        accountSettingsRecyclerView = (RecyclerView) rootView.findViewById(R.id.account_settings_recycler_view);
        deleteAccountBtn = (Button) rootView.findViewById(R.id.profile_settings_fragment_delete_account_btn);

        setSettingAdapter();

        return rootView;
    }

    private List<AccountSettings> getAccountSettingList(){
        final List<AccountSettings> accountSettings = new ArrayList<>();
        accountSettings.add(new AccountSettings(1,R.drawable.edit_profile_settings_icon,getString(R.string.profile_settings_edit_profile)));
        accountSettings.add(new AccountSettings(2,R.drawable.active_session_icon,getString(R.string.profile_settings_currently_active_sessions)));
        accountSettings.add(new AccountSettings(3,R.drawable.bug_icon,getString(R.string.profile_settings_report_a_bug)));
        accountSettings.add(new AccountSettings(4,R.drawable.change_password_icon,getString(R.string.profile_settings_change_password)));
        accountSettings.add(new AccountSettings(5,R.drawable.terms_of_service_icon,getString(R.string.app_terms_and_condition)));
        accountSettings.add(new AccountSettings(6,R.drawable.privacy_policy_settings_icon,getString(R.string.profile_settings_privacy_policy)));

        return accountSettings;
    }

    private void setSettingAdapter(){
        AccountSettingsAdapter accountSettingsAdapter = new AccountSettingsAdapter(mContext,getAccountSettingList(),this);
        accountSettingsRecyclerView.setAdapter(accountSettingsAdapter);
        accountSettingsRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2, LinearLayoutManager.VERTICAL,false));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // handling toolbar back navigation
        profileSettingsToolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // log out
        logoutBtn.setOnClickListener(view1 -> {
            enableDisableBtn(false);
            logoutFromSession();
        });

        // navigate to delete account fragment
        deleteAccountBtn.setOnClickListener(view1 -> navController.navigate(R.id.action_profileSettingsFragment_to_deleteAccountFragment));
    }

    // Enable or disable the logout btn
    private void enableDisableBtn(final boolean enable){
        logoutBtn.setEnabled(enable);
        if (enable){
            logoutBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
            logoutBtn.setText(getString(R.string.profile_settings_fragment_logout));
        }
        else{
            logoutBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
            logoutBtn.setText(getString(R.string.logging_out_user_text));
        }
    }

    @Override
    public void getSettingType(int settingType) {
        switch (settingType){
            case 1:{
                navController.navigate(R.id.action_profileSettingsFragment_to_editProfileFragment);
                break;
            }
            case 2:{
                navController.navigate(R.id.action_profileSettingsFragment_to_activeSessionFragment);
                break;
            }
            case 3:{
                navController.navigate(R.id.action_profileSettingsFragment_to_reportBugsFragment);
                break;
            }
            case 4:{
                navController.navigate(R.id.action_profileSettingsFragment_to_changePasswordFragment);
                break;
            }
            case 5:
            case 6:{
                final Bundle bundle = new Bundle();
                bundle.putInt(Constants.BUNDLE_APP_POLICIES,settingType);
                navController.navigate(R.id.action_profileSettingsFragment_to_appPoliciesFragment,bundle);
            }
        }
    }


    // logging out from current session
    private void logoutFromSession(){
        if (ParseUser.getCurrentUser() != null){
            ParseUser.logOutInBackground(e -> {
                enableDisableBtn(true);
                FeedsArgsViewModel feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);
                feedsArgsViewModel.clearAllArgs();
                sessionErrorViewModel.isSessionExpired(true);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        profileSettingsToolbar = null;
        accountSettingsRecyclerView = null;
        logoutBtn = null;
        deleteAccountBtn = null;
    }

}