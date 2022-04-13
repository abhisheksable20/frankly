package com.sakesnake.frankly.home.bottomnavupload.multiimageselector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavupload.DeviceImages;
import com.sakesnake.frankly.permissionmessage.StorageMessageFragment;
import com.sakesnake.frankly.permissionmessage.StoragePermissionViewModel;

import java.util.List;

public class MultiImageFragment extends Fragment implements PreImageSelectionCallback{
    private View rootView;
    private RecyclerView multiImageRecyclerView;
    private Toolbar multiImageToolbar;
    private ProgressBar multiImageProgressBar;
    private ImageView noImagesAvailableImageView;
    private TextView noImagesAvailableTextView, multiFragmentImageSelectionMessageTextView;
    private Context mContext;
    private StoragePermissionViewModel viewModel;
    private NavController navController;
    private MultiImageViewModel multiImageViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_multi_image, container, false);


        // Getting storage permission view model
        viewModel = new ViewModelProvider(requireActivity()).get(StoragePermissionViewModel.class);

        // Getting multi image view model
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);

        // Getting navigation controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        //init views
        multiImageRecyclerView = (RecyclerView) rootView.findViewById(R.id.multi_image_fragment_recycler_view);
        multiFragmentImageSelectionMessageTextView = (TextView) rootView.findViewById(R.id.multi_fragment_image_selection_message_text_view);

        multiImageToolbar = (Toolbar) rootView.findViewById(R.id.multi_image_selector_toolbar);

        multiImageProgressBar = (ProgressBar) rootView.findViewById(R.id.multi_image_fragment_progress_bar);
        noImagesAvailableImageView = (ImageView) rootView.findViewById(R.id.multi_image_fragment_no_images_image_view);
        noImagesAvailableTextView = (TextView) rootView.findViewById(R.id.multi_image_fragment_no_image_text_view);


        if (multiImageViewModel.getPreSelectedImages().size() > 0)
            multiImageToolbar.setTitle(multiImageViewModel.getPreSelectedImages().size()+"");

        checkIsPermissionGranted();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        multiImageToolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });

        // Toolbar menu item click listener
        multiImageToolbar.setOnMenuItemClickListener(item ->
        {
            if (item.getItemId() == R.id.done_multi_image_toolbar_menu_item) {
                navController.navigateUp();
                return true;
            }
            return false;
        });
    }

    private void checkIsPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            showInfoThenProceed();
        } else {
            permissionIsGranted();
        }
    }

    private void permissionIsGranted() {
        getDeviceImages();
    }

    private void showInfoThenProceed() {
        StorageMessageFragment fragment = new StorageMessageFragment();
        fragment.show(getChildFragmentManager(), "Multi_image_fragment_tag");

        viewModel.getStoragePermission().observe(getViewLifecycleOwner(), aBoolean ->
        {
            if (aBoolean) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                noImagesAvailableTextView.setVisibility(View.GONE);
                noImagesAvailableImageView.setVisibility(View.GONE);
            } else {
                noImagesAvailableTextView.setVisibility(View.VISIBLE);
                noImagesAvailableImageView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionIsGranted();
            } else {
                viewModel.setStoragePermission(false);
                noImagesAvailableTextView.setVisibility(View.VISIBLE);
                noImagesAvailableImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void selectedImages(List<Uri> selectedImages) {
        if (selectedImages.size() > 0){
            multiImageToolbar.setTitle(selectedImages.size()+"");
            multiImageViewModel.setPreSelectedImages(selectedImages);
        }else{
            multiImageToolbar.setTitle(getString(R.string.pick_an_image));
        }
    }

    // getting device images using executor background work
    private void getDeviceImages() {
        multiImageProgressBar.setVisibility(View.VISIBLE);
        DeviceImages.getMobileImages(mContext.getApplicationContext(), deviceImages -> {
            multiImageProgressBar.setVisibility(View.GONE);
            if(deviceImages.size() == 0){
                noImagesAvailableImageView.setVisibility(View.VISIBLE);
                noImagesAvailableTextView.setVisibility(View.VISIBLE);
                return;
            }

            MultiImagesAdapter adapter = new MultiImagesAdapter(mContext,
                    deviceImages,
                    multiImageViewModel.getPreSelectedImages(),
                    MultiImageFragment.this);

            multiImageRecyclerView.setHasFixedSize(true);
            multiImageRecyclerView.setItemViewCacheSize(20);
            multiImageRecyclerView.setAdapter(adapter);
            multiImageRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        multiImageRecyclerView = null;
        multiImageToolbar = null;
        multiImageProgressBar = null;
        noImagesAvailableImageView = null;
        noImagesAvailableTextView = null;
        rootView = null;
    }
}