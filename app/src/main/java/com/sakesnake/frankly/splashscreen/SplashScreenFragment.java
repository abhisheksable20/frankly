package com.sakesnake.frankly.splashscreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;
import com.parse.ParseUser;

public class SplashScreenFragment extends Fragment implements Runnable {
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        handler = new Handler();
        handler.postDelayed(this, 1000);
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void run() {
        NavController splashScreenController = Navigation.findNavController(getActivity(), R.id.main_app_fragment);
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().getString("originalName") == null){
                splashScreenController.navigate(R.id.action_splashScreenFragment_to_signUpGetNameFragment);
            }else{
                splashScreenController.navigate(R.id.action_splashScreenFragment_to_homeFragment);
            }
        } else {
            splashScreenController.navigate(R.id.action_splashScreenFragment_to_signUpScreenFragment);
        }
    }
}