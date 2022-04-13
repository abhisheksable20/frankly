package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.App;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.bottomnavupload.ConvertUriToByteArray;
import com.sakesnake.frankly.home.bottomnavupload.UriConvertCallback;
import com.sakesnake.frankly.home.bottomnavupload.UriListConvertCallback;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditProfileFragment extends Fragment implements UriListConvertCallback,UriConvertCallback, Runnable {

    private ImageView profileBannerImageView, profilePicImageView;

    private TextView editProfilePicTextView, defaultProfilePicTextView, removeProfilePicTextView,
            editBannerTextView, defaultBannerTextView, removeBannerTextView;

    private EditText editProfileNameEditText, editProfileDescriptionEditText;

    private View rootView;

    private Context mContext;

    private NavController navController;

    private MultiImageViewModel multiImageViewModel;

    private Button saveProfileBtn;

    public static final int MODIFY_PROFILE_BANNER = 100;

    public static final int MODIFY_PROFILE_PHOTO = 101;

    private Uri bannerImgUri, profilePicImageUri;

    private ParseUser currentUser;

    private boolean bannerRemoved, profilePicRemoved = false;

    private boolean waitForOther = false;

    private ParseFile profilePic = null;

    private ParseFile profileBannerPic = null;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // init view models
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);

        // init views
        initViews();

        if (currentUser == null)
            fetchLatestUserData();

        // Observing preselected images uri
        multiImageViewModel.observePreSelectedImages().observe(getViewLifecycleOwner(), (imageUri) -> {
            if (imageUri != null && imageUri.size() > 0){
                int modifyIntFlag = multiImageViewModel.getModifyFlag();

                if (modifyIntFlag == MODIFY_PROFILE_BANNER)
                    bannerImgUri = imageUri.get(0);

                else if (modifyIntFlag == MODIFY_PROFILE_PHOTO)
                    profilePicImageUri = imageUri.get(0);

                multiImageViewModel.clearPreSelectedImages();
            }

            setImageViews();
        });

        return rootView;
    }

    private void setImageViews(){
        if (bannerImgUri != null) {
            bannerRemoved = false;
            Glide.with(mContext).load(bannerImgUri).into(profileBannerImageView);
        }
        else if (!bannerRemoved)
            setBannerImage();

        if (profilePicImageUri != null) {
            profilePicRemoved = false;
            Glide.with(mContext).load(profilePicImageUri).into(profilePicImageView);
        }
        else if (!profilePicRemoved)
            setProfilePic();

    }

    private void fetchLatestUserData(){
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null)
            return;

        parseUser.fetchInBackground((object, e) -> {
            if (e == null) {
                currentUser = (ParseUser) object;
                setDefaultViews(currentUser);
            }
            else
                Log.e(Constants.TAG, "Failed to retrieve latest data ERROR ------> "+e.getMessage());
        });
    }

    private void setDefaultViews(final ParseUser parseUser){

        setBannerImage();

        setProfilePic();

        final String profileDescription = parseUser.getString(ParseConstants.PROFILE_DESCRIPTION);
        if (profileDescription != null)
            editProfileDescriptionEditText.setText(DocNormalizer.htmlToNormal(profileDescription));

        editProfileNameEditText.setText(parseUser.getString(ParseConstants.PROFILE_ORIGINAL_NAME));
    }

    // Setting profile banner image view
    private void setBannerImage(){
        if (currentUser == null)
            return;

        bannerRemoved = false;

        ParseFile profileBanner = currentUser.getParseFile(ParseConstants.PROFILE_BANNER);
        if (profileBanner != null)
            Glide.with(mContext).load(profileBanner.getUrl()).into(profileBannerImageView);
    }

    // Setting profile pic image view
    private void setProfilePic(){
        if (currentUser == null)
            return;

        profilePicRemoved = false;

        ParseFile profilePic = currentUser.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null)
            Glide.with(mContext).load(profilePic.getUrl()).into(profilePicImageView);
    }


    // initializing views
    private void initViews(){
        profileBannerImageView = (ImageView) rootView.findViewById(R.id.edit_profile_fragment_banner_image_view);
        profilePicImageView = (ImageView) rootView.findViewById(R.id.edit_profile_fragment_profile_photo_image_view);
        editProfilePicTextView = (TextView) rootView.findViewById(R.id.edit_profile_fragment_modify_profile_photo_text_view);
        defaultProfilePicTextView = (TextView) rootView.findViewById(R.id.default_profile_pic_text_view);
        removeProfilePicTextView = (TextView) rootView.findViewById(R.id.remove_profile_pic_text_view);
        editBannerTextView = (TextView) rootView.findViewById(R.id.edit_profile_fragment_modify_banner_text_view);
        defaultBannerTextView = (TextView) rootView.findViewById(R.id.default_profile_banner_text_view);
        removeBannerTextView = (TextView) rootView.findViewById(R.id.remove_profile_banner_text_view);
        saveProfileBtn = (Button) rootView.findViewById(R.id.save_profile_btn);
        editProfileNameEditText = (EditText) rootView.findViewById(R.id.edit_profile_fragment_change_name_edit_text);
        editProfileDescriptionEditText = (EditText) rootView.findViewById(R.id.edit_profile_description_edit_text);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // navigating to select banner image
        editBannerTextView.setOnClickListener(v -> {
            multiImageViewModel.setModifyIntFlag(EditProfileFragment.MODIFY_PROFILE_BANNER);
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_CALLED_FROM_WRITING_UPLOAD,true);
            navController.navigate(R.id.action_editProfileFragment_to_mobileImagesFragment,bundle);
        });

        // navigating to select profile image
        editProfilePicTextView.setOnClickListener(view1 -> {
            multiImageViewModel.setModifyIntFlag(EditProfileFragment.MODIFY_PROFILE_PHOTO);
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_CALLED_FROM_WRITING_UPLOAD,true);
            navController.navigate(R.id.action_editProfileFragment_to_mobileImagesFragment,bundle);
        });

        // Bring banner pic to default
        defaultBannerTextView.setOnClickListener(view1 -> {
            bannerImgUri = null;
            bannerRemoved = false;
            setBannerImage();
        });
        // Bring profile pic to default
        defaultProfilePicTextView.setOnClickListener(view1 -> {
            bannerImgUri = null;
            profilePicRemoved = false;
            setProfilePic();
        });

        // Remove banner pic
        removeBannerTextView.setOnClickListener(view1 -> {
            bannerRemoved = true;
            bannerImgUri = null;
            Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.no_banner_avail)).into(profileBannerImageView);
        });

        // Remove profile pic
        removeProfilePicTextView.setOnClickListener(view1 -> {
            profilePicRemoved = true;
            profilePicImageUri = null;
            Glide.with(mContext).load(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon)).into(profilePicImageView);
        });

        saveProfileBtn.setOnClickListener(view1 -> {
            enableDisableSaveBtn(false);
            initProfileSaver();
        });

    }

    // Enabling or disabling the profile save btn
    private void enableDisableSaveBtn(final boolean enableBtn){
        if (enableBtn){
            saveProfileBtn.setEnabled(true);
            saveProfileBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.upload_logo_color)));
            saveProfileBtn.setText(getString(R.string.account_settings_save));
        }
        else{
            saveProfileBtn.setEnabled(false);
            saveProfileBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext,R.color.light_upload_color)));
            saveProfileBtn.setText(getString(R.string.saving_changes));
        }
    }

    // initializing state of profile saver
    private void initProfileSaver(){
        if (profilePicImageUri != null  &&  bannerImgUri != null){
            List<Uri> imagesUri = new ArrayList<>();
            imagesUri.add(profilePicImageUri);
            imagesUri.add(bannerImgUri);
            ConvertUriToByteArray.getByteArrayFromImagesList(imagesUri,this, mContext.getApplicationContext());
        }
        else if (profilePicImageUri != null)
            ConvertUriToByteArray.getByteArrayFromImage(profilePicImageUri,this,mContext.getApplicationContext());
        else if (bannerImgUri != null)
            ConvertUriToByteArray.getByteArrayFromImage(bannerImgUri,this,mContext.getApplicationContext());
        else
            saveProfileChanges(null,null);
    }

    // Saving the profile changes
    private void saveProfileChanges(final ParseFile profileFile, final ParseFile profileBannerFile){
        if (currentUser == null)
            return;

        if (profilePic != null)
            currentUser.put(ParseConstants.PROFILE_PIC,profileFile);
        else if (profilePicRemoved)
            currentUser.remove(ParseConstants.PROFILE_PIC);

        if (profileBannerPic != null)
            currentUser.put(ParseConstants.PROFILE_BANNER,profileBannerFile);
        else if (bannerRemoved)
            currentUser.remove(ParseConstants.PROFILE_BANNER);

        currentUser.put(ParseConstants.PROFILE_ORIGINAL_NAME,editProfileNameEditText.getText().toString().trim());
        currentUser.put(ParseConstants.PROFILE_DESCRIPTION,DocNormalizer.spannedToHtml(editProfileDescriptionEditText.getText()));

        saveToParseServer(currentUser);
    }

    private void saveToParseServer(@NonNull final ParseUser parseUser){
        parseUser.saveInBackground(e -> {
            if (e == null) {
                Toast.makeText(mContext, "Profile changes saved", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
            }
            else {
                Toast.makeText(mContext, "Cannot save profile changes", Toast.LENGTH_SHORT).show();
                Log.e(Constants.TAG, "Error while saving changes: "+e.getMessage());
                enableDisableSaveBtn(true);
            }
        });
    }

    // Saving profile pic file
    private void saveProfilePicFile(final byte[] profilePicBytes){
        Date date = new Date();
        String currentTime = date.getTime()+".jpg";
        ParseFile parseFile = new ParseFile(currentTime,profilePicBytes);
        parseFile.saveInBackground((SaveCallback) e -> {
            if (e == null){
                profilePic = parseFile;
                if (!waitForOther)
                    saveProfileChanges(profilePic,null);
            }
        });
    }

    // Saving Profile banner pic file
    private void saveProfileBannerFile(final byte[] profileBannerBytes){
        Date date = new Date();
        String currentTime = date.getTime()+".jpg";
        ParseFile parseFile = new ParseFile(currentTime,profileBannerBytes);
        parseFile.saveInBackground((SaveCallback) e -> {
            if (e == null){
                profileBannerPic = parseFile;
                if (!waitForOther)
                    saveProfileChanges(null, profileBannerPic);
            }
        });
    }

    @Override
    public void getByteArrayImages(List<byte[]> getBytesImages) {
        // List 1 element will be profile pic
        // List 2 element will be profile banner pic
        if (getBytesImages.size() == 2){
            waitForOther = true;
            App.getCachedExecutorService().execute(this);
            saveProfilePicFile(getBytesImages.get(0));
            saveProfileBannerFile(getBytesImages.get(1));
        }
    }

    @Override
    public void getByteArrayFromImage(byte[] getByteArray) {
        if (profilePicImageUri != null)
            saveProfilePicFile(getByteArray);
        else if (bannerImgUri != null)
            saveProfileBannerFile(getByteArray);
    }

    @Override
    public void run() {
        if (profilePic != null  &&  profileBannerPic != null){
            App.getMainThread().post(() -> {
                saveProfileChanges(profilePic,profileBannerPic);
            });
        }
        else
            App.getCachedExecutorService().execute(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        profileBannerImageView = null;
        profilePicImageView = null;
        editBannerTextView = null;
        defaultBannerTextView = null;
        removeBannerTextView = null;
        editProfilePicTextView = null;
        defaultProfilePicTextView = null;
        removeProfilePicTextView = null;
        saveProfileBtn = null;
        editProfileNameEditText = null;
        editProfileDescriptionEditText = null;
        rootView = null;
    }

}