package com.sakesnake.frankly.home.bottomnavupload.functioninvitation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;


public class FunctionInvitationFragment extends Fragment
{
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // init root view
        rootView = inflater.inflate(R.layout.fragment_function_invitation, container, false);

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}