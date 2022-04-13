package com.sakesnake.frankly.home.bottomnavupload.writingupload.poempostpreview;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;

public class PoemPreviewFragment extends DialogFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_poem_preview, container, false);
    }
}