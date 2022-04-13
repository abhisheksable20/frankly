package com.sakesnake.frankly.home.bottomnavsearch.smartsearch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;

public class SmartSearchFragment extends Fragment
{
    private Toolbar smartFragmentToolbar;
    private View rootView;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_smart_search, container, false);

        toggleBottomNavVisibility(0);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init views
        smartFragmentToolbar = (Toolbar) rootView.findViewById(R.id.smart_search_fragment_toolbar);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        smartFragmentToolbar.setNavigationOnClickListener(v ->
        {
            navController.navigateUp();
        });
    }

    /*
        0:- Gone
        1:-UnHide
     */
    private void toggleBottomNavVisibility(int hideOrUnHide) {
        try {
            NavHostFragment fragment = (NavHostFragment) getParentFragment();
            Fragment parentFragment = (Fragment) fragment.getParentFragment();
            if (hideOrUnHide == 0)
                parentFragment.getView().findViewById(R.id.nav_view).setVisibility(View.GONE);
            else
                parentFragment.getView().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        toggleBottomNavVisibility(1);
        smartFragmentToolbar = null;
    }
}