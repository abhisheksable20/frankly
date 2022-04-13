package com.sakesnake.frankly.home.bottomnavsearch;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.FeedsArgsViewModel;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class BottomSearchFragment extends Fragment implements SearchListRecyclerAdapter.SetSearchedSelectedPos {

    private View rootView;

    private RecyclerView searchFragmentRecyclerView;

    private final ArrayList<SearchList> searchListArrayList = new ArrayList<>();

    private Context mContext;

    private SearchListRecyclerAdapter searchListRecyclerAdapter;

    private MaterialCardView searchFragmentCardView;

    private NavController navController;

    private FeedsArgsViewModel feedsArgsViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bottom_search_v2, container, false);

        // init view models
        feedsArgsViewModel = new ViewModelProvider(requireActivity()).get(FeedsArgsViewModel.class);

        // Getting navigation controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // filling arraylist
        searchListArrayList.add(new SearchList(getString(R.string.my_images_my_uploads), mContext.getColor(R.color.yellow_ideas),0)); // 0
        searchListArrayList.add(new SearchList(getString(R.string.search_frag_peoples_blog), mContext.getColor(R.color.orange_memes_and_comedy),1)); // 1
        searchListArrayList.add(new SearchList(getString(R.string.my_dreams_my_uploads), mContext.getColor(R.color.black),2)); // 2
        searchListArrayList.add(new SearchList(getString(R.string.search_frag_quotes_and_thoughts), mContext.getColor(R.color.blue_thoughts_and_quotes),3)); // 3
        searchListArrayList.add(new SearchList(getString(R.string.search_frag_news_and_updates), mContext.getColor(R.color.green_motivate_yourself),4)); // 4
        searchListArrayList.add(new SearchList(getString(R.string.search_frag_memes_and_comedy), mContext.getColor(R.color.orange_memes_and_comedy),5)); // 5
        searchListArrayList.add(new SearchList(getString(R.string.my_story_my_uploads), mContext.getColor(R.color.voilet_story),6)); // 6
        searchListArrayList.add(new SearchList(getString(R.string.my_poem_my_uploads), mContext.getColor(R.color.pink_poems),7)); // 7
        searchListArrayList.add(new SearchList(getString(R.string.search_frag_answer_the_question), mContext.getColor(R.color.brown_give_them_answer),9)); // 9
        searchListArrayList.add(new SearchList(getString(R.string.my_ideas_my_uploads), mContext.getColor(R.color.yellow_ideas),10)); // 10

        //init views
        searchFragmentRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_search_fragment);
        searchFragmentCardView = (MaterialCardView) rootView.findViewById(R.id.search_material_card_view_search_fragment);


        // Filling recycler view
        searchListRecyclerAdapter = new SearchListRecyclerAdapter(mContext, searchListArrayList, this);
        searchFragmentRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        searchFragmentRecyclerView.setAdapter(searchListRecyclerAdapter);

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigating to search profile  fragment
        searchFragmentCardView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_SHOW_SOFT_INPUT,true);
            navController.navigate(R.id.action_bottomSearchFragment_to_searchProfileFragment,bundle);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchListArrayList.clear();
        searchFragmentRecyclerView = null;
        searchListRecyclerAdapter = null;
        searchFragmentCardView = null;
        rootView = null;
    }

    @Override
    public void getSearchedSelectedPos(int pos) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_CONTENT_TYPE,pos);
        bundle.putInt(Constants.BUNDLE_SEARCH_CALL,Constants.SEARCH_CALL);
        feedsArgsViewModel.addFragArg(bundle);
        switch (pos){
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:
            case Constants.UPLOAD_MEMES_AND_COMEDY:
            case Constants.UPLOAD_IMAGE:{
                navController.navigate(R.id.action_bottomSearchFragment_to_imagesFeedFragment,bundle);
                break;
            }
            case Constants.UPLOAD_POEM:
            case Constants.UPLOAD_STORY:
            case Constants.UPLOAD_GIVE_QUESTION:
            case Constants.UPLOAD_IDEAS:
            case Constants.UPLOAD_DREAM:
            case Constants.UPLOAD_NEWS_AND_UPDATE:
            case Constants.UPLOAD_BLOG:{
                navController.navigate(R.id.action_bottomSearchFragment_to_writingFeedsFragment,bundle);
                break;
            }
        }
    }
}