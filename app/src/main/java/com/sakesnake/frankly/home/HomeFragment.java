package com.sakesnake.frankly.home;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {
    private View rootView;
    private BottomNavigationView homeBottomNavView;
    private NavController homeNavController;
    private SessionErrorViewModel sessionErrorViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // init view model
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        //init views
        homeBottomNavView = (BottomNavigationView) rootView.findViewById(R.id.nav_view);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeNavController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);
        NavigationUI.setupWithNavController(homeBottomNavView, homeNavController);


        // Observing IsToHideBottomNavView value
        sessionErrorViewModel.getIsToHideBottomNavView().observe(getViewLifecycleOwner(), hide -> {
            if (homeBottomNavView != null){
                if (hide)
                    homeBottomNavView.setVisibility(View.GONE);
                else
                    homeBottomNavView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        homeBottomNavView = null;
        rootView = null;
    }
}