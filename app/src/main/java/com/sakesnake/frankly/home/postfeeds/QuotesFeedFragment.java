package com.sakesnake.frankly.home.postfeeds;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;

// This fragment is replaced by PostFeedsFragment do not use this.
@Deprecated
public class QuotesFeedFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quotes_feed, container, false);
    }
}