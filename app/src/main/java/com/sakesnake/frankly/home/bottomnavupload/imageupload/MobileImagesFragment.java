package com.sakesnake.frankly.home.bottomnavupload.imageupload;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavupload.DeviceImages;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.permissionmessage.StorageMessageFragment;
import com.sakesnake.frankly.permissionmessage.StoragePermissionViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MobileImagesFragment extends Fragment implements TappedImageCallback{

    private View rootView;

    private RecyclerView deviceImagesRecyclerView;

    private Context mContext;

    private ProgressBar loadingDeviceImagesProgressBar;

    private ImageView noImageAvailable;

    private FloatingActionButton imageSelectedFloatingBtn;

    private TextView noImageAvailableTextView, selectMultiImageMsgTextView;

    private Toolbar mobileFragmentToolbar;

    private NavController navController;

    private StoragePermissionViewModel viewModel;

    private MultiImageViewModel multiImageViewModel;

    private final List<Uri> selectedImage = new ArrayList<>();

    private static int position = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // getting multi image view model
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);

        rootView = inflater.inflate(R.layout.fragment_mobile_images, container, false);


        // Getting view model
        viewModel = new ViewModelProvider(requireActivity()).get(StoragePermissionViewModel.class);

        //Restoring the fragment saved state
        if (savedInstanceState != null) {
            if (savedInstanceState.getInt(Constants.BUNDLE_SCROLLING_POSITION) != 0)
                position = savedInstanceState.getInt(Constants.BUNDLE_SCROLLING_POSITION);
        }


        // getting nav controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // init views
        deviceImagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.device_images_recycler_view);
        loadingDeviceImagesProgressBar = (ProgressBar) rootView.findViewById(R.id.loading_device_images_progress_bar);
        noImageAvailable = (ImageView) rootView.findViewById(R.id.no_images_to_show_icon_image_view);
        noImageAvailableTextView = (TextView) rootView.findViewById(R.id.no_images_message_text_view);
        mobileFragmentToolbar = (Toolbar) rootView.findViewById(R.id.mobile_images_fragment_toolbar);
        selectMultiImageMsgTextView = (TextView) rootView.findViewById(R.id.select_multiple_image_message_text_view);
        imageSelectedFloatingBtn = (FloatingActionButton) rootView.findViewById(R.id.done_selecting_image_floating_btn);


        if (getArguments() != null){
            if (getArguments().getBoolean(Constants.BUNDLE_FROM_WRITING_UPLOAD_SELECT_MULTI,false)){
                selectMultiImageMsgTextView.setVisibility(View.VISIBLE);
            }
        }

        //checking storage permission to get device images
        checkIsPermissionGranted();

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mobileFragmentToolbar.setNavigationOnClickListener(v ->
        {
            navController.navigateUp();
        });

        // Done with image selection now navigate back to writing upload fragment
        imageSelectedFloatingBtn.setOnClickListener(view1 -> {
            multiImageViewModel.setPreSelectedImages(selectedImage);
            navController.navigateUp();
        });
    }

    private void checkIsPermissionGranted() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            showInfoThenRequest();
        } else {
            permissionIsGranted();
        }
    }

    private void showInfoThenRequest() {
        StorageMessageFragment dialogFragment = new StorageMessageFragment();
        dialogFragment.show(getChildFragmentManager(), "Fragment_TAG");

        viewModel.getStoragePermission().observe(getViewLifecycleOwner(), aBoolean ->
        {
            if (aBoolean) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                noImageAvailable.setVisibility(View.GONE);
                noImageAvailableTextView.setVisibility(View.GONE);
            } else {
                noImageAvailable.setVisibility(View.VISIBLE);
                noImageAvailableTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionIsGranted();
            } else {
                viewModel.setStoragePermission(false);
                noImageAvailable.setVisibility(View.VISIBLE);
                noImageAvailableTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void permissionIsGranted() {
        getDeviceImages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deviceImagesRecyclerView = null;
        noImageAvailableTextView = null;
        loadingDeviceImagesProgressBar = null;
        selectMultiImageMsgTextView = null;
        noImageAvailable = null;
        mobileFragmentToolbar = null;
        rootView = null;
        imageSelectedFloatingBtn = null;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.BUNDLE_SCROLLING_POSITION, position);
    }


    // getting device images using executors background work
    private void getDeviceImages() {
        loadingDeviceImagesProgressBar.setVisibility(View.VISIBLE);
//        App.getCachedExecutorService().execute(this);

        DeviceImages.getMobileImages(mContext.getApplicationContext(), deviceImages -> {
            if (deviceImages.size() > 0){
                MobileImagesAdapter adapter = new MobileImagesAdapter(mContext,deviceImages,multiImageViewModel.getPreSelectedImages() ,this);
                deviceImagesRecyclerView.setAdapter(adapter);
                deviceImagesRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));
                deviceImagesRecyclerView.getLayoutManager().scrollToPosition(position);
                position = 0;
            }else{
                noImageAvailable.setVisibility(View.VISIBLE);
                noImageAvailableTextView.setVisibility(View.VISIBLE);
            }
            loadingDeviceImagesProgressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void tappedImage(int position,List<Uri> preSelectedImages) {
        if (getArguments() != null){
            if (getArguments().getBoolean(Constants.BUNDLE_CALLED_FROM_WRITING_UPLOAD,false)){
                selectedImage.clear();
                selectedImage.addAll(preSelectedImages);
                multiImageViewModel.setPreSelectedImages(selectedImage);
                navController.navigateUp();
                return;
            }
            else if (getArguments().getBoolean(Constants.BUNDLE_FROM_WRITING_UPLOAD_SELECT_MULTI)){
                selectedImage.clear();
                selectedImage.addAll(preSelectedImages);
                if (selectedImage.size() == 0){
                    mobileFragmentToolbar.setTitle(getString(R.string.pick_an_image));
                    imageSelectedFloatingBtn.setVisibility(View.GONE);
                }else{
                    mobileFragmentToolbar.setTitle(selectedImage.size()+"");
                    imageSelectedFloatingBtn.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
        Uri uri = preSelectedImages.get(0);
        selectedImage.clear();
        multiImageViewModel.setPreSelectedImages(selectedImage);
        MobileImagesFragment.position = position;
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_SELECTED_IMAGE_URI_KEY,uri.toString());
        navController.navigate(R.id.action_mobileImagesFragment_to_selectedImageFragment,bundle);
    }


    // executor runnable interface
//    @Override
//    public void run() {
//        ArrayList<MobileImages> mobileImages = DeviceImages.getDeviceImages(mContext);
//        App.getMainThread().post(() -> {
//            if (mobileImages != null) {
//                MobileImagesAdapter adapter = new MobileImagesAdapter(mContext, mobileImages, this);
//                deviceImagesRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
//                deviceImagesRecyclerView.setAdapter(adapter);
//                deviceImagesRecyclerView.getLayoutManager().scrollToPosition(position);
//                position = 0;
//            } else {
//                noImageAvailable.setVisibility(View.VISIBLE);
//                noImageAvailableTextView.setVisibility(View.VISIBLE);
//            }
//            loadingDeviceImagesProgressBar.setVisibility(View.GONE);
//        });
//    }

}