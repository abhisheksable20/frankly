package com.sakesnake.frankly.home.bottomnavupload.postpreview;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.RandomColorGenerator;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.MultiImageViewModel;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;
import com.sakesnake.frankly.home.postfeeds.AttachedImagesAdapter;
import com.sakesnake.frankly.porterstemmer.DocsInfo;
import com.parse.ParseUser;

public class PostPreviewFragment extends Fragment {
    private View rootView;
    private ViewStub imageOrMemesPreview,
            blogOrNewsPreview,
            dreamsOrIdeasPreview,
            storyPreview, poemPreview,
            quotesPreview,questionPreview;
    private NavController navController;
    private MultiImageViewModel multiImageViewModel;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_post_preview, container, false);

        // init nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        // Getting multi image and html view model view model
        multiImageViewModel = new ViewModelProvider(requireActivity()).get(MultiImageViewModel.class);

        // init views
        imageOrMemesPreview = (ViewStub) rootView.findViewById(R.id.images_or_memes_preview_view_stub);
        dreamsOrIdeasPreview = (ViewStub) rootView.findViewById(R.id.dreams_or_ideas_preview_view_stub);
        storyPreview = (ViewStub) rootView.findViewById(R.id.story_preview_view_stub);
        poemPreview = (ViewStub) rootView.findViewById(R.id.poem_preview_view_stub);
        quotesPreview = (ViewStub) rootView.findViewById(R.id.quotes_preview_view_stub);
        blogOrNewsPreview = (ViewStub) rootView.findViewById(R.id.blog_or_news_preview_view_stub);
        questionPreview = (ViewStub) rootView.findViewById(R.id.question_preview_view_stub);

        if (getArguments() != null && getArguments().containsKey(Constants.BUNDLE_SELECTED_TYPE_FOR_UPLOAD_KEY)){
            setPostPreview(getArguments().getInt(Constants.BUNDLE_SELECTED_TYPE_FOR_UPLOAD_KEY));
        }
        else{
            Toast.makeText(mContext, "Something went wrong. Restart the app.", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }

        return rootView;
    }

    private void setPostPreview(final int contentType){
        switch (contentType){
            case 0:
            case 5:{
                showImageOrMemesPreview();
                break;
            }
            case 1:
            case 4:{
                showBlogOrNewsPreview(contentType);
                break;
            }
            case 2:
            case 10:{
                showDreamOrIdeasPreview(contentType);
                break;
            }
            case 3:{
                showQuotesPreview();
                break;
            }
            case 9:{
                showQuestionPreview();
                break;
            }
            case 6:
            case 7:{
                showPoemOrStoryPreview(contentType);
                break;
            }
        }
    }

    private void showImageOrMemesPreview(){
        if (!(getArguments().containsKey(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY)))
            return;

        String imageUri = getArguments().getString(HashMapKeys.SINGLE_IMAGE_ATTACHED_KEY);

        View memeOrImageView = imageOrMemesPreview.inflate();
        ImageView imageOrMemeContent = (ImageView) memeOrImageView.findViewById(R.id.feed_user_main_post_image_view);
        TextView username = (TextView) memeOrImageView.findViewById(R.id.feed_user_username_text_view);
        ImageView profileImage = (ImageView) memeOrImageView.findViewById(R.id.feed_user_profile_photo_image_view);
        TextView contentDescription = (TextView) memeOrImageView.findViewById(R.id.feed_user_post_description_text_view);
        TextView contentLocation = (TextView) memeOrImageView.findViewById(R.id.feed_post_location_text_view);

        Glide.with(mContext).load(Uri.parse(imageUri)).into(imageOrMemeContent);
        setUserInfo(username,profileImage);
        setContentExtraInfo(contentLocation,contentDescription);
    }


    // Showing BlogOrNews Content preview
    private void showBlogOrNewsPreview(final int type){

        String title = getArguments().getString(HashMapKeys.BLOG_OR_NEWS_TITLE_KEY,"");
        String content = getArguments().getString(HashMapKeys.BLOG_OR_NEWS_CONTENT_KEY,"");

        View blogOrNewsView = blogOrNewsPreview.inflate();
        TextView blogTitle = (TextView) blogOrNewsView.findViewById(R.id.content_title_blog_or_news_feed_fragment_text_view);
        TextView blogContent = (TextView) blogOrNewsView.findViewById(R.id.main_content_blog_or_news_feed_fragment_text_view);
        ImageView backgroundImage = (ImageView) blogOrNewsView.findViewById(R.id.background_image_blog_or_news_feed_fragment_image_view);
        RecyclerView attachedImages = (RecyclerView) blogOrNewsView.findViewById(R.id.attached_images_blog_or_news_feed_fragment_recycler_view);
        TextView keywords = (TextView) blogOrNewsView.findViewById(R.id.keywords_of_blog_or_news_feed_fragment_text_view);
        TextView totalWords = (TextView) blogOrNewsView.findViewById(R.id.total_words_blog_or_news_feed_fragment_text_view);
        TextView description = (TextView) blogOrNewsView.findViewById(R.id.feed_user_post_description_text_view);
        TextView postLocation = (TextView) blogOrNewsView.findViewById(R.id.feed_post_location_text_view);
        TextView username = (TextView) blogOrNewsView.findViewById(R.id.profile_username_blog_or_news_feed_fragment_text_view);
        ImageView profileImage = (ImageView) blogOrNewsView.findViewById(R.id.profile_image_blog_or_news_feed_fragment_image_view);
        ProgressBar progressBar = (ProgressBar) blogOrNewsView.findViewById(R.id.bottom_progress_bar);

        progressBar.setVisibility(View.GONE);

        //Setting views
        blogTitle.setText(title);
        blogContent.setText(DocNormalizer.htmlToNormal(content));

        setUserInfo(username,profileImage);
        setDocsInfo(content,keywords,totalWords);
        setContentExtraInfo(postLocation,description);
        setContentBackgroundAndRecyclerView(attachedImages,backgroundImage);
    }

    // Showing DreamOrIdeas Preview
    private void showDreamOrIdeasPreview(final int contentType){
        String content = getArguments().getString(HashMapKeys.DREAM_OR_IDEA_CONTENT_KEY,"");

        View dreamOrIdeasView = dreamsOrIdeasPreview.inflate();
        ImageView dreamOrIdeasBackground = (ImageView) dreamOrIdeasView.findViewById(R.id.background_dream_or_idea_feed_fragment_image_view);
        ImageView dreamMatched = (ImageView) dreamOrIdeasView.findViewById(R.id.feed_like_user_post_image_view);
        TextView dreamOrIdeasContent = (TextView) dreamOrIdeasView.findViewById(R.id.content_dream_or_idea_feed_fragment_text_view);
        TextView dreamMatchHeading = (TextView) dreamOrIdeasView.findViewById(R.id.dream_match_heading_dream_feed_fragment_text_view);
        TextView postDescription = (TextView) dreamOrIdeasView.findViewById(R.id.feed_user_post_description_text_view);
        TextView postLocation = (TextView) dreamOrIdeasView.findViewById(R.id.feed_post_location_text_view);
        TextView username = (TextView) dreamOrIdeasView.findViewById(R.id.profile_username_dream_or_idea_feed_fragment_text_view);
        ImageView profileImage = (ImageView) dreamOrIdeasView.findViewById(R.id.profile_pic_dream_or_idea_feed_fragment_image_view);
        ProgressBar progressBar = (ProgressBar) dreamOrIdeasView.findViewById(R.id.bottom_progress_bar);

        progressBar.setVisibility(View.GONE);

        // Setting views
        dreamOrIdeasBackground.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
        dreamOrIdeasContent.setText(DocNormalizer.htmlToNormal(content));


        if (contentType == 2){
            dreamMatched.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.dream_match_icon));
            dreamMatchHeading.setVisibility(View.VISIBLE);
        }

        setUserInfo(username,profileImage);
        setContentExtraInfo(postLocation,postDescription);

    }

    // Showing Quotes Preview
    private void showQuotesPreview(){
        String content = getArguments().getString(HashMapKeys.QUOTES_OR_QUESTION_CONTENT_KEY,"");

        View quotesPreviewView = quotesPreview.inflate();
        ImageView quotesBackgroundImage = (ImageView) quotesPreviewView.findViewById(R.id.background_color_image_view);
        TextView quoteContent = (TextView) quotesPreviewView.findViewById(R.id.feed_user_quote_text_view);
        TextView username = (TextView) quotesPreviewView.findViewById(R.id.feed_user_username_text_view);
        ImageView profileImage = (ImageView) quotesPreviewView.findViewById(R.id.feed_user_profile_photo_image_view);
        TextView postDescription = (TextView) quotesPreviewView.findViewById(R.id.feed_user_post_description_text_view);
        TextView postLocation = (TextView) quotesPreviewView.findViewById(R.id.feed_post_location_text_view);

        // Setting up all preview views
        quoteContent.setText(content);

        setUserInfo(username,profileImage);
        setContentBackgroundAndRecyclerView(null,quotesBackgroundImage);
        setContentExtraInfo(postLocation,postDescription);
    }

    // Showing Question Preview
    private void showQuestionPreview(){
        String content = getArguments().getString(HashMapKeys.QUOTES_OR_QUESTION_CONTENT_KEY,"");

        View questionPreviewView = questionPreview.inflate();

        TextView questionContent = (TextView) questionPreviewView.findViewById(R.id.question_feed_content_text_view);
        ImageView userProfileImage = (ImageView) questionPreviewView.findViewById(R.id.feed_user_profile_photo_image_view);
        ImageView contentBackgroundImage = (ImageView) questionPreviewView.findViewById(R.id.question_feed_background_image_view);
        TextView username = (TextView) questionPreviewView.findViewById(R.id.feed_user_username_text_view);
        TextView postDescription = (TextView) questionPreviewView.findViewById(R.id.feed_user_post_description_text_view);
        TextView postLocation = (TextView) questionPreviewView.findViewById(R.id.feed_post_location_text_view);
        ImageView postAnswersComments = (ImageView) questionPreviewView.findViewById(R.id.feed_comment_user_post_image_view);

        contentBackgroundImage.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        postAnswersComments.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.question_comment_icon));

        questionContent.setText(DocNormalizer.htmlToNormal(content));
        setContentExtraInfo(postLocation,postDescription);
        setUserInfo(username,userProfileImage);
    }

    // Showing Story Preview
    private void showPoemOrStoryPreview(final int contentType){
        String title = getArguments().getString(HashMapKeys.POEM_OR_STORY_TITLE_KEY,"");
        String author = getArguments().getString(HashMapKeys.POEM_OR_STORY_AUTHOR_KEY,"");
        String content = getArguments().getString(HashMapKeys.POEM_OR_STORY_CONTENT_KEY,"");

        View storyOrPoemPreview;
        if (contentType == 6)
            storyOrPoemPreview = storyPreview.inflate();
        else
            storyOrPoemPreview = poemPreview.inflate();

        // Init view
        ImageView poemOrStoryBackground = (ImageView) storyOrPoemPreview.findViewById(R.id.content_background_poem_or_story_image_view);
        ImageView poemContentBackground = (ImageView) storyOrPoemPreview.findViewById(R.id.background_poem_feed_fragment_image_view);
        TextView postTitle = (TextView) storyOrPoemPreview.findViewById(R.id.title_poem_or_story_feed_fragment_text_view);
        TextView postAuthor = (TextView) storyOrPoemPreview.findViewById(R.id.author_poem_or_story_feed_fragment_text_view);
        TextView postContent = (TextView) storyOrPoemPreview.findViewById(R.id.content_poem_or_story_feed_fragment_text_view);
        TextView postTotalWords = (TextView) storyOrPoemPreview.findViewById(R.id.total_words_poem_or_story_feed_fragment_text_view);
        TextView postKeywords = (TextView) storyOrPoemPreview.findViewById(R.id.keywords_of_poem_or_story_feed_fragment_text_view);
        TextView postDescription = (TextView) storyOrPoemPreview.findViewById(R.id.feed_user_post_description_text_view);
        TextView postLocation = (TextView) storyOrPoemPreview.findViewById(R.id.feed_post_location_text_view);
        TextView username = (TextView) storyOrPoemPreview.findViewById(R.id.profile_username_poem_or_story_feed_fragment_text_view);
        ImageView profileImage = (ImageView) storyOrPoemPreview.findViewById(R.id.profile_image_poem_or_story_feed_fragment_image_view);
        ProgressBar progressBar = (ProgressBar) storyOrPoemPreview.findViewById(R.id.bottom_progress_bar);

        progressBar.setVisibility(View.GONE);

        // Setting up views
        postTitle.setText(title);
        postAuthor.setText(getString(R.string.author,author));
        postContent.setText(DocNormalizer.htmlToNormal(content));

        setUserInfo(username,profileImage);
        setContentBackgroundAndRecyclerView(null,poemOrStoryBackground);
        setDocsInfo(content,postKeywords,postTotalWords);
        setContentExtraInfo(postLocation,postDescription);

        if (contentType == 7)
            poemContentBackground.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
    }


    // Setting user info like username and profile photo
    private void setUserInfo(@NonNull final TextView username, @NonNull final ImageView profileImage){
        username.setText(ParseUser.getCurrentUser().getUsername());
    }


    // Setting basic info about writing content like total words an keywords
    private void setDocsInfo(@NonNull final String content,@NonNull final TextView keywords,@NonNull final TextView totalWordsView){
        DocsInfo.totalWords(DocNormalizer.htmlToNormal(content).toString(),
                ((totalWords, rootWords) -> {
            keywords.setText(rootWords);
            keywords.setSelected(true);

            String string = "Total words \u2022 "+totalWords;

            SpannableString spannableString = new SpannableString(string);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD),0,string.indexOf("\u2022"),Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            totalWordsView.setText(spannableString);
        }));
    }

    // Setting blog or news recycler view images and background for images for all type of content
    private void setContentBackgroundAndRecyclerView(final RecyclerView recyclerView, @NonNull final ImageView backgroundImage){
        if (multiImageViewModel.getPreSelectedImages().size() > 0){
            Glide.with(mContext).load(multiImageViewModel.getPreSelectedImages().get(0)).into(backgroundImage);
            if (multiImageViewModel.getPreSelectedImages().size() > 1){
                if (recyclerView == null)
                    return;

                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new GridLayoutManager(mContext,1,LinearLayoutManager.HORIZONTAL,false));
                recyclerView.setAdapter(new AttachedImagesAdapter(mContext,multiImageViewModel.getPreSelectedImages(),null));
            }
        }else{
            backgroundImage.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
        }
    }


    // Setting up location and post description of all type of content
    private void setContentExtraInfo(@NonNull final TextView locationView, @NonNull final TextView descriptionView){
        String location = "" + getArguments().getString(HashMapKeys.CONTENT_LOCATION_KEY,"");
        String description = getArguments().getString(HashMapKeys.CONTENT_DESCRIPTION_KEY,"");
        locationView.setText(location);

        if (description.equals("")){
            descriptionView.setVisibility(View.GONE);
            return;
        }

        Spanned spanned = getCollapsedDescription(description);
        if (spanned != null)
            descriptionView.setText(getCollapsedDescription(description));
        else
            descriptionView.setText(DocNormalizer.htmlToNormal(description).toString().trim());
    }

    private Spanned getCollapsedDescription(@NonNull final String htmlString){
        int brTag = 0;
        int brPosition = htmlString.indexOf("<br>");
        int lastBrPosition = -1;
        if (brPosition != -1){
            if (!(htmlString.length() > 60  &&  brPosition >= 60)){
                while (htmlString.startsWith("<br>",brPosition)){
                    brTag++;
                    brPosition = htmlString.indexOf("<br>",brPosition + 1);
                    if (brPosition != -1){
                        lastBrPosition = brPosition;
                    }
                    if (brPosition == -1 || brTag == 2 || brPosition >=60)
                        break;
                }
            }
        }
        String description;
        if (lastBrPosition != -1){
            description = htmlString.substring(0,lastBrPosition+5)+"...more </p>";
        }
        else{
            if (htmlString.length() > 60){
                String collap = DocNormalizer.htmlToNormal(htmlString).toString();
                if (collap.length() > 50){
                    description = collap.substring(0,50)+"...more";
                }else{
                    return new SpannableString(collap);
                }
            }else{
                return null;
            }
        }
        String collapsedDescription = DocNormalizer.htmlToNormal(description).toString().trim();
        SpannableString spannableString = new SpannableString(collapsedDescription);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.touch_background_color)),
                collapsedDescription.indexOf("...more"),
                collapsedDescription.length(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        imageOrMemesPreview = null;
        dreamsOrIdeasPreview = null;
        storyPreview = null;
        poemPreview = null;
        quotesPreview = null;
        questionPreview = null;
    }
}