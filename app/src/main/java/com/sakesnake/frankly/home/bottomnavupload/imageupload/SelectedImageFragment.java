package com.sakesnake.frankly.home.bottomnavupload.imageupload;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavupload.ConvertUriToByteArray;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersAdapter;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersRecyclerAdapter;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.WritingUploadViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedImageFragment extends Fragment implements ConnectedUsersAdapter.GetConnectedUser,
        ConnectedUsersRecyclerAdapter.RemoveConnectedUser{
    private View rootView;
    private Toolbar selectedImageToolbar;
    private NavController controller;
    private ImageView selectedImage;
    private EditText contentDescription,contentLocation;
    private AutoCompleteTextView connectUser;
    private Button uploadContentBtn,previewOfPostBtn;
    private RecyclerView connectedUsersRecyclerView;
    private TextView connectUserMessage;
    private ProgressBar loadingUsersProgressBar,uploadingProgressBar;
    private Context mContext;
    private WritingUploadViewModel writingUploadViewModel;
    private ConnectedUsersViewModel connectedUsersViewModel;
    private SessionErrorViewModel sessionErrorViewModel;
    private NavController navController;
    private String imageUri;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_selected_image, container, false);

        // getting navigation controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // getting writing upload, connected users, multi image view model and session error view model
        writingUploadViewModel = new ViewModelProvider(requireActivity()).get(WritingUploadViewModel.class);
        connectedUsersViewModel = new ViewModelProvider(requireActivity()).get(ConnectedUsersViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        //getting controller
        controller = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        //init views
        selectedImageToolbar = (Toolbar) rootView.findViewById(R.id.selected_image_fragment_toolbar);
        selectedImage = (ImageView) rootView.findViewById(R.id.selected_image_fragment_image_view);
        contentDescription = (EditText) rootView.findViewById(R.id.selected_image_fragment_description_edit_text);
        contentLocation = (EditText) rootView.findViewById(R.id.selected_image_fragment_content_location_edit_text);
        connectUser = (AutoCompleteTextView) rootView.findViewById(R.id.selected_fragment_upload_get_connected_username_edit_text);
        loadingUsersProgressBar = (ProgressBar) rootView.findViewById(R.id.selected_image_fragment_progress_bar);
        uploadingProgressBar = (ProgressBar) rootView.findViewById(R.id.selected_image_fragment_uploading_to_parse_progress_bar);
        uploadContentBtn = (Button) rootView.findViewById(R.id.selected_image_fragment_upload_to_parse_button);
        previewOfPostBtn = (Button) rootView.findViewById(R.id.image_memes_upload_show_post_preview_btn);
        connectedUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.selected_image_fragment_connected_users_recycler_view);
        connectUserMessage = (TextView) rootView.findViewById(R.id.selected_fragment_connect_user_message_text_view);


        if (getArguments() != null){
            if (getArguments().containsKey(Constants.BUNDLE_SELECTED_IMAGE_URI_KEY)){
                imageUri = getArguments().getString(Constants.BUNDLE_SELECTED_IMAGE_URI_KEY);
            }
        }
        else if (savedInstanceState != null){
            if (savedInstanceState.containsKey(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY)){
                imageUri = savedInstanceState.getString(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY);
                savedInstanceState.clear();
            }
        }

        if (imageUri != null)
            Glide.with(mContext).load(Uri.parse(imageUri)).fitCenter().into(selectedImage);

        connectedUsersViewModel.getConnectedUsers().observe(getViewLifecycleOwner(), this::setConnectedUsers);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectedImageToolbar.setNavigationOnClickListener(v -> controller.navigateUp());

        connectUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadingUsersProgressBar.setVisibility(View.VISIBLE);
                getUsersForConnections(editable.toString());
            }
        });

        // Upload content to parse btn
        uploadContentBtn.setOnClickListener(view1 -> {
            if (getArguments() != null) {
                uploadContentBtn.setEnabled(false);
                ObjectAnimator animator = ObjectAnimator.ofFloat(uploadContentBtn,"scaleX",1.0f,0f);
                animator.start();
                uploadingProgressBar.setVisibility(View.VISIBLE);
                uploadImageOrMemes();
            }
        });

        previewOfPostBtn.setOnClickListener(view1 -> {
            if (imageUri != null){
                String description = DocNormalizer.spannedToHtml(contentDescription.getText());
                String location = contentLocation.getText().toString().trim();

                Bundle bundle = new Bundle();
                bundle.putString(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,imageUri);
                bundle.putString(HashMapKeys.CONTENT_DESCRIPTION_KEY,description);
                bundle.putString(HashMapKeys.CONTENT_LOCATION_KEY,location);
                bundle.putInt(Constants.BUNDLE_SELECTED_TYPE_FOR_UPLOAD_KEY,writingUploadViewModel.getSelectedPosition().getValue());
                navController.navigate(R.id.action_selectedImageFragment_to_postPreviewFragment,bundle);
            }

        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null)
            outState.putString(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,imageUri);
    }

    private void setConnectedUsers(List<ParseUser> parseUsers){
        ConnectedUsersRecyclerAdapter adapter = new ConnectedUsersRecyclerAdapter(mContext,parseUsers, this);
        connectedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        connectedUsersRecyclerView.setAdapter(adapter);
    }


    // Getting username from the database to display on auto complete edit text
    private void getUsersForConnections(String username) {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.SEARCHED_USERNAME_KEY,username);
        ParseCloud.callFunctionInBackground("searchUser",hashMap,(FunctionCallback<Map<String,Object>>) (object, e)->{
            if (e == null) {
                List<ParseUser> parseUsers = (List<ParseUser>) object.get("result");
                if (parseUsers != null) {
                    if (parseUsers.size() > 0) {
                        List<String> stringList = new ArrayList<>();
                        for (int i = 0; i < parseUsers.size(); i++) {
                            stringList.add(parseUsers.get(i).getUsername());
                        }
                        ConnectedUsersAdapter adapter = new ConnectedUsersAdapter(mContext, stringList, parseUsers, this);
                        connectUser.setAdapter(adapter);
                    }
                }
            }
            if (loadingUsersProgressBar != null)
                loadingUsersProgressBar.setVisibility(View.GONE);
        });
    }

    // the user is selected using Connected user adapter for connection
    @Override
    public void getConnectedUser(ParseUser userObject) {
        connectUser.setText("");
        connectedUsersViewModel.connectUser(userObject);
        loadingUsersProgressBar.setVisibility(View.GONE);
        connectUserMessage.setVisibility(View.GONE);
    }




    private void uploadImageOrMemes(){
        HashMap<String,Object> hashMap = new HashMap<>();
        Uri imageUri = Uri.parse(getArguments().getString(Constants.BUNDLE_SELECTED_IMAGE_URI_KEY));
        ConvertUriToByteArray.getByteArrayFromImage(imageUri,imageByte -> {
            String description,location;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                description = Html.toHtml(contentDescription.getText(), Html.FROM_HTML_MODE_LEGACY);
            else
                description = Html.toHtml(contentDescription.getText());

            location = contentLocation.getText().toString().trim();

            hashMap.put(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,imageByte);
            hashMap.put(HashMapKeys.CONTENT_DESCRIPTION_KEY,description+"");
            hashMap.put(HashMapKeys.CONTENT_LOCATION_KEY,location+"");
            hashMap.put(HashMapKeys.UPLOAD_CONTENT_TYPE_KEY,writingUploadViewModel.getSelectedPosition().getValue());

            List<String> username = new ArrayList<>();
            for (int i=0; i<connectedUsersViewModel.getConnectedUser().size(); i++){
                username.add(connectedUsersViewModel.getConnectedUser().get(i).getUsername());
            }
            hashMap.put(HashMapKeys.CONNECTED_USERS_KEY,username);

            ParseCloud.callFunctionInBackground("uploadContentToParse",hashMap,(object, e) -> {
                if (e == null) {
                    // Successfully content uploaded to parse server
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(()->{
                        navController.navigate(R.id.action_selectedImageFragment_to_bottomHomeFragment);
                    },2000);
                    sessionErrorViewModel.setIsToHideBottomNavView(false);
                    loadingUsersProgressBar.setVisibility(View.GONE);
                    Snackbar.make(mContext,uploadContentBtn,"Successfully uploaded your content.",1500)
                            .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(mContext,R.color.upload_logo_color))
                            .show();
                }
                else {
                    if (e.getCode() == 209){
                        // User Session has been deleted by another user or by host
                        Toast.makeText(mContext, getString(R.string.session_expired_message), Toast.LENGTH_LONG).show();
                        sessionErrorViewModel.isSessionExpired(true);
                        return;
                    }

                    // Failed to upload content to parse server
                    uploadingProgressBar.setVisibility(View.GONE);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(uploadContentBtn,"scaleX",0f,1.0f);
                    animator.start();
                    uploadContentBtn.setEnabled(true);
                    Snackbar.make(mContext,uploadContentBtn,"Uff! Something went wrong please try again.",1500)
                            .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people))
                            .show();
                }
            });
        },mContext.getApplicationContext());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        selectedImageToolbar = null;
        rootView = null;
        selectedImage = null;
        connectUserMessage = null;
        connectedUsersRecyclerView = null;
        contentDescription = null;
        contentLocation = null;
        uploadContentBtn = null;
        connectUser = null;
        loadingUsersProgressBar = null;
        uploadingProgressBar = null;
        previewOfPostBtn = null;
    }

    @Override
    public void removeConnectedUser(int position) {
        connectedUsersViewModel.removeUser(position);
        if (connectedUsersViewModel.getConnectedUser().size() == 0)
            connectUserMessage.setVisibility(View.VISIBLE);
        else
            connectUserMessage.setVisibility(View.GONE);
    }
}