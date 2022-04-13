package com.sakesnake.frankly.home.bottomnavupload;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.WritingUploadViewModel;

import java.util.ArrayList;

public class BottomUploadFragment extends Fragment implements UploadListAdapter.SelectWhatToUpload
{
    private View rootView;

    private RecyclerView bottomUploadRecyclerView;

    private ArrayList<UploadList> uploadListArrayList = new ArrayList<>();

    private Context mContext;

    private UploadListAdapter uploadListAdapter;

    private NavController navController;

    private MultiImageViewModel multiImageViewModel;

    private SessionErrorViewModel sessionErrorViewModel;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // getting view model
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        rootView = inflater.inflate(R.layout.fragment_bottom_upload_v2, container, false);
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);


        // Unhiding bottom navigation view if hidden
        sessionErrorViewModel.setIsToHideBottomNavView(false);

        //filling arrayList
        uploadListArrayList.add(new UploadList(getString(R.string.post_an_image_upload_fragment),mContext.getColor(R.color.yellow_ideas),0));                      // 0
        uploadListArrayList.add(new UploadList(getString(R.string.write_a_blog_upload_fragment),mContext.getColor(R.color.orange_memes_and_comedy),1));             // 1
        uploadListArrayList.add(new UploadList(getString(R.string.saw_a_dream_today_upload_fragment),mContext.getColor(R.color.black),2));                          // 2
        uploadListArrayList.add(new UploadList(getString(R.string.tell_quotes_and_thoughts_upload_fragment),mContext.getColor(R.color.blue_thoughts_and_quotes),3));  // 3
        uploadListArrayList.add(new UploadList(getString(R.string.my_news_and_updates_my_uploads),mContext.getColor(R.color.green_motivate_yourself),4));           // 4
        uploadListArrayList.add(new UploadList(getString(R.string.my_memes_and_comedy_my_uploads),mContext.getColor(R.color.orange_memes_and_comedy),5));           // 5
        uploadListArrayList.add(new UploadList(getString(R.string.narrate_a_story_upload_fragment),mContext.getColor(R.color.voilet_story),6));                     // 6
        uploadListArrayList.add(new UploadList(getString(R.string.write_poetry_upload_fragment),mContext.getColor(R.color.pink_poems),7));                          // 7
        uploadListArrayList.add(new UploadList(getString(R.string.ask_a_question_upload_fragment),mContext.getColor(R.color.brown_give_them_answer),9));           // 9
        uploadListArrayList.add(new UploadList(getString(R.string.my_ideas_my_uploads),mContext.getColor(R.color.warning_color),10));                              // 10

        //init views  
        bottomUploadRecyclerView = (RecyclerView) rootView.findViewById(R.id.upload_upload_fragment_recycler_view);

        //filling recycler view
        uploadListAdapter = new UploadListAdapter(mContext,uploadListArrayList,this);
        bottomUploadRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2));
        bottomUploadRecyclerView.setAdapter(uploadListAdapter);

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomUploadRecyclerView = null;
        uploadListArrayList.clear();
        uploadListAdapter = null;
        rootView = null;
    }

    @Override
    public void selectWhatToUploadMethod(int position) {

        sessionErrorViewModel.setIsToHideBottomNavView(true);

        // getting selected position view model
        WritingUploadViewModel viewModel = new ViewModelProvider(requireActivity()).get(WritingUploadViewModel.class);
        viewModel.setPosition(position);

        if (navController != null) {
            if (position <= 14) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.BUNDLE_CLEAR_ATTACHED_IMAGES,true);

                if (position == 0  ||  position == 5)
                    navController.navigate(R.id.action_bottomUploadFragment_to_mobileImagesFragment,bundle);

                else {
                    multiImageViewModel.setPreSelectedImages(new ArrayList<>());
                    navController.navigate(R.id.action_bottomUploadFragment_to_writingUploadFragment);
                }
            }
        }
    }
}