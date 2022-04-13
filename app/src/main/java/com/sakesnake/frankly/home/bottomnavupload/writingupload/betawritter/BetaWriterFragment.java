package com.sakesnake.frankly.home.bottomnavupload.writingupload.betawritter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.bottomnavupload.writingupload.HashMapKeys;

import org.jetbrains.annotations.NotNull;


public class BetaWriterFragment extends Fragment implements ColorPaletteAdapter.GetSelectedColor {

    private Context mContext;
    private HtmlFormatViewModel htmlFormatViewModel;

    // layout views
    private View rootView;
    private EditText blogOrNewsTitleEditText, blogOrNewsContentEditText;
    private TextView titleHeadingTextView, contentHeadingTextView;
    private Toolbar layoutToolbar;
    private ImageView boldImageView, italicImageView, closeWarningMessageImageView;
    private RelativeLayout warningMessageRelativeLayout;
    private RecyclerView colorPaletteRecyclerView;
    private NavController navController;

    private Animation slideUpAnimation;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // init root view
        rootView = inflater.inflate(R.layout.fragment_beta_writer, container, false);


        // getting navigation controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // init view model
        htmlFormatViewModel = new ViewModelProvider(requireActivity()).get(HtmlFormatViewModel.class);

        // init views
        blogOrNewsTitleEditText = (EditText) rootView.findViewById(R.id.beta_fragment_blog_or_news_title_edit_text);
        blogOrNewsContentEditText = (EditText) rootView.findViewById(R.id.beta_writer_fragment_content_edit_text);
        titleHeadingTextView = (TextView) rootView.findViewById(R.id.beta_fragment_writer_title_text_view);
        contentHeadingTextView = (TextView) rootView.findViewById(R.id.beta_fragment_writer_content_text_view);
        layoutToolbar = (Toolbar) rootView.findViewById(R.id.beta_writer_fragment_toolbar);
        boldImageView = (ImageView) rootView.findViewById(R.id.beta_fragment_bold_image_view);
        italicImageView = (ImageView) rootView.findViewById(R.id.beta_fragment_italic_image_view);
        closeWarningMessageImageView = (ImageView) rootView.findViewById(R.id.beta_fragment_close_warning_image_view);
        warningMessageRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.beta_writer_warning_msg_relative_layout);
        colorPaletteRecyclerView = (RecyclerView) rootView.findViewById(R.id.beta_writer_fragment_color_palette_recycler_view);


        // getting arguments from previous fragment
        if (getArguments() != null) {
            titleHeadingTextView.setText(getArguments().getString(HashMapKeys.BUNDLE_TITLE_HEADING_KEY));
            blogOrNewsTitleEditText.setHint(getArguments().getString(HashMapKeys.BUNDLE_BLOG_OR_NEWS_TITLE_HINT_KEY));
            contentHeadingTextView.setText(getArguments().getString(HashMapKeys.BUNDLE_CONTENT_HEADING_KEY));
            blogOrNewsContentEditText.setHint(getArguments().getString(HashMapKeys.BUNDLE_BLOG_OR_NEWS_CONTENT_HINT_KEY));
        }


        // setting recycler view
        ColorPaletteAdapter recyclerAdapter = new ColorPaletteAdapter(mContext.getResources().getIntArray(R.array.colors), mContext, this);
        colorPaletteRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 1, LinearLayoutManager.HORIZONTAL, false));
        colorPaletteRecyclerView.setAdapter(recyclerAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // making text bold
        boldImageView.setOnClickListener(bold -> {
            if (blogOrNewsContentEditText.getSelectionStart() == blogOrNewsContentEditText.getSelectionEnd()) {
                Toast.makeText(mContext, "Please select some CONTENT text!!", Toast.LENGTH_SHORT).show();
                return;
            }

            Spannable spannable = blogOrNewsContentEditText.getText();
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    blogOrNewsContentEditText.getSelectionStart(),
                    blogOrNewsContentEditText.getSelectionEnd(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        });

        // making text italic
        italicImageView.setOnClickListener(italic -> {
            if (blogOrNewsContentEditText.getSelectionStart() == blogOrNewsContentEditText.getSelectionEnd()) {
                Toast.makeText(mContext, "Please select some CONTENT text!!", Toast.LENGTH_SHORT).show();
                return;
            }

            Spannable spannable = blogOrNewsContentEditText.getText();
            spannable.setSpan(new StyleSpan(Typeface.ITALIC),
                    blogOrNewsContentEditText.getSelectionStart(),
                    blogOrNewsContentEditText.getSelectionEnd(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        });

        // close warning message
        closeWarningMessageImageView.setOnClickListener(closeWarning -> {
            slideUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
            warningMessageRelativeLayout.startAnimation(slideUpAnimation);
            warningMessageRelativeLayout.setVisibility(View.GONE);
        });


        // toolbar done listener
        layoutToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.done_multi_image_toolbar_menu_item) {
                String htmlFormat;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    htmlFormat = Html.toHtml(blogOrNewsContentEditText.getText(), 0);
                } else {
                    htmlFormat = Html.toHtml(blogOrNewsContentEditText.getText());
                }
                htmlFormatViewModel.setHtmlString(htmlFormat);
                navController.navigateUp();
            }
            return false;
        });

        // navigating up
        layoutToolbar.setNavigationOnClickListener(navi -> {
            navController.navigateUp();
        });
    }

    @Override
    public void getSelectedColor(int color) {
        if (blogOrNewsContentEditText.getSelectionStart() == blogOrNewsContentEditText.getSelectionEnd()) {
            Toast.makeText(mContext, "Please select some CONTENT text!!", Toast.LENGTH_SHORT).show();
            return;
        }

        Spannable spannable = blogOrNewsContentEditText.getText();
        spannable.setSpan(new ForegroundColorSpan(color),
                blogOrNewsContentEditText.getSelectionStart(),
                blogOrNewsContentEditText.getSelectionEnd(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        blogOrNewsTitleEditText = null;
        blogOrNewsContentEditText = null;
        titleHeadingTextView = null;
        contentHeadingTextView = null;
        layoutToolbar = null;
        boldImageView = null;
        italicImageView = null;
        closeWarningMessageImageView = null;
        warningMessageRelativeLayout = null;
        colorPaletteRecyclerView = null;
    }
}