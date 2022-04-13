package com.sakesnake.frankly.home.bottomnavupload.writingupload;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.SessionErrorViewModel;
import com.sakesnake.frankly.home.bottomnavupload.ConvertUriToByteArray;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersAdapter;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersRecyclerAdapter;
import com.sakesnake.frankly.home.bottomnavupload.connectedusers.ConnectedUsersViewModel;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.betawritter.HtmlFormatViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WritingUploadFragment extends Fragment implements BlogsAndNewsAdapter.RemoveAttachedImage,
        ConnectedUsersAdapter.GetConnectedUser,
        ConnectedUsersRecyclerAdapter.RemoveConnectedUser {

    private View rootView, quotesAndQuestionView, poemAndStoryView, dreamsAndIdeasView, blogAndNewsView;
    private Toolbar writingUploadFragmentToolbar;

    // View models
    private WritingUploadViewModel selectedPositionViewModel;
    private MultiImageViewModel multiImageViewModel;
    private ConnectedUsersViewModel connectedUsersViewModel;
    private HtmlFormatViewModel htmlFormatViewModel;
    private SessionErrorViewModel sessionErrorViewModel;

    private int selectedPosition = -1;
    private Context mContext;
    private NavController navController;
    private ArrayList<Uri> imagesUri = new ArrayList<>();

    // Writing upload fragment views
    private EditText writingUploadGetDescription, writingUploadGetLocation;
    private AutoCompleteTextView writingUploadGetConnectedUsername;
    private Button writingUploadToParseButton, writingUploadShowPostPreviewButton;
    private ProgressBar writingUploadConnectedUserProgressBar,writingUploadContentProgressBar;
    private RecyclerView writingUploadConnectedRecyclerView;
    private TextView writingUploadConnectedMessageTextView;


    // Blogs and news
    private TextView blogsAndNewsTitle, blogsAndNewsContent, blogsAndNewsAttachImages;
    private EditText blogsAndNewsGetTitle, blogsAndNewsGetContent;
    private RecyclerView blogsAndNewsAttachedImagesRecyclerView;
    private TextView blogAndNewsBetaWriter;

    // Poem and story
    private EditText poemAndStoryGetTitle, poemAndStoryGetAuthor, poemAndStoryGetPoemAndStory;
    private TextView poemAndStoryAuthor, poemAndStoryContent, poemUploadImage;
    private ImageView poemUploadedImage;

    // Dreams and ideas
    private TextView dreamAndIdeasTextView, dreamsMessageTextView;
    private EditText dreamsAndIdeasGetDreamsAndIdeas;

    // Quotes and question
    private EditText quotesAndQuestionGetQuotesAndQuestion;
    private TextView quotesAndThoughtsChangeBackground;
    private ImageView quotesAndThoughtsBackgroundImage;

    // connected users array list
    private List<ParseUser> parseUsers = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Getting view models
        selectedPositionViewModel = new ViewModelProvider(requireActivity()).get(WritingUploadViewModel.class);
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);
        connectedUsersViewModel = new ViewModelProvider(requireActivity()).get(ConnectedUsersViewModel.class);
        htmlFormatViewModel = new ViewModelProvider(requireActivity()).get(HtmlFormatViewModel.class);
        sessionErrorViewModel = new ViewModelProvider(requireActivity()).get(SessionErrorViewModel.class);

        rootView = inflater.inflate(R.layout.fragment_writing_upload, container, false);

        // Getting navigation controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // initilizing views
        initializeWritingFragmentViews();

        // Observing position view model and setting toolbar title an layout visibility
        selectedPositionViewModel.getSelectedPosition().observe(getViewLifecycleOwner(), integer ->
        {
            selectedPosition = integer;
            if (integer == 1) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.write_a_blog_upload_fragment));
                blogAndNewsView.setVisibility(View.VISIBLE);
                blogsAndNewsTitle.setText(getString(R.string.poem_title));
                blogsAndNewsGetTitle.setHint(getString(R.string.blog_title_hint));
                blogsAndNewsContent.setText(getString(R.string.blog_content));
                blogsAndNewsGetContent.setHint(getString(R.string.blog_content_hint));
                setAttachedImages();

            } else if (integer == 2) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.saw_a_dream_today_upload_fragment));
                dreamsAndIdeasView.setVisibility(View.VISIBLE);
                dreamAndIdeasTextView.setText(getString(R.string.dream_saw_a_dream));
                dreamsAndIdeasGetDreamsAndIdeas.setHint(getString(R.string.dreams_hint));
                dreamsMessageTextView.setVisibility(View.VISIBLE);
            } else if (integer == 3) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.tell_quotes_and_thoughts_upload_fragment));
                quotesAndQuestionView.setVisibility(View.VISIBLE);
                quotesAndThoughtsBackgroundImage.setVisibility(View.VISIBLE);
                quotesAndThoughtsChangeBackground.setVisibility(View.VISIBLE);
                quotesAndQuestionGetQuotesAndQuestion.setHint(getString(R.string.write_your_quotes_and_thoughts));
                setBackgroundImage(3);

            } else if (integer == 4) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.my_news_and_updates_my_uploads));
                blogAndNewsView.setVisibility(View.VISIBLE);
                blogsAndNewsTitle.setText(getString(R.string.poem_title));
                blogsAndNewsGetTitle.setHint(getString(R.string.news_update_hint_title));
                blogsAndNewsContent.setText(getString(R.string.news_updates_text));
                blogsAndNewsGetContent.setHint(getString(R.string.news_and_updates_hint_goes_here));
                setAttachedImages();

            } else if (integer == 6) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.narrate_a_story_upload_fragment));
                poemAndStoryView.setVisibility(View.VISIBLE);
                poemAndStoryAuthor.setText(getString(R.string.story_author));
                poemAndStoryGetAuthor.setHint(getString(R.string.poem_hint_author));
                poemAndStoryContent.setText(getString(R.string.story_story));
                poemAndStoryGetPoemAndStory.setHint(getString(R.string.story_story_hint));
            } else if (integer == 7) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.write_poetry_upload_fragment));
                poemAndStoryView.setVisibility(View.VISIBLE);
                poemAndStoryAuthor.setText(getString(R.string.poem_author));
                poemAndStoryGetAuthor.setHint(getString(R.string.poem_hint_author));
                poemAndStoryContent.setText(getString(R.string.poem_Poem));
                poemAndStoryGetPoemAndStory.setHint(getString(R.string.poem_hint_poem));
                poemUploadImage.setVisibility(View.VISIBLE);
                setBackgroundImage(7);

            } else if (integer == 9) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.ask_a_question_upload_fragment));
                quotesAndQuestionView.setVisibility(View.VISIBLE);
                quotesAndQuestionGetQuotesAndQuestion.setHint(getString(R.string.ask_question_hint));
            } else if (integer == 10) {
                writingUploadFragmentToolbar.setTitle(getString(R.string.my_ideas_my_uploads));
                dreamsAndIdeasView.setVisibility(View.VISIBLE);
                dreamAndIdeasTextView.setText(getString(R.string.ideas_my_ideas));
                dreamsAndIdeasGetDreamsAndIdeas.setHint(getString(R.string.ideas_hint));
            }
        });

        // Observing connected user view model
        connectedUsersViewModel.getConnectedUsers().observe(getViewLifecycleOwner(), parseUsers -> {
            if (parseUsers != null) {
                setConnectedUser(parseUsers);
            }
        });

        return rootView;
    }

    // setting connected users recycler view
    private void setConnectedUser(List<ParseUser> parseUsers) {
        ConnectedUsersRecyclerAdapter adapter = new ConnectedUsersRecyclerAdapter(mContext, parseUsers, this);
        writingUploadConnectedRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        writingUploadConnectedRecyclerView.setAdapter(adapter);
    }

    // Initilizing all views
    private void initializeWritingFragmentViews() {
        // init views
        writingUploadFragmentToolbar = (Toolbar) rootView.findViewById(R.id.writing_fragment_upload_toolbar);


        // init included views
        quotesAndQuestionView = (View) rootView.findViewById(R.id.quotes_and_question_view);
        poemAndStoryView = (View) rootView.findViewById(R.id.poem_and_story_view);
        dreamsAndIdeasView = (View) rootView.findViewById(R.id.dreams_and_ideas_view);
        blogAndNewsView = (View) rootView.findViewById(R.id.blogs_and_news_view);


        // init Writing upload views
        writingUploadGetDescription = (EditText) rootView.findViewById(R.id.writing_upload_get_description_edit_text);
        writingUploadGetConnectedUsername = (AutoCompleteTextView) rootView.findViewById(R.id.writing_upload_get_connected_username_edit_text);
        writingUploadGetLocation = (EditText) rootView.findViewById(R.id.writing_upload_get_location_edit_text);
        writingUploadToParseButton = (Button) rootView.findViewById(R.id.writing_upload_upload_to_parse_button);
        writingUploadConnectedUserProgressBar = (ProgressBar) rootView.findViewById(R.id.writing_upload_connected_users_progress_bar);
        writingUploadContentProgressBar = (ProgressBar) rootView.findViewById(R.id.writing_upload_fragment_upload_content_progress_bar);
        writingUploadConnectedRecyclerView = (RecyclerView) rootView.findViewById(R.id.writing_upload_connected_recycler_view);
        writingUploadConnectedMessageTextView = (TextView) rootView.findViewById(R.id.writing_upload_connected_message_text_view);
        writingUploadShowPostPreviewButton = (Button) rootView.findViewById(R.id.writing_upload_show_post_preview_btn);



        // init included layout views
        // Blogs and news
        blogsAndNewsTitle = (TextView) rootView.findViewById(R.id.blog_and_news_title_text_view);
        blogsAndNewsGetTitle = (EditText) rootView.findViewById(R.id.blog_and_news_get_blog_news_title_edit_text);
        blogsAndNewsContent = (TextView) rootView.findViewById(R.id.blog_and_news_content_text_view);
        blogsAndNewsGetContent = (EditText) rootView.findViewById(R.id.blog_and_news_get_blog_news_content_edit_text);
        blogsAndNewsAttachImages = (TextView) rootView.findViewById(R.id.blog_and_news_attach_images_text_view);
        blogsAndNewsAttachedImagesRecyclerView = (RecyclerView) rootView.findViewById(R.id.blog_and_news_attached_images_recycler_view);
        blogAndNewsBetaWriter = (TextView) rootView.findViewById(R.id.blog_and_news_beta_writer);


        // Poem and story
        poemAndStoryGetTitle = (EditText) rootView.findViewById(R.id.poem_and_story_get_poem_and_story_title_edit_text);
        poemAndStoryAuthor = (TextView) rootView.findViewById(R.id.poem_and_story_author_text_view);
        poemAndStoryGetAuthor = (EditText) rootView.findViewById(R.id.poem_and_story_get_poem_and_story_author_edit_text);
        poemAndStoryContent = (TextView) rootView.findViewById(R.id.poem_and_story_poem_and_story_text_view);
        poemUploadImage = (TextView) rootView.findViewById(R.id.poem_and_story_upload_image_text_view);
        poemUploadedImage = (ImageView) rootView.findViewById(R.id.poem_get_uploaded_image_image_view);
        poemAndStoryGetPoemAndStory = (EditText) rootView.findViewById(R.id.poem_and_story_get_poem_and_story_edit_text);


        // Quotes and give question
        quotesAndQuestionGetQuotesAndQuestion = (EditText) rootView.findViewById(R.id.quotes_and_question_get_quotes_and_question_edit_text);
        quotesAndThoughtsBackgroundImage = (ImageView) rootView.findViewById(R.id.quotes_and_thoughts_background_image_image_view);
        quotesAndThoughtsChangeBackground = (TextView) rootView.findViewById(R.id.quotes_and_thoughts_change_background_text_view);

        // Dreams and ideas
        dreamAndIdeasTextView = (TextView) rootView.findViewById(R.id.saw_a_dream_and_ideas_text_view);
        dreamsMessageTextView = (TextView) rootView.findViewById(R.id.dreams_message_text_view);
        dreamsAndIdeasGetDreamsAndIdeas = (EditText) rootView.findViewById(R.id.dreams_and_ideas_get_dreams_edit_text);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // observing view model
        htmlFormatViewModel.getHtmlFormat().observe(getViewLifecycleOwner(),s -> {
            if (s != null){
                blogsAndNewsGetContent.setText(DocNormalizer.htmlToNormal(s));
                htmlFormatViewModel.setHtmlString(null);
            }
        });

        // toolbar navigation on click listener
        writingUploadFragmentToolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        // Connected username Auto complete edit text
        writingUploadGetConnectedUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                writingUploadConnectedUserProgressBar.setVisibility(View.VISIBLE);
                getUsersForConnections(s.toString().trim());
            }
        });

        blogsAndNewsAttachImages.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_FROM_WRITING_UPLOAD_SELECT_MULTI,true);
            navController.navigate(R.id.action_writingUploadFragment_to_mobileImagesFragment,bundle);
        });

        poemUploadImage.setOnClickListener(v -> {
            multiImageViewModel.setPreSelectedImages(new ArrayList<>());
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_CALLED_FROM_WRITING_UPLOAD,true);
            navController.navigate(R.id.action_writingUploadFragment_to_mobileImagesFragment,bundle);
        });

        quotesAndThoughtsChangeBackground.setOnClickListener(v -> {
            multiImageViewModel.setPreSelectedImages(new ArrayList<>());
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.BUNDLE_CALLED_FROM_WRITING_UPLOAD,true);
            navController.navigate(R.id.action_writingUploadFragment_to_mobileImagesFragment,bundle);
        });

        writingUploadToParseButton.setOnClickListener(v -> {
            uploadToParse(selectedPosition);
        });

        writingUploadShowPostPreviewButton.setOnClickListener(view1 -> {
            Bundle bundle = getContentBundle();
            if (bundle != null)
                navController.navigate(R.id.action_writingUploadFragment_to_postPreviewFragment,bundle);
        });

        blogAndNewsBetaWriter.setOnClickListener(betaWriter ->{
            Bundle details = new Bundle();
            details.putString(HashMapKeys.BUNDLE_TITLE_HEADING_KEY,blogsAndNewsTitle.getText().toString());
            details.putString(HashMapKeys.BUNDLE_CONTENT_HEADING_KEY,blogsAndNewsContent.getText().toString());
            details.putString(HashMapKeys.BUNDLE_BLOG_OR_NEWS_TITLE_HINT_KEY,blogsAndNewsGetTitle.getHint().toString());
            details.putString(HashMapKeys.BUNDLE_BLOG_OR_NEWS_CONTENT_HINT_KEY,blogsAndNewsGetContent.getHint().toString());
            navController.navigate(R.id.action_writingUploadFragment_to_betaWriterFragment,details);
        });

    }

    private Bundle getContentBundle(){
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.BUNDLE_SELECTED_TYPE_FOR_UPLOAD_KEY,selectedPosition);
        bundle.putString(HashMapKeys.CONTENT_LOCATION_KEY,writingUploadGetLocation.getText().toString().trim());
        String description = DocNormalizer.spannedToHtml(writingUploadGetDescription.getText());
        bundle.putString(HashMapKeys.CONTENT_DESCRIPTION_KEY,description);
        switch (selectedPosition){
            // Preview bundle for blog or news content
            case 1:
            case 4:{
                bundle.putString(HashMapKeys.BLOG_OR_NEWS_TITLE_KEY,blogsAndNewsGetTitle.getText().toString().trim());
                String htmlString = DocNormalizer.spannedToHtml(blogsAndNewsGetContent.getText());
                bundle.putString(HashMapKeys.BLOG_OR_NEWS_CONTENT_KEY,htmlString);
                return bundle;
            }
            // Preview bundle for dream or idea content
            case 2:
            case 10:{
                String htmlString = DocNormalizer.spannedToHtml(dreamsAndIdeasGetDreamsAndIdeas.getText());
                bundle.putString(HashMapKeys.DREAM_OR_IDEA_CONTENT_KEY,htmlString);
                return bundle;
            }
            // Preview bundle for quotes or question content
            case 3:
            case 9:{
                bundle.putString(HashMapKeys.QUOTES_OR_QUESTION_CONTENT_KEY,quotesAndQuestionGetQuotesAndQuestion.getText().toString().trim());
                return bundle;
            }
            // Preview bundle for story or poem content
            case 6:
            case 7:{
                bundle.putString(HashMapKeys.POEM_OR_STORY_TITLE_KEY,poemAndStoryGetTitle.getText().toString().trim());
                bundle.putString(HashMapKeys.POEM_OR_STORY_AUTHOR_KEY,poemAndStoryGetAuthor.getText().toString().trim());
                String htmlString = DocNormalizer.spannedToHtml(poemAndStoryGetPoemAndStory.getText());
                bundle.putString(HashMapKeys.POEM_OR_STORY_CONTENT_KEY,htmlString);
                return bundle;
            }
            default:{
                return null;
            }
        }
    }


    // Getting username from the database to display on auto complete edit text
    private void getUsersForConnections(String username) {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.SEARCHED_USERNAME_KEY,username);
        ParseCloud.callFunctionInBackground("searchUser",hashMap,(FunctionCallback<Map<String,Object>>) (object,e)->{
            if (e == null) {
                List<ParseUser> parseUsers = (List<ParseUser>) object.get("result");
                if (parseUsers != null) {
                    if (parseUsers.size() > 0) {
                        List<String> stringList = new ArrayList<>();
                        for (int i = 0; i < parseUsers.size(); i++) {
                            stringList.add(parseUsers.get(i).getUsername());
                        }
                        ConnectedUsersAdapter adapter = new ConnectedUsersAdapter(mContext, stringList, parseUsers, this);
                        writingUploadGetConnectedUsername.setAdapter(adapter);
                    }
                }
            }
            if (writingUploadConnectedUserProgressBar != null)
                writingUploadConnectedUserProgressBar.setVisibility(View.GONE);
        });
    }

    // the user is selected using Connected user adapter for connection
    @Override
    public void getConnectedUser(ParseUser userObject) {
        writingUploadGetConnectedUsername.setText("");
        connectedUsersViewModel.connectUser(userObject);
        writingUploadConnectedMessageTextView.setVisibility(View.GONE);
    }


    private void setAttachedImages() {
        if (multiImageViewModel.getPreSelectedImages().size() > 0){
            blogsAndNewsAttachedImagesRecyclerView.setVisibility(View.VISIBLE);
            BlogsAndNewsAdapter adapter = new BlogsAndNewsAdapter(mContext,
                    multiImageViewModel.getPreSelectedImages(),
                    WritingUploadFragment.this);

            blogsAndNewsAttachedImagesRecyclerView.setAdapter(adapter);
            blogsAndNewsAttachedImagesRecyclerView.setLayoutManager(new GridLayoutManager(mContext,3));
        }
    }

    private void setBackgroundImage(int which){
        if (which == 7){
            if (multiImageViewModel.getPreSelectedImages().size() > 0){
                poemUploadedImage.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(multiImageViewModel.getPreSelectedImages().get(0)).fitCenter().into(poemUploadedImage);
            }
        } else{
            if (multiImageViewModel.getPreSelectedImages().size() > 0) {
                quotesAndThoughtsBackgroundImage.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(multiImageViewModel.getPreSelectedImages().get(0)).centerCrop().into(quotesAndThoughtsBackgroundImage);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        nullAllViews();
    }


    // Null all writing upload fragment views
    private void nullAllViews() {
        rootView = null;
        writingUploadFragmentToolbar = null;

        writingUploadGetDescription = null;
        writingUploadGetConnectedUsername = null;
        writingUploadGetLocation = null;
        writingUploadToParseButton = null;
        writingUploadConnectedUserProgressBar = null;
        writingUploadContentProgressBar = null;
        writingUploadConnectedRecyclerView = null;
        writingUploadConnectedMessageTextView = null;
        writingUploadShowPostPreviewButton = null;

        quotesAndQuestionView = null;
        poemAndStoryView = null;
        dreamsAndIdeasView = null;
        blogAndNewsView = null;

        blogsAndNewsTitle = null;
        blogsAndNewsContent = null;
        blogsAndNewsAttachImages = null;
        blogsAndNewsGetTitle = null;
        blogsAndNewsGetContent = null;
        blogsAndNewsAttachedImagesRecyclerView = null;
        blogAndNewsBetaWriter = null;

        poemAndStoryGetTitle = null;
        poemAndStoryGetAuthor = null;
        poemAndStoryGetPoemAndStory = null;
        poemAndStoryAuthor = null;
        poemAndStoryContent = null;
        poemUploadImage = null;
        poemUploadedImage = null;

        dreamAndIdeasTextView = null;
        dreamsMessageTextView = null;
        dreamsAndIdeasGetDreamsAndIdeas = null;

        quotesAndQuestionGetQuotesAndQuestion = null;
        quotesAndThoughtsChangeBackground = null;
        quotesAndThoughtsBackgroundImage = null;
    }


    // Blogs and news adapter interface method
    @Override
    public void removeAttachedImage(List<Uri> removedImageUri) {
        multiImageViewModel.setPreSelectedImages(removedImageUri);
    }


    /*
        1 Blog
        2 Had a dream today
        3 quotes and thoughts
        4 news and updates
        6 story
        7 poem
        9 give a question
        10 ideas
    */

    /**
     * Blog, News, Dreams, Ideas --- Main content of this content will be stored in largeContent coloum in parse file
     * Quotes --- Quotes Background Image will be stored in parseFile singleImage coloum
     * Poem & Story --- Main Content of this content will be stored in parseFile in largeContent and poem
     *                  background image will be stored in singleImage Coloum
     */

    // Upload content to parse server
    private void uploadToParse(int contentType) {
        if (contentType == 1  ||  contentType == 4){
            blogOrNewsContentUpload(contentType);
        }

        else if (contentType == 2 || contentType == 10) {
            dreamOrIdeasUpload(contentType);
        }

        else if (contentType == 3 || contentType == 9) {
            quotesOrQuestionUpload(contentType);
        }

        else if (contentType == 6 || contentType == 7) {
            storyOrPoemUpload(contentType);
        }
    }


    // Is blog or news written properly
    private boolean isBlogOrNewsWrittenProperly() {
        if (blogsAndNewsGetTitle.getText().toString().trim().length() == 0 || blogsAndNewsGetContent.getText().toString().trim().length() == 0){
            if (blogsAndNewsGetTitle.getText().toString().trim().length() == 0)
                blogsAndNewsGetTitle.setError("Please enter title");
            else if (blogsAndNewsGetContent.getText().toString().trim().length() == 0)
                blogsAndNewsGetContent.setError("Write something here");

            return false;
        }else{
            return true;
        }
    }

    private void blogOrNewsContentUpload(int contentType){
        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.UPLOAD_CONTENT_TYPE_KEY,contentType);

        if (isBlogOrNewsWrittenProperly()){

            String contentPreview;
            if (blogsAndNewsGetContent.getText().toString().trim().length() > 300)
                    contentPreview = blogsAndNewsGetContent.getText().toString().substring(0,300).trim();
            else
                contentPreview = blogsAndNewsGetContent.getText().toString().trim()+"";

            String title = DocNormalizer.spannedToHtml(blogsAndNewsGetTitle.getText());
            String content = DocNormalizer.spannedToHtml(blogsAndNewsGetContent.getText());

            hashMap.put(HashMapKeys.BLOG_OR_NEWS_TITLE_KEY, title);
            hashMap.put(HashMapKeys.CONTENT_TEXT_PREVIEW_KEY,contentPreview);
            hashMap.put(HashMapKeys.BLOG_OR_NEWS_CONTENT_KEY, content.getBytes()); //This will store in parse file

            ConvertUriToByteArray.getByteArrayFromImagesList(multiImageViewModel.getPreSelectedImages(), getBytesImages -> {
                hashMap.put(HashMapKeys.MULTI_IMAGES_ATTACHED_KEY,getBytesImages); // This will stored in parse file
                uploadContentViaCloud(hashMap);
                }, mContext.getApplicationContext());

        }
    }


    // Is dream today or ideas written properly
    private boolean isHadDreamTodayOrIdeasWrittenProperly() {
        if (dreamsAndIdeasGetDreamsAndIdeas.getText().toString().trim().length() <= 0) {
            dreamsAndIdeasGetDreamsAndIdeas.setError("Write something here...");
            return false;
        } else
            return true;
    }


    private void dreamOrIdeasUpload(int contentType){
        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.UPLOAD_CONTENT_TYPE_KEY,contentType);

        if (isHadDreamTodayOrIdeasWrittenProperly()){

            String contentPreview;
            if (dreamsAndIdeasGetDreamsAndIdeas.getText().toString().trim().length() > 300)
                contentPreview = dreamsAndIdeasGetDreamsAndIdeas.getText().toString().substring(0,300).trim();
            else
                contentPreview = dreamsAndIdeasGetDreamsAndIdeas.getText().toString().trim()+"";

            String dream = DocNormalizer.spannedToHtml(dreamsAndIdeasGetDreamsAndIdeas.getText());
            hashMap.put(HashMapKeys.CONTENT_TEXT_PREVIEW_KEY,contentPreview);
            hashMap.put(HashMapKeys.DREAM_OR_IDEA_CONTENT_KEY, dream.getBytes());  // Will save in parse file
            uploadContentViaCloud(hashMap);
        }
    }


    // Is Quotes or Question written properly
    private boolean isQuotesOrQuestionWrittenProperly() {
        if (quotesAndQuestionGetQuotesAndQuestion.getText().toString().trim().length() <= 0) {
            quotesAndQuestionGetQuotesAndQuestion.setError("Write something here...");
            return false;
        } else {
            return true;
        }
    }


    private void quotesOrQuestionUpload(int contentType){
        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.UPLOAD_CONTENT_TYPE_KEY,contentType);

        if (isQuotesOrQuestionWrittenProperly()){
            String quotes = DocNormalizer.spannedToHtml(quotesAndQuestionGetQuotesAndQuestion.getText());
            hashMap.put(HashMapKeys.QUOTES_OR_QUESTION_CONTENT_KEY, quotes);

            if (multiImageViewModel.getPreSelectedImages().size() > 0){
                ConvertUriToByteArray.getByteArrayFromImage(multiImageViewModel.getPreSelectedImages().get(0),getByteArray -> {
                    hashMap.put(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,getByteArray); // get single byte array of an image
                    uploadContentViaCloud(hashMap);
                },mContext.getApplicationContext());
            }else{
                hashMap.put(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,new byte[]{});
                uploadContentViaCloud(hashMap);
            }
        }
    }

    // Is story Or poem written properly
    private boolean isStoryOrPoemWrittenProperly() {
        if (poemAndStoryGetTitle.getText().toString().trim().length() <= 0 ||
                poemAndStoryGetAuthor.getText().toString().trim().length() <= 0 ||
                poemAndStoryGetPoemAndStory.getText().toString().trim().length() <= 0) {

            if (poemAndStoryGetTitle.getText().toString().trim().length() <= 0)
                poemAndStoryGetTitle.setError("Please enter title");

            else if (poemAndStoryGetAuthor.getText().toString().trim().length() <= 0)
                poemAndStoryGetAuthor.setError("Who's the author");

            else if (poemAndStoryGetPoemAndStory.getText().toString().trim().length() <= 0)
                poemAndStoryGetPoemAndStory.setError("Write something here...");

            return false;
        } else
            return true;
    }


    private void storyOrPoemUpload(int contentType){
        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put(HashMapKeys.UPLOAD_CONTENT_TYPE_KEY,contentType);

        if (isStoryOrPoemWrittenProperly()){

            String contentPreview;
            if (poemAndStoryGetPoemAndStory.getText().toString().trim().length() > 300)
                contentPreview = poemAndStoryGetPoemAndStory.getText().toString().substring(0,300).trim();
            else
                contentPreview = poemAndStoryGetPoemAndStory.getText().toString().trim();

            String title = DocNormalizer.spannedToHtml(poemAndStoryGetTitle.getText());
            String author = DocNormalizer.spannedToHtml(poemAndStoryGetAuthor.getText());
            String content = DocNormalizer.spannedToHtml(poemAndStoryGetPoemAndStory.getText());

            hashMap.put(HashMapKeys.POEM_OR_STORY_TITLE_KEY, title);
            hashMap.put(HashMapKeys.POEM_OR_STORY_AUTHOR_KEY, author);
            hashMap.put(HashMapKeys.CONTENT_TEXT_PREVIEW_KEY,contentPreview);
            hashMap.put(HashMapKeys.POEM_OR_STORY_CONTENT_KEY, content.getBytes()); // This will saved in parse file

            if (multiImageViewModel.getPreSelectedImages().size() > 0){
                ConvertUriToByteArray.getByteArrayFromImage(multiImageViewModel.getPreSelectedImages().get(0),getByteArray -> {
                    hashMap.put(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,getByteArray);
                    uploadContentViaCloud(hashMap); // This will single image byte array
                },mContext.getApplicationContext());
            }else{
                hashMap.put(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY,new byte[]{});
                uploadContentViaCloud(hashMap);
            }
        }
    }


    private void uploadContentViaCloud(HashMap<String,Object> hashMap){

        hideUploadButton(true);

        String description = DocNormalizer.spannedToHtml(writingUploadGetDescription.getText());

        String location = writingUploadGetLocation.getText().toString()+"";

        hashMap.put(HashMapKeys.CONTENT_DESCRIPTION_KEY,description);
        hashMap.put(HashMapKeys.CONTENT_LOCATION_KEY,location);

        List<String> username = new ArrayList<>();
        for (int i=0; i<connectedUsersViewModel.getConnectedUser().size(); i++){
            username.add(connectedUsersViewModel.getConnectedUser().get(i).getUsername());
        }
        hashMap.put(HashMapKeys.CONNECTED_USERS_KEY,username);


        ParseCloud.callFunctionInBackground("uploadContentToParse", hashMap, (object, e) -> {
            if (e == null){
                // successfully uploaded content
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(()->{
                    navController.navigate(R.id.action_writingUploadFragment_to_bottomHomeFragment);
                },2000);
                sessionErrorViewModel.setIsToHideBottomNavView(false);
                writingUploadContentProgressBar.setVisibility(View.GONE);
                Snackbar.make(mContext,writingUploadToParseButton,"Successfully uploaded your content.",1500)
                        .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                        .setBackgroundTint(ContextCompat.getColor(mContext,R.color.upload_logo_color))
                        .show();
            }else{
                // failed to upload content
                if (e.getCode() == 209){
                    Toast.makeText(mContext, getString(R.string.session_expired_message), Toast.LENGTH_LONG).show();
                    sessionErrorViewModel.isSessionExpired(true);
                    return;
                }

                // Failed to upload content to parse server
                hideUploadButton(false);
                Snackbar.make(mContext,writingUploadToParseButton,"Uff! Something went wrong please try again.",1500)
                        .setTextColor(ContextCompat.getColor(mContext,R.color.white))
                        .setBackgroundTint(ContextCompat.getColor(mContext, R.color.red_help_people))
                        .show();
            }
        });
    }

    // If ture then hide the button false show the button
    private void hideUploadButton(boolean hide){
        ObjectAnimator animator;
        if (hide){
            writingUploadContentProgressBar.setVisibility(View.VISIBLE);
            writingUploadToParseButton.setEnabled(false);
            animator = ObjectAnimator.ofFloat(writingUploadToParseButton,"scaleX",1.0f,0f);
        }else{
            writingUploadContentProgressBar.setVisibility(View.GONE);
            writingUploadToParseButton.setEnabled(true);
            animator  = ObjectAnimator.ofFloat(writingUploadToParseButton,"scaleX",0f,1.0f);
        }
        animator.setDuration(300);
        animator.start();
    }

    // connected recycler view adapter interface (removing connected users)
    @Override
    public void removeConnectedUser(int position) {
        connectedUsersViewModel.removeUser(position);
        if (connectedUsersViewModel.getConnectedUser().size() == 0)
            writingUploadConnectedMessageTextView.setVisibility(View.VISIBLE);
        else
            writingUploadConnectedMessageTextView.setVisibility(View.GONE);
    }
}